package com.skriv.app.model

import android.net.Uri

data class DocumentState(
    val uri: Uri?,
    val displayName: String,
    val isSaved: Boolean,
    val isReadOnly: Boolean,
    val isLoading: Boolean,
    val hadBom: Boolean = false
)
