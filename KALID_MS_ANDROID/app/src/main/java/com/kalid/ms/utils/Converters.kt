package com.kalid.ms.utils

import androidx.room.TypeConverter
import com.kalid.ms.database.entities.OrderStatus
import com.kalid.ms.database.entities.PaymentStatus
import com.kalid.ms.database.entities.ProductType

class Converters {
    @TypeConverter
    fun fromOrderStatus(value: OrderStatus?): String? = value?.name

    @TypeConverter
    fun toOrderStatus(value: String?): OrderStatus? = value?.let { OrderStatus.valueOf(it) }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String? = value?.name

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? = value?.let { PaymentStatus.valueOf(it) }

    @TypeConverter
    fun fromProductType(value: ProductType?): String? = value?.name

    @TypeConverter
    fun toProductType(value: String?): ProductType? = value?.let { ProductType.valueOf(it) }
}
