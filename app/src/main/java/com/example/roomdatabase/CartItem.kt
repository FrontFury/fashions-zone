package com.example.roomdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
class CartItem {
    @PrimaryKey
    var id: Int = 0
    var title: String = ""
    var price: Double = 0.0
    var image: String = ""
    var quantity: Int = 1

    constructor()

    constructor(product: Product) {
        this.id = product.id
        this.title = product.title
        this.price = product.price
        this.image = product.image
        this.quantity = 1
    }
}