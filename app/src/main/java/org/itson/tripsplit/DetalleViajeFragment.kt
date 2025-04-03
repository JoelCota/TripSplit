package org.itson.tripsplit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.itson.tripsplit.databinding.FragmentDetalleViajeBinding

class DetalleViajeFragment : Fragment(R.layout.fragment_detalle_viaje) {

    private var _binding: FragmentDetalleViajeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleViajeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Boton Listado de gastos totales
        binding.btnListadoGastos.setOnClickListener {
            binding.textViewGastosTotales.apply {
                visibility = View.VISIBLE
                text = "Gastos totales:\nGasto restaurante: MX$50.00\nGasto restaurante: MX$60.00"
            }
            binding.barChart.visibility = View.GONE
            binding.textViewSaldosPendientes.visibility = View.GONE
        }

        // Boton de los Saldos pendientes
        binding.btnSaldos.setOnClickListener {
            binding.textViewSaldosPendientes.apply {
                visibility = View.VISIBLE
                text = "Saldos pendientes:\n\nJuan Daniel Castro debe MX$200.00 a Diego Hernandez\nDiego Hernandez debe MX$150.00 a Joel Lopez Cota\nJoel Lopez Cota debe MX$50.00 a Juan Daniel Castro"
            }
            binding.barChart.visibility = View.GONE
            binding.textViewGastosTotales.visibility = View.GONE
        }

        // Boton de la gráfica por categoría
        binding.btnGrafica.setOnClickListener {
            binding.textViewSaldosPendientes.visibility = View.GONE
            binding.textViewGastosTotales.visibility = View.GONE
            binding.barChart.visibility = View.VISIBLE

            val datos = listOf(50f, 75f, 40f, 90f, 60f)
            val categorias = listOf("Comida", "Hospedaje", "Transporte", "Tours", "Otros")

            // Enviamos los datos a la vista personalizada
            binding.barChart.setData(datos, categorias)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


