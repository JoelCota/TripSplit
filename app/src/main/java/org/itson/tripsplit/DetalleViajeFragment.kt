package org.itson.tripsplit

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

        // Configura la gráfica de barras
        setupBarChart()

        // Listener para el botón "Listado gastos totales"
        binding.btnListadoGastos.setOnClickListener {
            // Mostrar los gastos totales
            binding.textViewGastosTotales.apply {
                visibility = View.VISIBLE
                text = "Gastos totales:\nGasto restaurante: MX$50.00\nGasto restaurante: MX$60.00"
            }
            // Ocultar la gráfica
            binding.barChart.visibility = View.GONE

            // Ocultar saldos pendientes
            binding.textViewSaldosPendientes.visibility = View.GONE
        }

        // Listener para el botón "Saldos pendientes"
        binding.btnSaldos.setOnClickListener {
            // Mostrar los saldos pendientes
            binding.textViewSaldosPendientes.apply {
                visibility = View.VISIBLE
                text = "Saldos pendientes:\n\nJuan Daniel Castro debe MX$200.00 a Diego Hernandez\nDiego Hernandez debe MX$150.00 a Joel Lopez Cota\nJoel Lopez Cota debe MX$50.00 a Juan Daniel Castro"
            }
            // Ocultar la gráfica
            binding.barChart.visibility = View.GONE

            // Ocultar gastos totales
            binding.textViewGastosTotales.visibility = View.GONE
        }

        // Listener para el botón "Gráfica por categoría"
        binding.btnGrafica.setOnClickListener {
            // Esconder los saldos pendientes
            binding.textViewSaldosPendientes.visibility = View.GONE

            // Esconder los gastos totales
            binding.textViewGastosTotales.visibility = View.GONE

            // Mostrar la gráfica de barras
            binding.barChart.visibility = View.VISIBLE
        }
    }

    private fun setupBarChart() {
        val barChart: BarChart = binding.barChart

        // Definir los datos de cada categoría
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0f, 700f))  // Fiesta
        entries.add(BarEntry(1f, 2400f)) // Comida
        entries.add(BarEntry(2f, 500f))  // Transporte
        entries.add(BarEntry(3f, 1200f)) // Alojamientos

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            return
        }

        // Crear los datasets para las barras
        val fiestaDataSet = BarDataSet(entries.subList(0, 1), "Fiesta")
        fiestaDataSet.color = Color.parseColor("#4CAF50")  // Verde Claro

        val comidaDataSet = BarDataSet(entries.subList(1, 2), "Comida")
        comidaDataSet.color = Color.parseColor("#2196F3")  // Azul

        val transporteDataSet = BarDataSet(entries.subList(2, 3), "Transporte")
        transporteDataSet.color = Color.parseColor("#FFEB3B")  // Amarillo

        val alojamientosDataSet = BarDataSet(entries.subList(3, 4), "Alojamientos")
        alojamientosDataSet.color = Color.parseColor("#FF9800")  // Naranja

        // Crear los datos de la gráfica
        val barData = BarData(fiestaDataSet, comidaDataSet, transporteDataSet, alojamientosDataSet)
        barData.barWidth = 0.4f
        barChart.data = barData

        // Etiquetas de las categorías
        val labels = listOf("Fiesta", "Comida", "Transporte", "Alojamientos")
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.granularity = 1f
        barChart.axisLeft.granularity = 1f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

