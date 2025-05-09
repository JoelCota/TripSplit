package org.itson.tripsplit.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.log10
import kotlin.math.pow

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
        color = Color.parseColor("#333333")
        textSize = 30f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
    }

    private val barColors = listOf(
        Color.parseColor("#673AB7"),
        Color.parseColor("#9575CD"),
        Color.parseColor("#D1C4E9"),
        Color.parseColor("#B39DDB"),
        Color.parseColor("#7E57C2"),
        Color.parseColor("#512DA8")
    )

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
        val barWidth = width / (data.size * 2.5f)
        val maxBarHeight = height - 140f
        val baselineY = height - 60f

        val maxValue = data.maxOrNull() ?: 1f

        // Línea base
        canvas.drawLine(0f, baselineY, width, baselineY, axisPaint)

        data.forEachIndexed { index, value ->
            val scaledHeight = (value / maxValue) * maxBarHeight
            val left = barWidth + index * barWidth * 2.5f
            val top = baselineY - scaledHeight
            val right = left + barWidth
            val bottom = baselineY

            // Color de la barra
            barPaint.color = barColors[index % barColors.size]

            // Dibujar barra
            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, 20f, 20f, barPaint)

            // Dibujar valor encima (con formateo si es grande)
            val valueFormatted = formatLargeNumber(value)
            val valueY = if (top - 20f < 30f) 30f else top - 15f // evitar que suba demasiado
            canvas.drawText(valueFormatted, left + barWidth / 2, valueY, valuePaint)

            // Etiqueta abajo
            val label = labels.getOrNull(index) ?: "Item ${index + 1}"
            canvas.drawText(label, left + barWidth / 2, height - 20f, labelPaint)
        }
    }

    private fun formatLargeNumber(value: Float): String {
        return when {
            value >= 1_000_000 -> "%.1fM".format(value / 1_000_000)
            value >= 1_000 -> "%.1fK".format(value / 1_000)
            else -> "%.0f".format(value)
        }
    }
}
