package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

enum class NoteType {
    TEXT,
    CHECKLIST,
    SKETCH,
    AUDIO
}

data class ChecklistItem(
    val id: String,
    val text: String,
    val isDone: Boolean = false
)

data class SketchPoint(
    val x: Float,
    val y: Float
)

data class SketchStroke(
    val points: List<SketchPoint>,
    val colorHex: Long,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

enum class NoteFontStyle(val displayName: String, val fontFamily: FontFamily) {
    SANS("Sans", FontFamily.SansSerif),
    SERIF("Serif", FontFamily.Serif),
    MONO("Mono", FontFamily.Monospace),
    CURSIVE("Casual", FontFamily.Cursive)
}

data class NoteColorTheme(
    val id: Int,
    val name: String,
    val lightBg: Color,
    val darkBg: Color,
    val cardBorderLight: Color,
    val cardBorderDark: Color,
    val textColorLight: Color,
    val textColorDark: Color,
    val accentColor: Color
)

object NoteColorThemes {
    val presets = listOf(
        NoteColorTheme(
            id = 0,
            name = "Periwinkle",
            lightBg = Color(0xFFD6E3FF),
            darkBg = Color(0xFF1E2D4A),
            cardBorderLight = Color(0xFFB9CEF8),
            cardBorderDark = Color(0xFF2E4166),
            textColorLight = Color(0xFF001B3E),
            textColorDark = Color(0xFFD6E3FF),
            accentColor = Color(0xFF005AC1)
        ),
        NoteColorTheme(
            id = 1,
            name = "Lavender",
            lightBg = Color(0xFFF3E8FF),
            darkBg = Color(0xFF331F4A),
            cardBorderLight = Color(0xFFE4CBFF),
            cardBorderDark = Color(0xFF4A316B),
            textColorLight = Color(0xFF21005D),
            textColorDark = Color(0xFFF3E8FF),
            accentColor = Color(0xFF7E22CE)
        ),
        NoteColorTheme(
            id = 2,
            name = "Sage",
            lightBg = Color(0xFFE8F3EF),
            darkBg = Color(0xFF183329),
            cardBorderLight = Color(0xFFC7E2D8),
            cardBorderDark = Color(0xFF264C3E),
            textColorLight = Color(0xFF00201A),
            textColorDark = Color(0xFFE8F3EF),
            accentColor = Color(0xFF0D9488)
        ),
        NoteColorTheme(
            id = 3,
            name = "Warm Peach",
            lightBg = Color(0xFFFFF0E1),
            darkBg = Color(0xFF382315),
            cardBorderLight = Color(0xFFFED7AA),
            cardBorderDark = Color(0xFF553621),
            textColorLight = Color(0xFF2E1500),
            textColorDark = Color(0xFFFFF0E1),
            accentColor = Color(0xFFEA580C)
        ),
        NoteColorTheme(
            id = 4,
            name = "Butter Lemon",
            lightBg = Color(0xFFFEF9C3),
            darkBg = Color(0xFF353013),
            cardBorderLight = Color(0xFFFDE047),
            cardBorderDark = Color(0xFF544A1B),
            textColorLight = Color(0xFF373215),
            textColorDark = Color(0xFFFEF9C3),
            accentColor = Color(0xFFCA8A04)
        ),
        NoteColorTheme(
            id = 5,
            name = "Rose Blush",
            lightBg = Color(0xFFFCE7F3),
            darkBg = Color(0xFF3B1929),
            cardBorderLight = Color(0xFFFBCFE8),
            cardBorderDark = Color(0xFF59263E),
            textColorLight = Color(0xFF500724),
            textColorDark = Color(0xFFFCE7F3),
            accentColor = Color(0xFFDB2777)
        ),
        NoteColorTheme(
            id = 6,
            name = "Sky Blue",
            lightBg = Color(0xFFE0F2FE),
            darkBg = Color(0xFF132D42),
            cardBorderLight = Color(0xFFBAE6FD),
            cardBorderDark = Color(0xFF1E4666),
            textColorLight = Color(0xFF082F49),
            textColorDark = Color(0xFFE0F2FE),
            accentColor = Color(0xFF0284C7)
        ),
        NoteColorTheme(
            id = 7,
            name = "Minimal Pearl",
            lightBg = Color(0xFFFFFFFF),
            darkBg = Color(0xFF191C20),
            cardBorderLight = Color(0xFFE0E2EC),
            cardBorderDark = Color(0xFF2D3136),
            textColorLight = Color(0xFF1A1C1E),
            textColorDark = Color(0xFFE2E2E9),
            accentColor = Color(0xFF44474E)
        )
    )

    fun getById(id: Int): NoteColorTheme {
        return presets.getOrNull(id) ?: presets[0]
    }
}

enum class ViewMode {
    GRID,
    LIST
}

enum class ScreenDestination {
    HOME,
    EDITOR,
    ARCHIVE,
    TRASH,
    SETTINGS
}
