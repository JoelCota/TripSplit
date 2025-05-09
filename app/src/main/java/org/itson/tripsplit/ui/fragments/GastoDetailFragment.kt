package org.itson.tripsplit.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.R
import org.itson.tripsplit.data.adapter.GastoAdapter
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.repository.GastoRepository

class GastoDetailFragment : Fragment() {

    private lateinit var txtTitulo: TextView
    private lateinit var txtCantidad: TextView
    private lateinit var txtPago: TextView
    private lateinit var btnEliminar: ImageButton
    private lateinit var btnEditar: ImageButton
    private lateinit var gastoAdapter: GastoAdapter
    private var listaGastos = mutableListOf<Gasto>()

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
        val grupoId = arguments?.getString("grupoId") ?: return
        val gastoId = arguments?.getString("gastoId") ?: return

        btnEliminar = view.findViewById(R.id.btnDeleteGroup)
        btnEditar = view.findViewById(R.id.btnEditGroup)

        btnEliminar.setOnClickListener {
            if (grupoId.isNotEmpty() && gastoId.isNotEmpty()) {
                mostrarDialogoConfirmacionEliminarGasto(grupoId, gastoId)
            }

        }
        val gastoRepo = GastoRepository()
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
        txtPago.text = "$nombrePagador pagó ${gastos.moneda} $${gastos.cantidad}"

        val usuarioActual = FirebaseAuth.getInstance().currentUser?.uid
        val esPagador = usuarioActual == gastos.pagadoPor?.id
        btnEditar.visibility = if (esPagador) View.VISIBLE else View.GONE
        btnEliminar.visibility = if (esPagador) View.VISIBLE else View.GONE
    }

    private fun calcularDeudas(gasto: Gasto): List<String> {
        val deudas = mutableListOf<String>()
        // Ejemplo: Supongamos que divididoEntre contiene los usuarios que deben pagar
        for (usuario in gasto.divididoEntre) {
            val montoAPagar = gasto.cantidad / gasto.divididoEntre.size
            val debe = usuario.id == FirebaseAuth.getInstance().currentUser?.uid
            val mensaje = if (debe) {
                "Debes $montoAPagar a ${usuario.nombre}"
            } else {
                "${usuario.nombre} te debe $montoAPagar"
            }
            deudas.add(mensaje)
        }
        return deudas
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
}
