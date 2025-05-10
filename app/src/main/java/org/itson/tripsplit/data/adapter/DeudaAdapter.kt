package org.itson.tripsplit.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Deuda

class DeudaAdapter(
    context: Context,
    private val deudas: List<Deuda>,
    private val usuarioActualId: String,
    private val nombresUsuarios: Map<String, String>
) : ArrayAdapter<Deuda>(context, 0, deudas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_deuda, parent, false)
        val textoDeuda = view.findViewById<TextView>(R.id.txtDeuda)

        val deuda = deudas[position]
        val nombreDeudor = nombresUsuarios[deuda.deudor] ?: "Desconocido"
        val nombreAcreedor = nombresUsuarios[deuda.acreedor] ?: "Desconocido"

        val mensaje: String
        if (deuda.deudor == usuarioActualId) {
            mensaje = "Debes ${deuda.moneda}\$${"%.2f".format(deuda.monto)} a $nombreAcreedor"
            textoDeuda.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        } else if (deuda.acreedor == usuarioActualId) {
            mensaje = "$nombreDeudor te debe ${deuda.moneda}\$${"%.2f".format(deuda.monto)}"
            textoDeuda.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        } else {
            mensaje = "$nombreDeudor debe ${deuda.moneda} \$${"%.2f".format(deuda.monto)} a $nombreAcreedor"
            textoDeuda.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        textoDeuda.text = mensaje
        return view
    }
}

