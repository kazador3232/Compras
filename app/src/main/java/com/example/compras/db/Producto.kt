package com.example.compras.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    var producto:String,
    var comprado:Boolean
)
