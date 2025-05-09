package org.itson.tripsplit.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Gasto
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.text.split

class GastoAdapter(context: Context, private val resource: Int, private val gastos: List<Gasto>) :
    ArrayAdapter<Gasto>(context, resource, gastos) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            // Inflar el layout personalizado
            view = inflater.inflate(resource, parent, false)

            // Crear y asignar el ViewHolder
            holder = ViewHolder(
                txtNombre = view.findViewById(R.id.txtNombreGasto),
                txtCantidad = view.findViewById(R.id.txtCantidadGasto),
                txtFecha = view.findViewById(R.id.txtFechaGasto),
                txtDetalle = view.findViewById(R.id.txtDetalleGasto),
                txtCategoria = view.findViewById(R.id.txtCategoria)
            )

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        // Obtener el gasto actual
        val gasto = gastos[position]

        // Rellenar los campos
        holder.txtNombre.text = gasto.nombre
        holder.txtCantidad.text = "${gasto.moneda} $${gasto.cantidad}"
        holder.txtFecha.text = formatDate(gasto.fecha)
        holder.txtCategoria.text = gasto.categoria

        // Mostrar quién pagó
        holder.txtDetalle.text = when {
            gasto.pagadoPor != null -> "Pagado por ${gasto.pagadoPor?.nombre?.split(" ")?.get(0)}"
            else -> "Sin información"
        }

        return view
    }

    // Clase auxiliar para mantener las vistas
    private data class ViewHolder(
        val txtNombre: TextView,
        val txtCantidad: TextView,
        val txtFecha: TextView,
        val txtDetalle: TextView,
        val txtCategoria: TextView
    )

    // Función para formatear la fecha si viene en formato "yyyy-MM-dd"
    private fun formatDate(dateStr: String): String {
        if (dateStr.isEmpty()) return ""

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        return try {
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}