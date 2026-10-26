package com.example.enhancedsplitwise

data class Expense(
    val title: String,
    val amount: Double,
    val group: String,
    val paidBy: String,
    val splitMethod: String
)

object ExpenseStore {
    val expenses = mutableListOf<Expense>()
}
