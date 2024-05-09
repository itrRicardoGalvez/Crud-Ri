package RecyclerViewHelper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import modelo.dataclassProductos
import ricardo.galvez.crudri.R

    class Adaptador(private var Datos: List<dataclassProductos>) : RecyclerView.Adapter<ViewHolder>() {

        fun actualizarLista(nuevaLista: List<dataclassProductos>){
            Datos = nuevaLista
            notifyDataSetChanged()
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
        }
    }
