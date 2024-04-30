package com.example.compras

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.compras.db.Producto
import com.example.compras.db.ProductoDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val titulo = resources.getString(R.string.titulo)
        val agregar = resources.getString(R.string.agregar)
        val volver = resources.getString(R.string.volver)
        setContent {
            AppComprasUI(titulo, agregar, volver)
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()){
                Text(titulo)
            }
        }
    }
}
enum class Accion {
    LISTAR, CREAR
}
@Composable
fun AppComprasUI(titulo: String, agregar: String, volver: String) {
    val contexto                  = LocalContext.current
    val (productos, setProductos) = remember{ mutableStateOf( emptyList<Producto>() ) }
    val (accion, setAccion)       = remember{ mutableStateOf(Accion.LISTAR) }
    val agregar = agregar
    LaunchedEffect(productos) {
        withContext(Dispatchers.IO) {
            val db = ProductoDB.getInstance( contexto )
            setProductos( db.productoDao().getAll() )
            Log.v("AppComprasUI", "LaunchedEffect()")
        }
    }
    val onSave = {
        setAccion(Accion.LISTAR)
        setProductos(emptyList())
    }
    when(accion) {
        Accion.CREAR -> ProductoFormUI(null, onSave, agregar, volver)
        else -> ProductosListadoUI(
            productos, onSave,
            onAdd = { setAccion( Accion.CREAR )
            }, agregar
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosListadoUI(productos:List<Producto>, onSave:() -> Unit = {}, onAdd:() -> Unit = {},
        agregar: String) {
    val a = agregar
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAdd() },
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "agregar"
                    )
                },
                text = { Text(a) }
            )
        }
    ) { contentPadding ->
        if( productos.isNotEmpty() ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            )
            {
                items(productos) { producto ->
                    ProductoItemUI(producto, onSave)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay productos en la lista")
            }
        }
    }
}
@Composable
fun ProductoItemUI(producto:Producto, onSave:() -> Unit = {}) {
    val contexto        = LocalContext.current
    val alcanceCorrutina = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp)
    ) {
        if( producto.comprado ){
            Icon(
                Icons.Filled.Check,
                contentDescription = "Producto comprado",
                modifier = Modifier.clickable {
                    alcanceCorrutina.launch( Dispatchers.IO ) {
                        val dao = ProductoDB.getInstance( contexto ).productoDao()
                        producto.comprado = false
                        dao.update( producto )
                        onSave()
                    }
                }
            )
        }else {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = "Producto por comprar",
                modifier = Modifier.clickable {
                    alcanceCorrutina.launch( Dispatchers.IO ) {
                        val dao = ProductoDB.getInstance( contexto ).productoDao()
                        producto.comprado = true
                        dao.update( producto )
                        onSave()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            producto.producto,
            modifier = Modifier.weight(2f)
        )
        Icon(
            Icons.Filled.Delete,
            contentDescription = "Eliminar compra",
            modifier = Modifier.clickable {
                alcanceCorrutina.launch( Dispatchers.IO ) {
                    val dao = ProductoDB.getInstance( contexto ).productoDao()
                    dao.delete( producto )
                    onSave()
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormUI(c:Producto?, onSave:()->Unit = {}, agregar: String, volver: String){
    val contexto = LocalContext.current
    val (producto, setProducto) = remember { mutableStateOf(
        c?.producto ?: "" ) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val a = agregar
    val v = volver
    Scaffold(
        snackbarHost = { SnackbarHost( snackbarHostState) }
    ) {paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = producto,
                onValueChange = { setProducto(it) },
                label = { Text("Producto") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val dao = ProductoDB.getInstance(
                        contexto
                    ).productoDao()
                    val producto = Producto(
                        c?.id ?: 0, producto,
                        comprado = false
                    )
                    dao.insert(producto)
                    snackbarHostState.showSnackbar("Se agrego ${producto.producto} a la lista")
                    onSave()
                }
            }) {
                Text(a)
            }
            Button(onClick = {
                    onSave()
            }) {
                Text(v)
            }
        }
    }
}