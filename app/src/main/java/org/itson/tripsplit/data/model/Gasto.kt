package org.itson.tripsplit.data.model

data class Gasto(val id: String ="",
    val descripcion: String ="",
    val monto: Double,
    val categoria: String,
    val pagadoPor: String,
    val participantes: List<String>,
    val fecha: Long) {
}