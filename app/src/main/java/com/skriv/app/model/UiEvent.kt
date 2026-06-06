package com.skriv.app.model

sealed interface UiEvent {
    data class Snackbar(val message: String, val actionLabel: String? = null) : UiEvent
    data class ErrorDialog(val title: String, val message: String) : UiEvent
    data object NavigateBack : UiEvent
}
