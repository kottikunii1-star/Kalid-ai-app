package com.kalid.ms.ui.customer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.database.entities.Customer
import com.kalid.ms.databinding.ActivityCustomerDetailBinding
import com.kalid.ms.repository.CustomerRepository
import com.kalid.ms.repository.OrderRepository
import com.kalid.ms.ui.adapter.OrderListAdapter
import com.kalid.ms.ui.viewmodel.CustomerViewModel
import com.kalid.ms.ui.viewmodel.OrderViewModel
import com.kalid.ms.utils.SharedPreferencesManager
import com.kalid.ms.utils.ValidationUtils

class CustomerDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerDetailBinding
    private lateinit var customerViewModel: CustomerViewModel
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var orderAdapter: OrderListAdapter
    private var customerId: Long = -1
    private var currentCustomer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        customerId = intent.getLongExtra("customerId", -1)
        setupViewModels()
        setupRecyclerView()
        setupListeners()

        if (customerId != -1L) {
            loadCustomerDetails()
        }
    }

    private fun setupViewModels() {
        val database = KalidMsDatabase.getDatabase(this)
        val customerRepository = CustomerRepository(database.customerDao())
        val orderRepository = OrderRepository(database.orderDao(), database.orderItemDao())
        
        customerViewModel = ViewModelProvider(
            this,
            CustomerViewModel.Factory(customerRepository)
        )[CustomerViewModel::class.java]
        
        orderViewModel = ViewModelProvider(
            this,
            OrderViewModel.Factory(orderRepository)
        )[OrderViewModel::class.java]
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderListAdapter { }
        binding.customerOrdersRecyclerView.adapter = orderAdapter
        binding.customerOrdersRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { saveCustomer() }
    }

    private fun loadCustomerDetails() {
        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        customerViewModel.getUserCustomers(userId).observe(this) { customers ->
            currentCustomer = customers.find { it.id == customerId }
            currentCustomer?.let { customer ->
                binding.customerNameInput.setText(customer.name)
                binding.phoneInput.setText(customer.phoneNumber)
                binding.emailInput.setText(customer.email)
                binding.addressInput.setText(customer.address)

                orderViewModel.getCustomerOrders(userId, customerId).observe(this) { orders ->
                    orderAdapter.submitList(orders)
                }
            }
        }
    }

    private fun saveCustomer() {
        val name = binding.customerNameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()

        if (!ValidationUtils.isNotEmpty(name, phone)) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        if (customerId == -1L) {
            // Create new customer
            val newCustomer = Customer(
                userId = userId,
                name = name,
                phoneNumber = phone,
                email = email,
                address = address
            )
            customerViewModel.addCustomer(newCustomer).observe(this) { result ->
                result.onSuccess {
                    Toast.makeText(this, "Customer saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                result.onFailure { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Update existing customer
            val updatedCustomer = currentCustomer?.copy(
                name = name,
                phoneNumber = phone,
                email = email,
                address = address
            ) ?: return
            
            customerViewModel.updateCustomer(updatedCustomer).observe(this) { result ->
                result.onSuccess {
                    Toast.makeText(this, "Customer updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                result.onFailure { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
