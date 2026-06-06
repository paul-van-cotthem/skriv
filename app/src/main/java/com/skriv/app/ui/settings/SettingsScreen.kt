package com.skriv.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color
import com.skriv.app.util.SkrivIcons
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory)
) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
    val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()

    val scaffoldBgColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = scaffoldBgColor,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = SkrivIcons.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scaffoldBgColor
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TYPOGRAPHY SECTION ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Typography",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Monospace font: ${if (userPrefs.fontMonospace) "on" else "off"}") },
                        supportingContent = {
                            Text(
                                if (userPrefs.fontMonospace) {
                                    "Monospace font is active."
                                } else {
                                    "Sans-serif proportional font is active."
                                }
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = userPrefs.fontMonospace,
                                onCheckedChange = { viewModel.setFontMonospace(it) },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Font size: ${userPrefs.fontSizeSp}") },
                        supportingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text("A", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                                SettingsSlider(
                                    value = userPrefs.fontSizeSp.toFloat().coerceIn(6f, 36f),
                                    onValueChange = { viewModel.setFontSizeSp(it.roundToInt()) },
                                    valueRange = 6f..36f,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("A", style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp))
                            }
                        }
                    )
                    val spacings = listOf("compact" to "Compact", "normal" to "Normal", "relaxed" to "Relaxed", "double" to "Double")
                    val foundIndex = spacings.indexOfFirst { it.first == userPrefs.lineSpacing }
                    val currentSpacingIndex = if (foundIndex == -1) 1 else foundIndex
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Line spacing: ${spacings[currentSpacingIndex].second.lowercase()}") },
                        supportingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text("Compact", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                                SettingsSlider(
                                    value = currentSpacingIndex.toFloat(),
                                    onValueChange = { viewModel.setLineSpacing(spacings[it.roundToInt()].first) },
                                    valueRange = 0f..3f,
                                    steps = 2,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("Double", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                            }
                        }
                    )
                    val margins = listOf(16, 24, 48, 64)
                    val marginLabels = listOf("Narrow", "Normal", "Relaxed", "Wide")
                    val currentMarginIndex = when {
                        userPrefs.readingMarginDp <= 16 -> 0
                        userPrefs.readingMarginDp <= 36 -> 1
                        userPrefs.readingMarginDp <= 56 -> 2
                        else -> 3
                    }
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Margin: ${marginLabels[currentMarginIndex].lowercase()}") },
                        supportingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text("Narrow", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                                SettingsSlider(
                                    value = currentMarginIndex.toFloat(),
                                    onValueChange = { viewModel.setReadingMarginDp(margins[it.roundToInt()]) },
                                    valueRange = 0f..3f,
                                    steps = 2,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("Wide", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                            }
                        }
                    )
                }
            }

            item { HorizontalDivider() }

            // --- EDITOR SECTION ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Editor options",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Word wrap: ${if (userPrefs.wordWrap) "on" else "off"}") },
                        supportingContent = {
                            Text(
                                if (userPrefs.wordWrap) {
                                    "Long lines wrap to fit the screen."
                                } else {
                                    "Long lines extend horizontally."
                                }
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = userPrefs.wordWrap,
                                onCheckedChange = { viewModel.setWordWrap(it) },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Line numbers: ${if (userPrefs.lineNumbers) "on" else "off"}") },
                        trailingContent = {
                            Switch(
                                checked = userPrefs.lineNumbers,
                                onCheckedChange = { viewModel.setLineNumbers(it) },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Word count: ${if (userPrefs.wordCountVisible) "on" else "off"}") },
                        trailingContent = {
                            Switch(
                                checked = userPrefs.wordCountVisible,
                                onCheckedChange = { viewModel.setWordCountVisible(it) },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Auto-save in background: ${if (userPrefs.autoSaveOnBackground) "on" else "off"}") },
                        supportingContent = {
                            Text(
                                if (userPrefs.autoSaveOnBackground) {
                                    "Changes are saved automatically when the app goes to the background."
                                } else {
                                    "Changes are not saved automatically when the app goes to the background."
                                }
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = userPrefs.autoSaveOnBackground,
                                onCheckedChange = { viewModel.setAutoSaveOnBackground(it) },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    )
                }
            }

            item { HorizontalDivider() }

            // --- APPEARANCE SECTION ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Theme") },
                        supportingContent = {
                            val modes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                modes.forEachIndexed { index, pair ->
                                    SegmentedButton(
                                        selected = userPrefs.darkMode == pair.first,
                                        onClick = { viewModel.setDarkMode(pair.first) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                                        colors = SegmentedButtonDefaults.colors()
                                    ) {
                                        Text(pair.second)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            item { HorizontalDivider() }

            // --- FILES SECTION ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Files and storage",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Default extension") },
                        supportingContent = {
                            val extensions = listOf("txt" to ".txt", "md" to ".md")
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                extensions.forEachIndexed { index, pair ->
                                    SegmentedButton(
                                        selected = userPrefs.defaultExtension == pair.first,
                                        onClick = { viewModel.setDefaultExtension(pair.first) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = extensions.size),
                                        colors = SegmentedButtonDefaults.colors()
                                    ) {
                                        Text(pair.second)
                                    }
                                }
                            }
                        }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Open last edited file on startup: ${if (userPrefs.openLastFileOnStartup) "on" else "off"}") },
                        trailingContent = {
                            Switch(
                                checked = userPrefs.openLastFileOnStartup,
                                onCheckedChange = { viewModel.setOpenLastFileOnStartup(it) },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    )
                }
            }

            item { HorizontalDivider() }

            // --- ABOUT SECTION ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Skriv") },
                        trailingContent = { Text("v$versionName") }
                    )
                    val uriHandler = LocalUriHandler.current
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("User manual") },
                        supportingContent = { Text("Learn how to use Skriv, troubleshoot issues, and access advanced features.") },
                        trailingContent = {
                            Icon(
                                imageVector = SkrivIcons.ChevronRight,
                                contentDescription = "Open manual link",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://paul-van-cotthem.github.io/skriv/")
                        }
                    )
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Privacy policy") },
                        supportingContent = { Text("Read our commitment to zero data collection and absolute privacy.") },
                        trailingContent = {
                            Icon(
                                imageVector = SkrivIcons.ChevronRight,
                                contentDescription = "Open privacy link",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://paul-van-cotthem.github.io/skriv/privacy.html")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    val activeFraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier,
        colors = SliderDefaults.colors(
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = activeFraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    )
}
