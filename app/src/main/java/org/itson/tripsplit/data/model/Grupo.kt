package org.itson.tripsplit.data.model

data class Grupo(
    var id: String = "",
    var nombre: String = "",
    var idCreador: String = "",
    var usuarios: List<Usuario> = emptyList(),
    var gastos: List<Gasto> = emptyList()
)
{

}

