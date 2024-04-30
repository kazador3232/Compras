package com.example.compras.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDao {
    @Query("SELECT COUNT(*) FROM producto")
    fun count():Int
    @Query("SELECT * FROM producto ORDER BY comprado")
    fun getAll():List<Producto>
    @Query("SELECT * FROM producto WHERE id = :id")
    fun findById(id:Int):Producto
    @Insert
    fun insert(producto:Producto):Long
    @Insert
    fun insertAll(productos:Producto)
    @Update
    fun update(productos:Producto)
    @Delete
    fun delete(producto:Producto)
}