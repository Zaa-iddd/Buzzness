package com.example.pos_app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String description;
    public double amount; // Total amount
    public long timestamp;
    public String itemsJson; // Stores the list of items in JSON format for the receipt

    public Transaction(String description, double amount, long timestamp, String itemsJson) {
        this.description = description;
        this.amount = amount;
        this.timestamp = timestamp;
        this.itemsJson = itemsJson;
    }
}