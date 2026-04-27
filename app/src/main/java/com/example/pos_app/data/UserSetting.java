package com.example.pos_app.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings")
public class UserSetting {
    @PrimaryKey
    @NonNull
    public String key;
    public String value;

    public UserSetting(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
    }
}
