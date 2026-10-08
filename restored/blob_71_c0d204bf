package com.example.model

enum class TileCategory(val title: String) {
    CONNECTIVITY("Connectivity"),
    HARDWARE("Hardware & Device"),
    AUDIO_DISPLAY("Sound & Screen"),
    SYSTEM("System & Modes"),
    EXTERNAL("Third-Party Tiles"),
    THIRD_PARTY("Third-Party Tiles");

    companion object {
        fun fromString(name: String?): TileCategory {
            if (name == null) return SYSTEM
            return try {
                valueOf(name.uppercase())
            } catch (e: Exception) {
                if (name.contains("third", ignoreCase = true) || name.contains("external", ignoreCase = true)) {
                    THIRD_PARTY
                } else {
                    SYSTEM
                }
            }
        }
    }
}
