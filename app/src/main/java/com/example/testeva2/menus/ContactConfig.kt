package com.example.testeva2.menus

data class ContactConfig(
    var contact_name_1: String = "",
    var contact_number_1: String = "",
    var contact_name_2: String = "",
    var contact_number_2: String = "",
    var call_enabled: Boolean = true
) {
    // Constructor vacío OBLIGATORIO para Firebase
    constructor() : this("", "", "", "", true)
}