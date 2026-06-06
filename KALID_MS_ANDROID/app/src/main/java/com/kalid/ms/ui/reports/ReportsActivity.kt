package com.kalid.ms.ui.reports

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.databinding.ActivityReportsBinding
import com.kalid.ms.repository.OrderRepository
import com.kalid.ms.ui.viewmodel.OrderViewModel
import com.kalid.ms.utils.CurrencyFormatter
import com.kalid.ms.utils.SharedPreferencesManager
import java.util.*

class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        setupViewModel()
        setupListeners()
        loadReports()
    }

    private fun setupViewModel() {
        val database = KalidMsDatabase.getDatabase(this)
        val orderRepository = OrderRepository(database.orderDao(), database.orderItemDao())
        orderViewModel = ViewModelProvider(
            this,
            OrderViewModel.Factory(orderRepository)
        )[OrderViewModel::class.java]
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun loadReports() {
        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        // Total Orders
        orderViewModel.getTotalOrdersCount(userId).observe(this) { count ->
            binding.totalOrdersValue.text = count.toString()
        }

        // Total Revenue
        orderViewModel.getTotalRevenue(userId).observe(this) { revenue ->
            binding.totalRevenueValue.text = CurrencyFormatter.formatCurrency(revenue)
        }

        // Total Advance Payments
        orderViewModel.getTotalAdvancePayments(userId).observe(this) { advance ->
            binding.totalAdvanceValue.text = CurrencyFormatter.formatCurrency(advance)
        }

        // Total Remaining Balance
        orderViewModel.getTotalRemainingBalance(userId).observe(this) { remaining ->
            binding.totalRemainingValue.text = CurrencyFormatter.formatCurrency(remaining)
        }
    }
}
