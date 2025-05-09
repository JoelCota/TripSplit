package org.itson.tripsplit.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Deuda

class DeudaAdapter(
    private val context: Context,
    private val deudas: List<Deuda>,
    private val obtenerNombre: (String, (String) -> Unit) -> Unit
) : ArrayAdapter<Deuda>(context, R.layout.item_deuda, deudas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_deuda, parent, false)

        val txtDeuda = view.findViewById<TextView>(R.id.txtDeuda)
        val deuda = getItem(position)

        if (deuda != null) {
            obtenerNombre(deuda.deudor) { nombreDeudor ->
                obtenerNombre(deuda.acreedor) { nombreAcreedor ->
                    txtDeuda.text = "$nombreDeudor le debe a $nombreAcreedor: $${"%.2f".format(deuda.monto)}"
                }
            }
        }

        return view
    }
}