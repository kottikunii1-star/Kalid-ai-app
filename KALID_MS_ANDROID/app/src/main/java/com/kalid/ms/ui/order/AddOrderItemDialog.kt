package com.kalid.ms.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kalid.ms.database.entities.OrderItem
import com.kalid.ms.database.entities.ProductType
import com.kalid.ms.databinding.DialogAddOrderItemBinding
import com.kalid.ms.utils.ValidationUtils

class AddOrderItemDialog(
    private val onItemAdded: (OrderItem) -> Unit
) : DialogFragment() {
    private lateinit var binding: DialogAddOrderItemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddOrderItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProductTypeSpinner()
        setupListeners()
    }

    private fun setupProductTypeSpinner() {
        val productTypes = ProductType.values().map { it.name.replace("_", " ") }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, productTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productTypeSpinner.adapter = adapter
    }

    private fun setupListeners() {
        binding.addButton.setOnClickListener { addItem() }
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun addItem() {
        val width = binding.widthInput.text.toString()
        val height = binding.heightInput.text.toString()
        val pricePerSqm = binding.priceInput.text.toString()

        if (!ValidationUtils.isNotEmpty(width, height, pricePerSqm)) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ValidationUtils.isValidDouble(width) || !ValidationUtils.isValidDouble(height) ||
            !ValidationUtils.isValidDouble(pricePerSqm)) {
            Toast.makeText(context, "Invalid number format", Toast.LENGTH_SHORT).show()
            return
        }

        val productType = ProductType.values()[binding.productTypeSpinner.selectedItemPosition]
        val item = OrderItem(
            orderId = 0,
            productType = productType,
            width = width.toDouble(),
            height = height.toDouble(),
            pricePerSquareMeter = pricePerSqm.toDouble()
        )
        item.copy(
            area = item.calculateArea(),
            totalPrice = item.calculateTotalPrice()
        ).let { onItemAdded(it) }

        dismiss()
    }
}
