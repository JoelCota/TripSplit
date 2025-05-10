package org.itson.tripsplit.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.itson.tripsplit.R
import org.itson.tripsplit.data.adapter.DeudaAdapter
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.data.model.Usuario
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.data.repository.UserRepository
import org.itson.tripsplit.repository.GastoRepository

class GastoDetailFragment : Fragment() {

    private lateinit var txtTitulo: TextView
    private lateinit var txtCantidad: TextView
    private lateinit var txtPago: TextView
    private lateinit var btnEliminar: ImageButton
    private lateinit var btnEditar: ImageButton
    private lateinit var listaResumenDeudas: ListView
    private val userRepository=UserRepository()
    private val grupoRepository= GrupoRepository()
    private val gastoRepository= GastoRepository()
    val usuarioActual = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gasto_detail, container, false)
        // Encontrar el botón
        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        // Hacer que btnBack regrese al fragmento anterior
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack() // Esto regresa al fragmento anterior
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         txtTitulo = view.findViewById(R.id.txtNombreGasto)
         txtCantidad = view.findViewById(R.id.txtCantidadGasto)
         txtPago = view.findViewById(R.id.txtPago)
        listaResumenDeudas=view.findViewById(R.id.listaMiembrosGasto)
        val grupoId = arguments?.getString("grupoId") ?: return
        val gastoId = arguments?.getString("gastoId") ?: return
        val gastoRepo = GastoRepository()
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        drawable?.setTint(ContextCompat.getColor(requireContext(), android.R.color.black))
        btnEliminar = view.findViewById(R.id.btnDeleteGroup)
        btnEliminar.setImageDrawable(drawable)
        btnEditar = view.findViewById(R.id.btnEditGroup)

        btnEliminar.setOnClickListener {
            if (grupoId.isNotEmpty() && gastoId.isNotEmpty()) {
                mostrarDialogoConfirmacionEliminarGasto(grupoId, gastoId)
            }
        }

        btnEditar.setOnClickListener {
            if (grupoId.isNotEmpty() && gastoId.isNotEmpty()) {
                gastoRepo.obtenerGastos(grupoId) { gastos ->
                    val gasto = gastos.find { it.id == gastoId }
                    if (gasto != null) {
                        grupoRepository.obtenerUsuariosDeGrupo(grupoId) { listaUsuarios ->
                            mostrarDialogoEditarGasto(grupoId, gasto, listaUsuarios)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gasto no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        gastoRepo.calcularDeudasPorIdGasto(grupoId, gastoId) {deudasList ->
            if (deudasList.isEmpty()) {
                Log.d("GastoDetailFragment", "No hay deudas disponibles")
            } else {
                // Obtener los IDs únicos de deudores y acreedores
                val idsUnicos = deudasList.flatMap { listOf(it.deudor, it.acreedor) }.toSet().toList()
                userRepository.obtenerNombresUsuarios(idsUnicos) { mapaNombres: Map<String, String> ->
                    val adapter = DeudaAdapter(requireContext(), deudasList, usuarioActual.toString(), mapaNombres)
                    listaResumenDeudas.adapter = adapter
                }
            }
        }

        gastoRepo.obtenerGastos(grupoId) { gastos ->
            val gasto = gastos.find { it.id == gastoId }
            if (gasto != null) {
                mostrarGastos(gasto)
            } else {
                Toast.makeText(requireContext(), "Gasto no encontrado", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun mostrarGastos(gastos: Gasto) {
        txtTitulo.text = gastos.nombre
        txtCantidad.text = "${gastos.moneda} $${gastos.cantidad}"
        val nombrePagador = gastos.pagadoPor?.nombre ?: "Desconocido"
        txtPago.text = "${nombrePagador.split(" ").get(0)} pagó ${gastos.moneda} $${gastos.cantidad}"

        val esPagador = usuarioActual == gastos.pagadoPor?.id
        btnEditar.visibility = if (esPagador) View.VISIBLE else View.INVISIBLE
        btnEliminar.visibility = if (esPagador) View.VISIBLE else View.INVISIBLE

    }

    private fun mostrarDialogoConfirmacionEliminarGasto(grupoId: String, gastoId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar gasto")
            .setMessage("¿Estás seguro de eliminar este gasto?")
            .setPositiveButton("Eliminar") { _, _ ->
                gastoRepository.eliminarGasto(grupoId, gastoId){
                   onComplete-> Log.d("GastoEdit","Gasto eliminado con exito")
                  parentFragmentManager.popBackStack()

                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun mostrarDialogoEditarGasto(grupoId: String, gastoOriginal: Gasto, listaUsuarios: List<Usuario>) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_editar_gasto, null)

        val inputNombre = view.findViewById<EditText>(R.id.editTextNombreGasto)
        val inputCantidad = view.findViewById<EditText>(R.id.editTextCantidadGasto)
        val spinnerPagadoPor = view.findViewById<Spinner>(R.id.spinnerPagadoPor)
        val spinnerMoneda = view.findViewById<Spinner>(R.id.spinnerMoneda)
        val spinnerCategoria = view.findViewById<Spinner>(R.id.spinnerCategoria)
        val selectorDivididoEntre=view.findViewById<TextView>(R.id.txtDivididoEntre)
        inputNombre.setText(gastoOriginal.nombre)
        inputCantidad.setText(gastoOriginal.cantidad.toString())

        val categorias = listOf("General", "Comida", "Transporte", "Hospedaje", "Ocio")
        val monedas = arrayOf("USD", "MXN", "EUR")
        //Lleando spinnerCategorias
        val adapterCategorias = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategorias
        val indexCategoria = categorias.indexOf(gastoOriginal.categoria)
        if (indexCategoria >= 0) spinnerCategoria.setSelection(indexCategoria)
        //Lleando spinerMonedas
        val adapterMonedas = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, monedas)
        adapterMonedas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMoneda.adapter = adapterMonedas
        val indexMoneda = monedas.indexOf(gastoOriginal.moneda)
        if (indexMoneda >= 0) spinnerMoneda.setSelection(indexMoneda)

        val nombresUsuarios = listaUsuarios.map { it.nombre }
        val adapterUsuarios = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresUsuarios)
        adapterUsuarios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPagadoPor.adapter = adapterUsuarios
        val indexPagadoPor = nombresUsuarios.indexOf(gastoOriginal.pagadoPor?.nombre)
        if (indexPagadoPor >= 0) spinnerPagadoPor.setSelection(indexPagadoPor)

        val seleccionados = BooleanArray(listaUsuarios.size) { i ->
            gastoOriginal.divididoEntre.any { it.nombre == listaUsuarios[i].nombre }
        }
        val seleccionadosTemp = mutableListOf<Usuario>().apply {
            addAll(gastoOriginal.divididoEntre)
        }
        fun actualizarTextoSeleccionados() {
            val texto = if (seleccionadosTemp.isEmpty()) {
                "Seleccionar usuarios"
            } else {
                seleccionadosTemp.joinToString(", ") { it.nombre }
            }
            selectorDivididoEntre.text = texto
        }
        actualizarTextoSeleccionados()

        selectorDivididoEntre.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar usuarios")
                .setMultiChoiceItems(nombresUsuarios.toTypedArray(), seleccionados) { _, which, isChecked ->
                    val usuario = listaUsuarios[which]
                    if (isChecked) {
                        if (!seleccionadosTemp.contains(usuario)) {
                            seleccionadosTemp.add(usuario)
                        }
                    } else {
                        seleccionadosTemp.remove(usuario)
                    }
                }
                .setPositiveButton("Aceptar") { _, _ -> actualizarTextoSeleccionados() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Editar gasto")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString()
                val nuevaCantidad = inputCantidad.text.toString().toDoubleOrNull()
                val nuevaCategoria = spinnerCategoria.selectedItem.toString()
                val nuevaMoneda = spinnerMoneda.selectedItem.toString()
                val nombrePagadoPor = spinnerPagadoPor.selectedItem.toString()
                val pagadoPor = listaUsuarios.find { it.nombre == nombrePagadoPor }

                if (nuevoNombre.isNotEmpty() && nuevaCantidad != null && pagadoPor != null && seleccionadosTemp.isNotEmpty()) {
                    val gastoActualizado = gastoOriginal.copy(
                        nombre = nuevoNombre,
                        cantidad = nuevaCantidad,
                        categoria = nuevaCategoria,
                        moneda = nuevaMoneda,
                        pagadoPor = pagadoPor,
                        divididoEntre = seleccionadosTemp.toList()
                    )

                    val repo = GastoRepository()
                    repo.actualizarGasto(grupoId, gastoActualizado) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Gasto actualizado", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Datos inválidos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }
}
