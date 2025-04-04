package org.itson.tripsplit.data.model

data class Group(
    val id: String = "",
    val name: String = "",
    var members: Map<String, Boolean> = mapOf(),
    val expenses: Map<String, Expense> = mapOf()
) {

}
