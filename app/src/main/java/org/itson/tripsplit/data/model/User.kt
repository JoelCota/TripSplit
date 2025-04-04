package org.itson.tripsplit.data.model

data class User(
    val uid: String = "",
    val email: String?,
    val name: String?,
    val groups: Map<String, Boolean> = mapOf(),
    val expenses: Map<String, Expense> = mapOf()
) {
    constructor() : this("", "", "", emptyMap(),emptyMap())
}
