package org.itson.tripsplit.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import org.itson.tripsplit.R

class NuevoGastoFragment : Fragment(), ListaSeleccionDialogFragment.OnItemSelectedListener {

    private lateinit var spinner: Spinner
    private lateinit var txtPagadoPor: TextView
    private lateinit var txtDivididoEntre: TextView
    private lateinit var txtMoneda: TextView
    private lateinit var txtCategoria: TextView
    private lateinit var btnSetCurrency: Button
    private lateinit var btnCategories: Button

    private val miembros = arrayOf("Juan", "Maria", "Pedro") // Lista de miembros
    private val monedas = arrayOf("USD", "MXN", "EUR", "GBP")
    private val categorias = arrayOf("Comida", "Transporte", "Hospedaje", "Entretenimiento")
    private val viajes = arrayOf("Viaje 1", "Viaje 2", "Viaje 3", "Viaje 4") // Ejemplo de lista de viajes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_nuevo_gasto, container, false)


        // Inicializar los TextView
        txtPagadoPor = rootView.findViewById(R.id.txtPagadoPor)
        txtDivididoEntre = rootView.findViewById(R.id.txtDivididoEntre)
        txtMoneda = rootView.findViewById(R.id.txtMoneda)
        txtCategoria = rootView.findViewById(R.id.txtCategoria)

        // Inicializar los botones
        btnSetCurrency = rootView.findViewById(R.id.btnSelectCurrency)
        btnCategories = rootView.findViewById(R.id.btnSelectCategory)

        // Click en "Pagado por"
        txtPagadoPor.setOnClickListener {
            val dialog = MiembrosDialogFragment.newInstance(miembros, "single")
            dialog.show(parentFragmentManager, "MiembrosDialogFragment")
        }

        // Click en "Dividido entre"
        txtDivididoEntre.setOnClickListener {
            val dialogMultiple = MiembrosDialogFragment.newInstance(miembros, "multiple")
            dialogMultiple.show(parentFragmentManager, "MiembrosDialogFragment")
        }

        // Click en "Seleccionar Moneda"
        btnSetCurrency.setOnClickListener {
            val dialog =
                ListaSeleccionDialogFragment.newInstance(monedas, "Seleccionar Moneda", this)
            dialog.show(parentFragmentManager, "currencyDialog")
        }

        // Click en "Seleccionar Categoría"
        btnCategories.setOnClickListener {
            val dialog =
                ListaSeleccionDialogFragment.newInstance(categorias, "Seleccionar Categoría", this)
            dialog.show(parentFragmentManager, "categoryDialog")
        }

        // Escuchar resultados de selección
        setFragmentResultListener("pagadoPor") { _, bundle ->
            val nombre = bundle.getString("resultado") ?: return@setFragmentResultListener
            actualizarPagadoPor(nombre)
        }

        setFragmentResultListener("divididoEntre") { _, bundle ->
            val miembrosSeleccionados = bundle.getStringArrayList("resultado") ?: return@setFragmentResultListener
            actualizarDivididoEntre(miembrosSeleccionados)
        }

        return rootView
    }

    fun actualizarPagadoPor(nombre: String) {
        txtPagadoPor.text = "Pagado por $nombre "
    }

    fun actualizarDivididoEntre(miembrosSeleccionados: MutableList<String>) {
        val totalMiembros = miembros.size
        val texto: String = when {
            miembrosSeleccionados.size == 1 -> "y dividido entre  ${miembrosSeleccionados[0]}"
            miembrosSeleccionados.size == totalMiembros -> "y dividido entre  Todos"
            else -> "y dividido entre  ${miembrosSeleccionados[0]} y ${miembrosSeleccionados.size - 1} más"
        }
        txtDivididoEntre.text = texto
    }

    override fun onItemSelected(item: String) {
        when {
            monedas.contains(item) -> {
                txtMoneda.text = item
                Toast.makeText(requireContext(), "Moneda seleccionada: $item", Toast.LENGTH_SHORT).show()
            }
            categorias.contains(item) -> {
                txtCategoria.text = item
                Toast.makeText(requireContext(), "Categoría seleccionada: $item", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
