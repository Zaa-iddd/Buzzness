package com.example.pos_app.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AppDao {
    // Inventory
    @Query("SELECT * FROM products ORDER BY salesCount DESC")
    List<Product> getAllProductsByPopularity();

    @Insert
    void insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);

    @Query("SELECT COUNT(*) FROM products")
    int getProductCount();

    @Query("SELECT COUNT(*) FROM products WHERE quantity < :threshold")
    int getLowStockCount(int threshold);

    // Financials
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    List<Transaction> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);

    @Insert
    long insertTransaction(Transaction transaction);
    
    @Query("SELECT TOTAL(amount) FROM transactions")
    double getTotalBalance();

    @Query("SELECT COUNT(*) FROM transactions")
    int getTransactionCount();

    // Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSetting(UserSetting setting);

    @Query("SELECT value FROM settings WHERE `key` = :key")
    String getSetting(String key);
}