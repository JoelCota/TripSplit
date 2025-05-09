package org.itson.tripsplit.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import org.itson.tripsplit.R
import org.itson.tripsplit.data.adapter.DeudaAdapter
import org.itson.tripsplit.data.adapter.GastoAdapter
import org.itson.tripsplit.data.model.Deuda
import org.itson.tripsplit.data.repository.GrupoRepository
import org.itson.tripsplit.databinding.FragmentReporteViajesBinding
import org.itson.tripsplit.repository.GastoRepository

class ReporteViajesFragment : Fragment(R.layout.fragment_reporte_viajes) {

    private var _binding: FragmentReporteViajesBinding? = null
    private val binding get() = _binding!!
    private val gastosRepository=GastoRepository()
    private val gruposRepository = GrupoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReporteViajesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listGastos = view.findViewById<ListView>(R.id.listaGastos)
        val listDeudas = view.findViewById<ListView>(R.id.listaDeudas)

        val grupoId = arguments?.getString("grupoId") ?: return
        val totalGastado: TextView= binding.root.findViewById(R.id.txtTotalGastado)
        val txtTripTitle: TextView= binding.root.findViewById(R.id.txtTituloViaje)
        gastosRepository.obtenerTotalGastado(grupoId) { total ->
            val total = total
            totalGastado.text = "Han gastado ${total}"
        }
        gruposRepository.getNombreGrupo(grupoId,{nombreGrupo ->
            if(nombreGrupo != null){
                txtTripTitle.text = nombreGrupo
            }
        })
        // Boton Listado de gastos totales
        binding.btnListadoGastos.setOnClickListener {
            binding.listaGastos.visibility = View.VISIBLE
            binding.barChart.visibility = View.GONE
            gastosRepository.obtenerGastos(grupoId) { listaGastos ->
                if (listaGastos.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay gastos registrados.", Toast.LENGTH_SHORT).show()
                } else {
                    val adapter = GastoAdapter(requireContext(), R.layout.item_gasto, listaGastos)
                    listGastos.adapter = adapter
                }
            }
        }



        // Boton de los Saldos pendientes
        binding.btnSaldos.setOnClickListener {
            binding.listaGastos.visibility = View.GONE
            binding.barChart.visibility = View.GONE
            binding.listaDeudas.visibility = View.VISIBLE

            gastosRepository.calcularDeudasPorIdGrupo(grupoId) { deudas ->
                if (deudas.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay gastos registrados.", Toast.LENGTH_SHORT).show()
                } else {
                    val adapter = DeudaAdapter(requireContext(), deudas) { userId, callback ->
                        FirebaseDatabase.getInstance().getReference("usuarios").child(userId)
                            .get()
                            .addOnSuccessListener {
                                val nombre =
                                    it.child("nombre").getValue(String::class.java) ?: "Desconocido"
                                callback(nombre.split(" ")[0])
                            }
                    }
                    listDeudas.adapter = adapter
                }
            }
        }
        // Boton de la gráfica por categoría
        binding.btnGrafica.setOnClickListener {
            binding.listaGastos.visibility = View.VISIBLE
            binding.barChart.visibility = View.VISIBLE

            GastoRepository().obtenerTotalesPorCategoria(grupoId) { totales ->
                val categorias = totales.keys.toList()
                val datos = totales.values.toList()
                if (totales.isEmpty()){
                    Toast.makeText(requireContext(),"No existen gastos", Toast.LENGTH_SHORT).show()
                }
                // Enviamos los datos a la vista personalizada
                binding.barChart.setData(datos, categorias)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


