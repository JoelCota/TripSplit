package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.R
import org.itson.tripsplit.data.adapter.GastoAdapter
import org.itson.tripsplit.data.model.Gasto
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.repository.GastoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import org.itson.tripsplit.data.adapter.DeudaAdapter

class GruposDetailFragment : Fragment() {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var btnEditTitle: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var txtTripTitle: TextView
    private lateinit var btnReporteGastos: TextView
    private val grupoRepository = GrupoRepository()
    private lateinit var listaGastos:List<Gasto>
    private lateinit var groupDescriptionTextView: TextView
    private lateinit var txtTotalBalanceValor: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grupos_detail, container, false)

        btnEditTitle = view.findViewById(R.id.btnEditGroup)
        btnReporteGastos= view.findViewById(R.id.btnReporteGastos)
        btnBack = view.findViewById(R.id.btnBack)
        val grupoId = arguments?.getString("grupoId") ?: ""
        Log.d("GruposDetail",grupoId)
        groupDescriptionTextView = view.findViewById(R.id.txtTotalGastadoValor)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_gasto)
        btnReporteGastos.setOnClickListener{
            val fragment = ReporteViajesFragment()

            val args = Bundle()
            args.putString("grupoId", grupoId)

            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        fab.setOnClickListener {
            // Navegar a NuevoGastoFragment
            val bundle = Bundle()
            bundle.putString("grupoId", arguments?.getString("grupoId"))
            val nuevoGastoFragment = NuevoGastoFragment()
            nuevoGastoFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, nuevoGastoFragment)
                .addToBackStack(null)
                .commit()
        }




        grupoRepository.obtenerGastosPorGrupo(grupoId) { listaGastos ->
            if (listaGastos.isNotEmpty()) {
                this.listaGastos =listaGastos
            } else {
                Log.d("Gasto", "No hay gastos registrados para este grupo.")
            }
        }

        //Implement  el que se cosnidan los gastos para poder guar


        if (currentUid != null) {
            grupoRepository.esUsuarioCreador(grupoId, currentUid) { esCreador ->
                if (esCreador) {
                    btnEditTitle.visibility = View.VISIBLE
                } else {
                    btnEditTitle.visibility = View.GONE
                }
            }
        }


        // Navegar a org.itson.tripsplit.fragments.EditarGrupoFragment
        btnEditTitle.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("grupoId", arguments?.getString("grupoId"))

            val editarfragment = EditarGrupoFragment()
            editarfragment.arguments = bundle // Pasa el grupoId al fragmento Editar Grupo

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editarfragment)
                .addToBackStack(null)
                .commit()
        }

        // Hacer que btnBack regrese al fragmento anterior
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listGastos = view.findViewById<ListView>(R.id.listGastos)
        val gruposId = arguments?.getString("grupoId") ?: return
        txtTotalBalanceValor=view.findViewById(R.id.txtTotalBalanceValor)

        mostrarTotalGastos(gruposId)
        val listView = view.findViewById<ListView>(R.id.listaDeudas)



        val gastoRepo = GastoRepository()

        gastoRepo.calcularDeudasPorIdGrupo(gruposId) { deudas ->

            val adapter = DeudaAdapter(requireContext(), deudas) { userId, callback ->
                FirebaseDatabase.getInstance().getReference("usuarios").child(userId)
                    .get()
                    .addOnSuccessListener {
                        val nombre = it.child("nombre").getValue(String::class.java) ?: "Desconocido"
                        callback(nombre.split(" ")[0])
                    }
            }

            listView.adapter = adapter
        }
        if (currentUid!=null){
            gastoRepo.obtenerBalanceUsuario(gruposId,currentUid){balance ->
                txtTotalBalanceValor.text="$${balance}"
            }
        }
        listGastos.setOnItemClickListener { _, _, position, _ ->
            val gastoSeleccionado = listGastos.adapter.getItem(position) as? Gasto
            if (gastoSeleccionado != null) {
                val  bundle = Bundle()
                bundle.putString("gastoId", gastoSeleccionado.id)
                bundle.putString("grupoId", gruposId)
                val detalleFragment = GastoDetailFragment()
                detalleFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detalleFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
        gastoRepo.obtenerGastos(gruposId) { listaGastos ->
            val adapter = GastoAdapter(requireContext(), R.layout.item_gasto, listaGastos)
            listGastos.adapter = adapter
        }

        txtTripTitle = view.findViewById(R.id.txtTripTitle)
        val grupoId = arguments?.getString("grupoId")
        if (grupoId != null) {
            val grupoRepository = GrupoRepository()
            grupoRepository.getNombreGrupo(grupoId) { nombreGrupo ->
                if(nombreGrupo != null){
                    txtTripTitle.text = nombreGrupo
                }
            }
        }
        val textViewMesActual = view.findViewById<TextView>(R.id.textMesGasto)
        actualizarMesActual(textViewMesActual)
    }
    private fun actualizarMesActual(textView: TextView) {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val fechaActual = Date()
        textView.text = dateFormat.format(fechaActual).capitalize()
    }
    private fun mostrarTotalGastos(grupoId: String){
        val gastoRepo = GastoRepository()
        gastoRepo.obtenerTotalGastado(grupoId) { total ->
            view?.findViewById<TextView>(R.id.txtTotalGastadoValor)?.text = "$${"%.2f".format(total)}"
        }
    }
}
