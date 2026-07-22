package com.example.roomdatabase;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey
    public int id;
    public String title;
    public double price;
    public String description;
    public String category;
    public String image;

    public Product() {}
}