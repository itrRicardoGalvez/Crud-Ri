package ricardo.galvez.crudri

import RecyclerViewHelper.Adaptador
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.dataclassProductos
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rcvProductos)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //1- mandar a llamar a todos los elementos de la pantalla
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtPrecio = findViewById<EditText>(R.id.txtPrecio)
        val txtCantidad = findViewById<EditText>(R.id.txtCantidad)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)

        fun Limpiar(){
            txtNombre.setText("")
            txtCantidad.setText("")
            txtPrecio.setText("")
        }

        ////////////////////TODO: mostrar datos//////////////////////////////

        //////////////////////////Mostrar////////////////////////////
        val rcvProductos = findViewById<RecyclerView>(R.id.rcvProductos)

        //Asignar un layout al RecyclerView
        rcvProductos.layoutManager = LinearLayoutManager(this)

        //Funcion para obtener datos
        fun obtenerDatos(): List<dataclassProductos>{
            val objConexion = ClaseConexion().cadenaConexion()
            val statement = objConexion?.createStatement()
            val resultSet = statement?.executeQuery("select * from tbProductos0")!!

            val productos = mutableListOf<dataclassProductos>()
            while (resultSet.next()){
                val uuid = resultSet.getString("uuid")
                val nombre = resultSet.getString("nombreProducto")
                val precio = resultSet.getInt("precio")
                val cantidad = resultSet.getInt("cantidad")
                val producto = dataclassProductos(uuid, nombre, precio, cantidad)
                productos.add(producto)
            }
            return productos
        }
        //Asignar un adaptador
        CoroutineScope(Dispatchers.IO).launch {
            val productosDB = obtenerDatos()
            withContext(Dispatchers.Main){
                val miAdaptador = Adaptador(productosDB)
                rcvProductos.adapter = miAdaptador
            }
        }

        /////////////////TODO: guardar datos/////////////////
        //2- Programar el boton
        btnAgregar.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO){

                //Guardar datos
                //1- creo un objeto de la clase conexión
                val claseConexion = ClaseConexion().cadenaConexion()

                //2- creo una variable que contenga un PreparedStatement
                val addProduct = claseConexion?.prepareStatement("insert into tbProductos (uuid, nombreProducto, precio, cantidad) values(?,?,?,?)")!!
                addProduct.setString(1, UUID.randomUUID().toString())
                addProduct.setString(2, txtNombre.text.toString())
                addProduct.setInt(3, txtPrecio.text.toString().toInt())
                addProduct.setInt(4, txtCantidad.text.toString().toInt())
                addProduct.executeUpdate()

                val nuevosProductos = obtenerDatos()
                withContext(Dispatchers.Main){
                    (rcvProductos.adapter as? Adaptador)?.actualizarLista(nuevosProductos)
                }
            }
            //Limpiar()
        }

    }
}
