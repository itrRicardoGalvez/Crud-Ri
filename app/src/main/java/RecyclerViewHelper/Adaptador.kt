package RecyclerViewHelper

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.dataclassProductos
import ricardo.galvez.crudri.R

    class Adaptador(private var Datos: List<dataclassProductos>) : RecyclerView.Adapter<ViewHolder>() {

        fun actualizarLista(nuevaLista: List<dataclassProductos>){
            Datos = nuevaLista
            notifyDataSetChanged()
        }

        //Funciòn para actualizar el recyclerview
        //Cuando actualizo los datos

        fun actualizarListaDespuesDeActualizarDatos(uuid: String, nuevoNombre: String){
            val index = Datos.indexOfFirst { it.uuid == uuid }
            Datos[index].nombreProducto = nuevoNombre
            notifyItemChanged(index)
        }

        fun eliminarRegistro(nombreProducto: String, posicion: Int){

            //1- Crear un objeto de la clase conexion
            val objConexion = ClaseConexion().cadenaConexion()

            //Quitar el elemento de la lista
            val listaDatos = Datos.toMutableList()
            listaDatos.removeAt(posicion)

            //Quitar de la base de datos
            GlobalScope.launch(Dispatchers.IO){
                val delProducto = objConexion?.prepareStatement("delete tbProductos where nombreProducto = ?")!!
                delProducto.setString(1, nombreProducto)
                delProducto.executeUpdate()

                val commit = objConexion.prepareStatement("commit")!!
                commit.executeUpdate()
            }

            //le decimos al adaptador que se eliminaron los datos
            Datos = listaDatos.toList()
            notifyItemRemoved(posicion)
            notifyDataSetChanged()
        }

        fun actualizarProducto(nombreProducto: String, uuid: String){

            //1- Creo una corrutina
            GlobalScope.launch(Dispatchers.IO){
                //1- Creo un objeto de la clase de conexion
                val objConexion = ClaseConexion().cadenaConexion()

                //2- Creo una variable que contenga un preparestatemente
                val updateProducto = objConexion?.prepareStatement("update tbProductos set nombreProducto = ? where uuid = ?")!!
                updateProducto.setString(1, nombreProducto)
                updateProducto.setString(2, uuid)
                updateProducto.executeUpdate()

                val commit = objConexion?.prepareStatement("commit")!!
                commit.executeUpdate()

                withContext(Dispatchers.Main){
                    actualizarListaDespuesDeActualizarDatos(uuid, nombreProducto)
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val vista =
                LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
            return ViewHolder(vista)
        }
        override fun getItemCount() = Datos.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val producto = Datos[position]
            holder.textView.text = producto.nombreProducto

            val item = Datos[position]

            holder.imgBorrar.setOnClickListener(){
                //Creamos una alerta
                //1- Creamos el contexto

                val context = holder.itemView.context

                //Creo la alerta
                val builder = AlertDialog.Builder(context)
                //Le pongo un titulo a mi alerta

                builder.setTitle("¿Esta seguro?")
                //Ponerle un mensaje

                builder.setMessage("¿Deseas eliminar el registro?")

                //Agregamos los botones

                builder.setPositiveButton("Si"){dialog, which ->
                    eliminarRegistro(item.nombreProducto, position)
                }
                builder.setNegativeButton("No"){dialog, which ->
                }

                //Creamos la alerta
                val alertDialog = builder.create()
                //Mostramos la alerta
                alertDialog.show()
                /////////
            }

            holder.imgEditar.setOnClickListener{

                val context = holder.itemView.context

                //Creo la alerta
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Editar nombre")

                //Agregamos un cuadro de texto para que el usuario
                //pueda escribir el nuevo nombre

                val cuadritoNuevoNombre = EditText(context)
                cuadritoNuevoNombre.setHint(item.nombreProducto)
                builder.setView(cuadritoNuevoNombre)

                builder.setPositiveButton("Actualizar"){ dialog, wich ->
                    actualizarProducto(cuadritoNuevoNombre.text.toString(), item.uuid)
                }

                builder.setNegativeButton("Cancelar"){ dialog, wich ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }

            //Darle clic a la card
            holder.itemView.setOnClickListener{

            }
        }
    }
