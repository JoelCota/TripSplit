package org.itson.tripsplit.data.model
data class Group(
    val id: String = "",  // ID generado automáticamente
    val nombre: String,   // Nombre del grupo
    val gastos: String,   // Gastos del grupo
    val miembros: Map<String, Boolean> = emptyMap() // Se guarda como un mapa en Firebase
)