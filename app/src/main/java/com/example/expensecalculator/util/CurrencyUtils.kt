package com.example.expensecalculator.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    /**
     * Convert amount from one currency to another using exchange rate
     */
    fun convert(amount: Double, rate: Double, scale: Int = 2): Double {
        return BigDecimal.valueOf(amount)
            .multiply(BigDecimal.valueOf(rate))
            .setScale(scale, RoundingMode.HALF_UP)
            .toDouble()
    }

    /**
     * Format amount with currency symbol
     */
    fun format(amount: Double, currencyCode: String): String {
        val currency = CurrencyCode.fromCode(currencyCode)
        return "${currency.symbol}${"%.2f".format(amount)}"
    }

    /**
     * Format amount with locale-specific formatting
     */
    fun formatWithLocale(amount: Double, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val nf = NumberFormat.getCurrencyInstance(locale)
        try {
            nf.currency = Currency.getInstance(currencyCode)
        } catch (_: Exception) {
            // Fallback to manual formatting
            return format(amount, currencyCode)
        }
        return nf.format(amount)
    }

    /**
     * Get symbol for currency code
     */
    fun getSymbol(currencyCode: String): String {
        return CurrencyCode.fromCode(currencyCode).symbol
    }
}

