package com.kalid.ms.ui.order

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.databinding.ActivityOrderListBinding
import com.kalid.ms.repository.OrderRepository
import com.kalid.ms.ui.adapter.OrderListAdapter
import com.kalid.ms.ui.viewmodel.OrderViewModel
import com.kalid.ms.utils.SharedPreferencesManager

class OrderListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderListBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var adapter: OrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        loadOrders()
    }

    private fun setupViewModel() {
        val database = KalidMsDatabase.getDatabase(this)
        val orderRepository = OrderRepository(database.orderDao(), database.orderItemDao())
        orderViewModel = ViewModelProvider(
            this,
            OrderViewModel.Factory(orderRepository)
        )[OrderViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = OrderListAdapter { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("orderId", order.id)
            startActivity(intent)
        }
        binding.ordersRecyclerView.adapter = adapter
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.newOrderButton.setOnClickListener {
            startActivity(Intent(this, NewOrderActivity::class.java))
        }
    }

    private fun loadOrders() {
        val userId = prefsManager.getUserId()
        if (userId == -1L) return

        orderViewModel.getUserOrders(userId).observe(this) { orders ->
            adapter.submitList(orders)
        }
    }
}
