package com.kalid.ms.ui.order

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.database.entities.*
import com.kalid.ms.databinding.ActivityNewOrderBinding
import com.kalid.ms.repository.CustomerRepository
import com.kalid.ms.repository.OrderRepository
import com.kalid.ms.ui.adapter.OrderItemAdapter
import com.kalid.ms.ui.viewmodel.CustomerViewModel
import com.kalid.ms.ui.viewmodel.OrderViewModel
import com.kalid.ms.utils.CurrencyFormatter
import com.kalid.ms.utils.DateTimeUtils
import com.kalid.ms.utils.SharedPreferencesManager
import com.kalid.ms.utils.ValidationUtils
import java.util.*

class NewOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewOrderBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var customerViewModel: CustomerViewModel
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var itemAdapter: OrderItemAdapter
    private var orderItems = mutableListOf<OrderItem>()
    private var selectedCustomerId: Long = -1
    private var pricePerSquareMeter: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        pricePerSquareMeter = prefsManager.getPricePerSquareMeter()
        setupViewModels()
        setupRecyclerView()
        setupListeners()
    }

    private fun setupViewModels() {
        val database = KalidMsDatabase.getDatabase(this)
        val orderRepository = OrderRepository(database.orderDao(), database.orderItemDao())
        val customerRepository = CustomerRepository(database.customerDao())
        
        orderViewModel = ViewModelProvider(
            this,
            OrderViewModel.Factory(orderRepository)
        )[OrderViewModel::class.java]
        
        customerViewModel = ViewModelProvider(
            this,
            CustomerViewModel.Factory(customerRepository)
        )[CustomerViewModel::class.java]
    }

    private fun setupRecyclerView() {
        itemAdapter = OrderItemAdapter { item ->
            orderItems.remove(item)
            itemAdapter.submitList(orderItems)
            updateTotals()
        }
        binding.orderItemsRecyclerView.adapter = itemAdapter
        binding.orderItemsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        
        binding.orderDateInput.setOnClickListener { showDatePicker { date ->
            binding.orderDateInput.setText(DateTimeUtils.formatDate(date))
        }}
        
        binding.deliveryDateInput.setOnClickListener { showDatePicker { date ->
            binding.deliveryDateInput.setText(DateTimeUtils.formatDate(date))
        }}
        
        binding.addItemButton.setOnClickListener { showAddItemDialog() }
        
        binding.advancePaymentInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateTotals()
        }
        
        binding.saveOrderButton.setOnClickListener { saveOrder() }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                onDateSelected(selectedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showAddItemDialog() {
        // Show dialog to add new order item
        val dialog = AddOrderItemDialog { item ->
            orderItems.add(item)
            itemAdapter.submitList(orderItems)
            updateTotals()
        }
        dialog.show(supportFragmentManager, "AddItemDialog")
    }

    private fun updateTotals() {
        val itemsTotal = orderItems.sumOf { it.calculateTotalPrice() }
        val advancePayment = CurrencyFormatter.parseDouble(
            binding.advancePaymentInput.text.toString()
        )
        val remainingBalance = itemsTotal - advancePayment

        binding.grandTotalValue.text = CurrencyFormatter.formatCurrency(itemsTotal)
        binding.remainingBalanceValue.text = CurrencyFormatter.formatCurrency(remainingBalance)
    }

    private fun saveOrder() {
        val customerName = binding.customerNameInput.text.toString().trim()
        val customerPhone = binding.customerPhoneInput.text.toString().trim()
        val orderDateStr = binding.orderDateInput.text.toString()
        val deliveryDateStr = binding.deliveryDateInput.text.toString()
        val advancePaymentStr = binding.advancePaymentInput.text.toString()

        if (!ValidationUtils.isNotEmpty(customerName, customerPhone, orderDateStr, deliveryDateStr)) {
            Toast.makeText(this, "Please fill all customer information", Toast.LENGTH_SHORT).show()
            return
        }

        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = prefsManager.getUserId()
        val orderDate = DateTimeUtils.getDateFromString(orderDateStr)
        val deliveryDate = DateTimeUtils.getDateFromString(deliveryDateStr)
        val grandTotal = orderItems.sumOf { it.calculateTotalPrice() }
        val advancePayment = CurrencyFormatter.parseDouble(advancePaymentStr)
        val remainingBalance = grandTotal - advancePayment

        // Create customer
        val customer = Customer(
            userId = userId,
            name = customerName,
            phoneNumber = customerPhone
        )

        // Save customer first
        customerViewModel.addCustomer(customer).observe(this) { result ->
            result.onSuccess { customerId ->
                // Create order
                val order = Order(
                    userId = userId,
                    customerId = customerId,
                    orderDate = orderDate,
                    deliveryDate = deliveryDate,
                    grandTotal = grandTotal,
                    advancePayment = advancePayment,
                    remainingBalance = remainingBalance,
                    status = OrderStatus.PENDING
                )

                // Save order
                orderViewModel.createOrder(order).observe(this) { orderResult ->
                    orderResult.onSuccess { orderId ->
                        // Save order items
                        orderItems.forEach { item ->
                            val newItem = item.copy(orderId = orderId)
                            orderViewModel.addOrderItem(newItem).observe(this) { }
                        }

                        Toast.makeText(this, "Order saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    orderResult.onFailure { error ->
                        Toast.makeText(this, "Error saving order: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error saving customer: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
