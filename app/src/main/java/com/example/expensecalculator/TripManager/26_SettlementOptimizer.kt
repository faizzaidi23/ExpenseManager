package com.example.expensecalculator.TripManager

import kotlin.math.abs

data class Settlement(
    val from: String,
    val to: String,
    val amount: Double
)


object SettlementOptimizer {

    fun calculateOptimizedSettlements(balances: Map<String, Double>): List<Settlement> {
        val settlements = mutableListOf<Settlement>()


        val filteredBalances = balances.filter { abs(it.value) >= 0.01 }

        if (filteredBalances.isEmpty()) {
            return emptyList()
        }

        val debtors = filteredBalances.filter { it.value < 0 }
            .map { it.key to abs(it.value) }
            .toMutableList()

        val creditors = filteredBalances.filter { it.value > 0 }
            .map { it.key to it.value }
            .toMutableList()

        // Sort both lists in descending order for optimization
        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        var debtorIndex = 0
        var creditorIndex = 0

        // Greedy algorithm: Match largest debtor with largest creditor
        while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
            val (debtorName, debtAmount) = debtors[debtorIndex]
            val (creditorName, creditAmount) = creditors[creditorIndex]

            // Settle the minimum of what's owed and what's due
            val settlementAmount = minOf(debtAmount, creditAmount)

            // Add this settlement transaction
            settlements.add(
                Settlement(
                    from = debtorName,
                    to = creditorName,
                    amount = settlementAmount
                )
            )

            // Update remaining amounts
            val remainingDebt = debtAmount - settlementAmount
            val remainingCredit = creditAmount - settlementAmount

            // Move to next debtor or creditor based on who's settled
            if (remainingDebt < 0.01) {
                debtorIndex++
            } else {
                debtors[debtorIndex] = debtorName to remainingDebt
            }

            if (remainingCredit < 0.01) {
                creditorIndex++
            } else {
                creditors[creditorIndex] = creditorName to remainingCredit
            }
        }

        return settlements
    }
}
