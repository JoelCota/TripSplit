package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.R
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.data.model.Usuario
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.repository.GastoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

class NuevoGastoFragment : Fragment(), ListaSeleccionDialogFragment.OnItemSelectedListener {

    private lateinit var spinner: Spinner
    private lateinit var txtPagadoPor: TextView
    private lateinit var txtDivididoEntre: TextView
    private lateinit var txtMoneda: TextView
    private lateinit var edtCantidad: EditText
    private lateinit var edtNombre: EditText
    private lateinit var txtCategoria: TextView
    private lateinit var btnSetCurrency: Button
    private lateinit var btnCategories: Button
    private lateinit var btnGuardar: Button
    private val grupoRepository = GrupoRepository()
    private val gastoRepository = GastoRepository()
    private val usuarioRepository = UserRepository()

    private var categorySelected : String = "General"
    private lateinit var paidBy : Usuario

    private lateinit var miembros: List<Usuario>
    private lateinit var divididosEntre: List<Usuario>
    private val monedas = arrayOf("USD", "MXN", "EUR")
    private val categorias = arrayOf("General","Comida", "Transporte", "Hospedaje", "Entretenimiento")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var monedaSeleccionada: String = "USD"
        val rootView = inflater.inflate(R.layout.fragment_nuevo_gasto, container, false)
        val grupoId = arguments?.getString("grupoId") ?: ""
        Log.d(" ID NUEVO GASTO", grupoId)
        edtNombre=rootView.findViewById(R.id.edtNombreGasto)
        edtCantidad=rootView.findViewById(R.id.edtCantidad)
        txtPagadoPor = rootView.findViewById(R.id.txtPagadoPor)
        txtDivididoEntre = rootView.findViewById(R.id.txtDivididoEntre)
        txtMoneda = rootView.findViewById(R.id.txtMoneda)
        txtCategoria = rootView.findViewById(R.id.txtCategoria)
        btnGuardar= rootView.findViewById(R.id.btnGuardar)
        btnSetCurrency = rootView.findViewById(R.id.btnSelectCurrency)
        btnCategories = rootView.findViewById(R.id.btnSelectCategory)

        grupoRepository.obtenerUsuariosDeGrupo(
            grupoId = grupoId ,
            onResultado = { usuarios ->
                miembros = usuarios
                Log.d("INFO GASTO", usuarios.toString())
                divididosEntre = usuarios // Selección por defecto: todos
            }
        )
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (!userId.isNullOrEmpty()){
        usuarioRepository.getUser(
            userId,
            callback = { usuario ->
                if (usuario != null) {
                    paidBy=usuario
                    divididosEntre= listOf(usuario)
                }
            }
        )
        }

        // Click en "Pagado por"
        txtPagadoPor.setOnClickListener {
            Log.d("NuevoGastoFragment", "PagadoPor")
            val dialog = MiembrosDialogFragment.newInstance(miembros, "single")
            dialog.show(parentFragmentManager, "MiembrosDialogFragment")
        }

        // Click en "Dividido entre"
        txtDivididoEntre.setOnClickListener {
            Log.d("NuevoGastoFragment", "dividido entre")
            val dialogMultiple = MiembrosDialogFragment.newInstance(miembros, "multiple")
            dialogMultiple.show(parentFragmentManager, "MiembrosDialogFragment")
        }

        btnSetCurrency.setOnClickListener {
            val dialog =
                ListaSeleccionDialogFragment.newInstance(monedas, "Seleccionar Moneda", object : ListaSeleccionDialogFragment.OnItemSelectedListener {
                    override fun onItemSelected(item: String) {
                        monedaSeleccionada = item
                        txtMoneda.text = item
                        Log.d("MonedaSeleccionada", "Moneda seleccionada: $item")
                    }
                })
            dialog.show(parentFragmentManager, "currencyDialog")
        }
        btnGuardar.setOnClickListener {
            if(edtNombre.text.isEmpty() || edtCantidad.text.isEmpty()){
                Toast.makeText(requireContext(), "Todos los campos deben estar llenos.", Toast.LENGTH_SHORT).show()
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cantidad = edtCantidad.text.toString().toDouble()
                val gasto = Gasto(
                    nombre = edtNombre.text.toString(),
                    categoria = categorySelected,
                    cantidad = cantidad,
                    moneda=monedaSeleccionada,
                    fecha = sdf.format(Date()),
                    pagadoPor = paidBy,
                    divididoEntre = divididosEntre
                )


                gastoRepository.agregarGasto(
                    grupoId, gasto){
                        success ->
                    if (success) {
                        // Si el gasto se agrega correctamente
                        println("Gasto agregado con éxito")
                    } else {
                        // Si algo falló
                        println("Error al agregar el gasto")
                    }
                }
            }
        }

        // Click en "Seleccionar Categoría"
        btnCategories.setOnClickListener {
            val dialog =
                ListaSeleccionDialogFragment.newInstance(categorias, "Seleccionar Categoría", this)
            dialog.show(parentFragmentManager, "categoryDialog")
        }

        setFragmentResultListener("pagadoPor") { _, bundle ->
            val usuario = bundle.getParcelable<Usuario>("usuarioSeleccionado") ?: return@setFragmentResultListener
            paidBy = usuario
            Log.d("ResultDialog", paidBy.toString())
            actualizarPagadoPor(usuario.nombre)
        }

        setFragmentResultListener("divididoEntre") { _, bundle ->
            val miembrosSeleccionados = bundle.getStringArrayList("resultado") ?: return@setFragmentResultListener
            divididosEntre = divididosEntre.filter { miembro -> miembro.id in miembrosSeleccionados }
            actualizarDivididoEntre(divididosEntre)
        }



        return rootView
    }

    fun actualizarPagadoPor(nombre: String) {
        txtPagadoPor.text = "Pagado por $nombre "
    }

    fun actualizarDivididoEntre(miembrosSeleccionados: List<Usuario>) {
        val totalMiembros = 3
        val texto: String = when {
            miembrosSeleccionados.size == 1 -> "y dividido entre  ${miembrosSeleccionados[0].nombre}"
            miembrosSeleccionados.size == totalMiembros -> "y dividido entre  Todos"
            else -> "y dividido entre  ${miembrosSeleccionados[0].nombre} y ${miembrosSeleccionados.size - 1} más"
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
                categorySelected=item
                txtCategoria.text = item
                Toast.makeText(requireContext(), "Categoría seleccionada: $item", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
