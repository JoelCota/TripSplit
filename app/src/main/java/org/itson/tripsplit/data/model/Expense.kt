package org.itson.tripsplit.data.model

data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val paidBy: String = "",
    val divididoEntre: Map<String, Double> = mapOf()
)