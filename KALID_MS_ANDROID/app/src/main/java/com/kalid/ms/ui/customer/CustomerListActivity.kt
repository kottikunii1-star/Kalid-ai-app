package com.kalid.ms.ui.customer

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.databinding.ActivityCustomerListBinding
import com.kalid.ms.repository.CustomerRepository
import com.kalid.ms.ui.adapter.CustomerListAdapter
import com.kalid.ms.ui.viewmodel.CustomerViewModel
import com.kalid.ms.utils.SharedPreferencesManager

class CustomerListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerListBinding
    private lateinit var customerViewModel: CustomerViewModel
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var adapter: CustomerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        loadCustomers()
    }

    private fun setupViewModel() {
        val database = KalidMsDatabase.getDatabase(this)
        val customerRepository = CustomerRepository(database.customerDao())
        customerViewModel = ViewModelProvider(
            this,
            CustomerViewModel.Factory(customerRepository)
        )[CustomerViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = CustomerListAdapter { customer ->
            val intent = Intent(this, CustomerDetailActivity::class.java)
            intent.putExtra("customerId", customer.id)
            startActivity(intent)
        }
        binding.customersRecyclerView.adapter = adapter
        binding.customersRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.addCustomerButton.setOnClickListener {
            startActivity(Intent(this, CustomerDetailActivity::class.java))
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) searchCustomers(newText)
                return true
            }
        })
    }

    private fun loadCustomers() {
        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        customerViewModel.getUserCustomers(userId).observe(this) { customers ->
            adapter.submitList(customers)
        }
    }

    private fun searchCustomers(query: String) {
        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        customerViewModel.searchCustomers(userId, query).observe(this) { customers ->
            adapter.submitList(customers)
        }
    }
}
