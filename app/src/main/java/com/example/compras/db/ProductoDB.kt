package com.example.compras.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Producto::class], version = 1)
abstract class ProductoDB : RoomDatabase() {
    abstract fun productoDao():ProductoDao
    companion object {
        @Volatile
        private var BASE_DATOS : ProductoDB? = null
        fun getInstance(contexto: Context):ProductoDB {
            return BASE_DATOS ?: synchronized(this) {
                Room.databaseBuilder(
                    contexto.applicationContext,
                    ProductoDB::class.java,
                    "Agenda.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { BASE_DATOS = it }
            }
        }
    }
}