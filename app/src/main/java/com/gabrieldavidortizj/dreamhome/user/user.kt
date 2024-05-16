package com.gabrieldavidortizj.dreamhome.user

data class user (
    val address: String = "",
    val nombre: String = "",
    val phone: String = "",
    val provider: String = "",
    val tipo: String = "",
    val favoriteProperties: MutableList<String> = mutableListOf()
)