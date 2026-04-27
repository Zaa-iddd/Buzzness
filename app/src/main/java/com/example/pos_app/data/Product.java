package com.example.pos_app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String name;
    public int quantity;
    public double price;
    public int salesCount; // To track most bought items
    public String qrCode; // Custom QR code identifier

    public Product(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.salesCount = 0;
    }
}