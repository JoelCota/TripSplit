package org.itson.tripsplit.utils

class CurrencyUtils {

    // Tasas de cambio estimadas (puedes actualizarlas según tus necesidades)
    private val exchangeRatesToUsd = mapOf(
        "MXN" to 0.059, // Peso mexicano a dólar
        "EUR" to 1.08,  // Euro a dólar
        "USD" to 1.0,   // Dólar a dólar
        "BRL" to 0.20   // Real brasileño a dólar
    )

    /**
     * Convierte una cantidad desde la moneda indicada a dólares (USD).
     *
     * @param amount Cantidad a convertir.
     * @param currency Moneda original (ej. "MXN", "EUR", etc.)
     * @return Monto equivalente en USD.
     * @throws IllegalArgumentException si la moneda no está soportada.
     */
    fun toUsd(amount: Double, currency: String): Double {
        val rate = exchangeRatesToUsd[currency.uppercase()]
            ?: throw IllegalArgumentException("Moneda no soportada: $currency")
        return amount * rate
    }
    fun toUsdWithTwoDecimals(amount: Double, currency: String): Double {
        val converted = toUsd(amount, currency)
        return "%.2f".format(converted).toDouble()
    }
}