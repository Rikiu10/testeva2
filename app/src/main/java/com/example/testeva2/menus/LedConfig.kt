package com.example.testeva2.menus


data class LedConfig(
    var warning_level: Int = 50,
    var critical_level: Int = 100
) {
    constructor() : this(50, 100)
}