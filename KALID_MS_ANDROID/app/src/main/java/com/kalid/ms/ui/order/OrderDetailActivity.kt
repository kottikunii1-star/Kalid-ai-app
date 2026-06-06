package com.kalid.ms.ui.order

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalid.ms.database.KalidMsDatabase
import com.kalid.ms.database.entities.Order
import com.kalid.ms.database.entities.OrderStatus
import com.kalid.ms.databinding.ActivityOrderDetailBinding
import com.kalid.ms.repository.OrderRepository
import com.kalid.ms.ui.adapter.OrderItemAdapter
import com.kalid.ms.ui.viewmodel.OrderViewModel
import com.kalid.ms.utils.CurrencyFormatter
import com.kalid.ms.utils.DateTimeUtils

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var itemAdapter: OrderItemAdapter
    private var currentOrder: Order? = null
    private val orderId by lazy { intent.getLongExtra("orderId", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (orderId == -1L) {
            finish()
            return
        }

        setupViewModel()
        setupRecyclerView()
        setupListeners()
        loadOrderDetails()
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
        itemAdapter = OrderItemAdapter { item ->
            Toast.makeText(this, "Delete item", Toast.LENGTH_SHORT).show()
        }
        // binding.orderItemsRecyclerView.adapter = itemAdapter
        // binding.orderItemsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        
        binding.updateStatusButton?.setOnClickListener { updateOrderStatus() }
        binding.deleteOrderButton?.setOnClickListener { deleteOrder() }
    }

    private fun loadOrderDetails() {
        // Load order and items
        orderViewModel.getUserOrders(0).observe(this) { orders ->
            val order = orders.find { it.id == orderId }
            if (order != null) {
                currentOrder = order
                displayOrderDetails(order)
                loadOrderItems()
            }
        }
    }

    private fun displayOrderDetails(order: Order) {
        binding.apply {
            orderIdValue?.text = order.id.toString()
            orderDateValue?.text = DateTimeUtils.formatDate(order.orderDate)
            deliveryDateValue?.text = DateTimeUtils.formatDate(order.deliveryDate)
            grandTotalValue?.text = CurrencyFormatter.formatCurrency(order.grandTotal)
            advancePaymentValue?.text = CurrencyFormatter.formatCurrency(order.advancePayment)
            remainingBalanceValue?.text = CurrencyFormatter.formatCurrency(order.remainingBalance)
            statusValue?.text = order.status.name
        }
    }

    private fun loadOrderItems() {
        orderViewModel.getOrderItems(orderId).observe(this) { items ->
            itemAdapter.submitList(items)
        }
    }

    private fun updateOrderStatus() {
        if (currentOrder == null) return
        
        val newStatus = when (currentOrder!!.status) {
            OrderStatus.PENDING -> OrderStatus.IN_PROGRESS
            OrderStatus.IN_PROGRESS -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> OrderStatus.PENDING
        }

        val updatedOrder = currentOrder!!.copy(status = newStatus)
        orderViewModel.updateOrder(updatedOrder).observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show()
                currentOrder = updatedOrder
                displayOrderDetails(updatedOrder)
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteOrder() {
        if (currentOrder == null) return
        
        orderViewModel.deleteOrder(currentOrder!!).observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
