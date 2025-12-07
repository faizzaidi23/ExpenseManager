package com.example.expensecalculator.util

enum class CurrencyCode(val code: String, val symbol: String, val displayName: String) {
    INR("INR", "₹", "Indian Rupee"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    JPY("JPY", "¥", "Japanese Yen"),
    AUD("AUD", "A$", "Australian Dollar"),
    CAD("CAD", "C$", "Canadian Dollar"),
    CHF("CHF", "Fr", "Swiss Franc"),
    CNY("CNY", "¥", "Chinese Yuan"),
    AED("AED", "د.إ", "UAE Dirham");

    override fun toString(): String = "$symbol $displayName ($code)"

    companion object {
        fun fromCode(code: String): CurrencyCode {
            return values().find { it.code == code } ?: INR
        }
    }
}

