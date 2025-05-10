package org.itson.tripsplit.ui.fragments

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.itson.tripsplit.R
import org.itson.tripsplit.data.adapter.DeudaAdapter
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.data.model.Usuario
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
        btnEliminar = view.findViewById(R.id.btnDeleteGroup)
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
                        cargarUsuariosDelGrupo(grupoId) { listaUsuarios ->
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
                eliminarGastoDeGrupo(grupoId, gastoId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarGastoDeGrupo(grupoId: String, gastoId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("gastosPorGrupo").child(grupoId).child(gastoId)

        ref.removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Gasto eliminado", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEditarGasto(grupoId: String, gastoOriginal: Gasto, listaUsuarios: List<Usuario>) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_editar_gasto, null)

        val inputNombre = view.findViewById<EditText>(R.id.editTextNombreGasto)
        val inputCantidad = view.findViewById<EditText>(R.id.editTextCantidadGasto)
        val spinnerPagadoPor = view.findViewById<Spinner>(R.id.spinnerPagadoPor)
        val spinnerCategoria = view.findViewById<Spinner>(R.id.spinnerCategoria)
        val selectorDivididoEntre = view.findViewById<TextView>(R.id.selectorDivididoEntre)

        inputNombre.setText(gastoOriginal.nombre)
        inputCantidad.setText(gastoOriginal.cantidad.toString())

        // Spinner de categoría
        val categorias = listOf("General", "Comida", "Transporte", "Hospedaje", "Ocio")
        val adapterCategorias = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategorias
        val indexCategoria = categorias.indexOf(gastoOriginal.categoria)
        if (indexCategoria >= 0) spinnerCategoria.setSelection(indexCategoria)

        // Spinner de quien pagó
        val nombresUsuarios = listaUsuarios.map { it.nombre }
        val adapterUsuarios = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresUsuarios)
        adapterUsuarios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPagadoPor.adapter = adapterUsuarios
        val indexPagadoPor = nombresUsuarios.indexOf(gastoOriginal.pagadoPor?.nombre)
        if (indexPagadoPor >= 0) spinnerPagadoPor.setSelection(indexPagadoPor)

        // Selección múltiple para "dividido entre"
        val seleccionados = BooleanArray(listaUsuarios.size) { i ->
            gastoOriginal.divididoEntre.any { it.nombre == listaUsuarios[i].nombre }
        }
        val seleccionadosTemp = mutableListOf<Usuario>().apply {
            addAll(gastoOriginal.divididoEntre)
        }

        fun actualizarTextoSeleccionados() {
            val texto = seleccionadosTemp.joinToString(", ") { it.nombre }
            selectorDivididoEntre.text = if (texto.isNotEmpty()) texto else "Usuarios que comparten"
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

        // Confirmar cambios
        AlertDialog.Builder(requireContext())
            .setTitle("Editar gasto")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString()
                val nuevaCantidad = inputCantidad.text.toString().toDoubleOrNull()
                val nuevaCategoria = spinnerCategoria.selectedItem.toString()
                val nombrePagadoPor = spinnerPagadoPor.selectedItem.toString()
                val pagadoPor = listaUsuarios.find { it.nombre == nombrePagadoPor }

                if (nuevoNombre.isNotEmpty() && nuevaCantidad != null && pagadoPor != null && seleccionadosTemp.isNotEmpty()) {
                    val gastoActualizado = gastoOriginal.copy(
                        nombre = nuevoNombre,
                        cantidad = nuevaCantidad,
                        categoria = nuevaCategoria,
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





    private fun cargarUsuariosDelGrupo(grupoId: String, callback: (List<Usuario>) -> Unit) {
        val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(grupoId)

        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaUsuarios = mutableListOf<Usuario>()
                for (userSnapshot in snapshot.children) {
                    val usuario = userSnapshot.getValue(Usuario::class.java)
                    usuario?.let { listaUsuarios.add(it) }
                }
                callback(listaUsuarios)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}
