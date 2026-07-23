package com.example.roomdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
class Product {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var title: String = ""
    var price: Double = 0.0
    var description: String = ""
    var category: String = ""
    var image: String = ""

    constructor()

    constructor(id: Int, title: String, price: Double, description: String, category: String, image: String) {
        this.id = id
        this.title = title
        this.price = price
        this.description = description
        this.category = category
        this.image = image
    }
}
