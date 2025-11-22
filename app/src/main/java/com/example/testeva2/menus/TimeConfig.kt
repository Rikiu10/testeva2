package com.example.testeva2.menus

data class TimeConfig(
    var enabled: Boolean = false,
    var start_hour: Int = 8,
    var end_hour: Int = 20,
    var start_minute: Int = 0,
    var end_minute: Int = 0
) {
    constructor() : this(false, 8, 20, 0, 0)
}