package com.example.testeva2.menus

data class HistoryRecord(
    var gas_level: Int = 0,
    var timestamp: Long = 0,
    var is_critical: Boolean = false,
    var raw_data_key: String = ""
) {
    // Constructor vacío OBLIGATORIO para Firebase
    constructor() : this(0, 0, false, "")
}