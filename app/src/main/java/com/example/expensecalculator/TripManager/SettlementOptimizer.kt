package com.example.expensecalculator.TripManager

import kotlin.math.abs

/**
 * Data class representing a settlement transaction
 * @param from Person who needs to pay
 * @param to Person who will receive money
 * @param amount Amount to be transferred
 */
data class Settlement(
    val from: String,
    val to: String,
    val amount: Double
)

/**
 * Smart Settlement Optimizer
 * Minimizes the number of transactions needed to settle all debts
 */
object SettlementOptimizer {

    /**
     * Calculate optimized settlements from balances
     * Uses greedy algorithm to minimize number of transactions
     *
     * @param balances Map of participant name to their balance
     *                 Positive = owed money, Negative = owes money
     * @return List of Settlement transactions
     */
    fun calculateOptimizedSettlements(balances: Map<String, Double>): List<Settlement> {
        val settlements = mutableListOf<Settlement>()

        // Filter out near-zero balances (within 0.01 tolerance)
        val filteredBalances = balances.filter { abs(it.value) >= 0.01 }

        if (filteredBalances.isEmpty()) {
            return emptyList()
        }

        // Separate debtors (who owe money) and creditors (who are owed money)
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
