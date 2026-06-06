package com.skriv.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import com.skriv.app.util.SkrivIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FindBar(
    state: FindBarState,
    onQueryChanged: (String) -> Unit,
    onReplacementChanged: (String) -> Unit,
    onCaseSensitiveToggled: () -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplace: (String) -> Unit,
    onReplaceAll: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = MaterialTheme.colorScheme.surfaceContainer
    val containerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Surface(
        color = containerBg,
        shadowElevation = 8.dp,
        shape = containerShape,
        modifier = modifier.fillMaxWidth()
    ) {
        var isReplaceExpanded by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Row 1: Find Field & Navigation Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Toggle Replace arrow button on left
                IconButton(
                    onClick = { isReplaceExpanded = !isReplaceExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isReplaceExpanded) SkrivIcons.KeyboardArrowUp else SkrivIcons.ChevronRight,
                        contentDescription = if (isReplaceExpanded) "Hide replace field" else "Show replace field",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Find input field
                CompactOutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    placeholder = { Text("Find...", style = MaterialTheme.typography.bodyMedium) },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            // Case-sensitive "Aa" toggle
                            IconButton(
                                onClick = onCaseSensitiveToggled,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text(
                                    text = "Aa",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = if (state.caseSensitive) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))

                            if (state.query.isNotEmpty()) {
                                val matchText = if (state.matchCount > 0) {
                                    "${state.currentMatchIndex + 1}/${state.matchCount}"
                                } else {
                                    "0"
                                }
                                Text(
                                    text = matchText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Navigation controls container (fixed width to match Replace row actions)
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(116.dp)
                ) {
                    // Up arrow (Previous match)
                    IconButton(
                        onClick = onFindPrevious,
                        enabled = state.matchCount > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.ArrowUpward,
                            contentDescription = "Previous match",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Down arrow (Next match)
                    IconButton(
                        onClick = onFindNext,
                        enabled = state.matchCount > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.ArrowDownward,
                            contentDescription = "Next match",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Close button (✕)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = SkrivIcons.Close,
                            contentDescription = "Close search",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Row 2 (Replace options, only when expanded)
            if (isReplaceExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Spacer matching width of the leftmost toggle button to align fields
                    Spacer(modifier = Modifier.width(36.dp))

                    // Replace input field
                    CompactOutlinedTextField(
                        value = state.replacement,
                        onValueChange = onReplacementChanged,
                        placeholder = { Text("Replace...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f)
                    )

                    // Action buttons container (fixed width to match Row 1 navigation controls)
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(116.dp)
                    ) {
                        // Replace button (aligned under Prev & Next buttons)
                        TextButton(
                            onClick = { onReplace(state.replacement) },
                            enabled = state.matchCount > 0,
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.width(76.dp)
                        ) {
                            Text(
                                text = "Replace",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // All button (aligned under Close button)
                        TextButton(
                            onClick = { onReplaceAll(state.replacement) },
                            enabled = state.matchCount > 0,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.width(36.dp)
                        ) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(8.dp)
    val textFieldColors = OutlinedTextFieldDefaults.colors()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        interactionSource = interactionSource,
        modifier = modifier.height(48.dp),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = placeholder,
                trailingIcon = trailingIcon,
                colors = textFieldColors,
                contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = textFieldColors,
                        shape = shape
                    )
                }
            )
        }
    )
}

