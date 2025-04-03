package org.itson.tripsplit.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CustomBarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: List<Float> = emptyList()
    private var labels: List<String> = emptyList()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 4f
    }

    private val colors = listOf(
        Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW
    )

    // Ahora acepta etiquetas
    fun setData(values: List<Float>, etiquetas: List<String>? = null) {
        data = values
        labels = etiquetas ?: List(values.size) { "Item ${it + 1}" }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()

        val barWidth = width / (data.size * 2)
        val maxBarHeight = height - 100f

        canvas.drawLine(0f, height - 60f, width, height - 60f, axisPaint)

        data.forEachIndexed { index, value ->
            val left = barWidth + index * barWidth * 2
            val barHeight = (value / 100f) * maxBarHeight
            val top = height - 60f - barHeight
            val right = left + barWidth
            val bottom = height - 60f

            // Color para cada barra
            barPaint.color = colors[index % colors.size]
            canvas.drawRect(left, top, right, bottom, barPaint)

            // Valor que se ve encima de cada barra
            canvas.drawText("${value.toInt()}", left + barWidth / 2, top - 10f, valuePaint)

            // Etiqueta debajo de cada barra
            val label = labels.getOrNull(index) ?: "Item ${index + 1}"
            canvas.drawText(label, left + barWidth / 2, height - 20f, labelPaint)
        }
    }
}
