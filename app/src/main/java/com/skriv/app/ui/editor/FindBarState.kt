package com.skriv.app.ui.editor

data class FindBarState(
    val isVisible: Boolean = false,
    val query: String = "",
    val replacement: String = "",
    val matchRanges: List<IntRange> = emptyList(),
    val currentMatchIndex: Int = 0,
    val caseSensitive: Boolean = false
) {
    val matchCount: Int get() = matchRanges.size
}
