package com.skriv.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SkrivIcons {
    val ArrowBack: ImageVector = ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(20f, 11f)
        horizontalLineTo(7.83f)
        lineTo(13.41f, 5.41f)
        lineTo(12f, 4f)
        lineTo(4f, 12f)
        lineTo(12f, 20f)
        lineTo(13.41f, 18.59f)
        lineTo(7.83f, 13f)
        horizontalLineTo(20f)
        close()
    }.build()

    val Check: ImageVector = ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(9f, 16.17f)
        lineTo(4.83f, 12f)
        lineTo(3.41f, 13.41f)
        lineTo(9f, 19f)
        lineTo(21f, 7f)
        lineTo(19.59f, 5.59f)
        close()
    }.build()

    val MoreVert: ImageVector = ImageVector.Builder(
        name = "MoreVert",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 8f)
        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
        reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
        reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
        reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
        close()
        moveTo(12f, 10f)
        curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
        reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
        reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
        reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
        close()
        moveTo(12f, 16f)
        curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
        reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
        reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
        reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
        close()
    }.build()

    val Close: ImageVector = ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(19f, 6.41f)
        lineTo(17.59f, 5f)
        lineTo(12f, 10.59f)
        lineTo(6.41f, 5f)
        lineTo(5f, 6.41f)
        lineTo(10.59f, 12f)
        lineTo(5f, 17.59f)
        lineTo(6.41f, 19f)
        lineTo(12f, 13.41f)
        lineTo(17.59f, 19f)
        lineTo(19f, 17.59f)
        lineTo(13.41f, 12f)
        close()
    }.build()

    val KeyboardArrowUp: ImageVector = ImageVector.Builder(
        name = "KeyboardArrowUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(7.41f, 15.41f)
        lineTo(12f, 10.83f)
        lineTo(16.59f, 15.41f)
        lineTo(18f, 14f)
        lineTo(12f, 8f)
        lineTo(6f, 14f)
        close()
    }.build()

    val KeyboardArrowDown: ImageVector = ImageVector.Builder(
        name = "KeyboardArrowDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(7.41f, 8.59f)
        lineTo(12f, 13.17f)
        lineTo(16.59f, 8.59f)
        lineTo(18f, 10f)
        lineTo(12f, 16f)
        lineTo(6f, 10f)
        close()
    }.build()

    val Delete: ImageVector = ImageVector.Builder(
        name = "Delete",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(6f, 19f)
        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
        horizontalLineToRelative(8f)
        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
        verticalLineTo(7f)
        horizontalLineTo(6f)
        verticalLineToRelative(12f)
        close()
        moveTo(19f, 4f)
        horizontalLineToRelative(-3.5f)
        lineTo(14.5f, 3f)
        horizontalLineToRelative(-5f)
        lineTo(8.5f, 4f)
        horizontalLineTo(5f)
        verticalLineToRelative(2f)
        horizontalLineToRelative(14f)
        verticalLineTo(4f)
        close()
    }.build()

    val Warning: ImageVector = ImageVector.Builder(
        name = "Warning",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(1f, 21f)
        horizontalLineToRelative(22f)
        lineTo(12f, 2f)
        lineTo(1f, 21f)
        close()
        moveTo(12f, 18f)
        curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
        reflectiveCurveToRelative(0.45f, -1f, 1f, -1f)
        reflectiveCurveToRelative(1f, 0.45f, 1f, 1f)
        reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f)
        close()
        moveTo(12f, 14f)
        curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
        verticalLineToRelative(-4f)
        curveToRelative(0f, -0.55f, 0.9f, -1f, 1f, -1f)
        reflectiveCurveToRelative(1f, 0.45f, 1f, 1f)
        verticalLineToRelative(4f)
        curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
        close()
    }.build()

    val Undo: ImageVector = ImageVector.Builder(
        name = "Undo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(12.5f, 8f)
        curveTo(9.85f, 8f, 7.45f, 9f, 5.6f, 10.6f)
        lineTo(2f, 7f)
        verticalLineTo(16f)
        horizontalLineTo(11f)
        lineTo(7.49f, 12.49f)
        curveTo(8.87f, 11.23f, 10.58f, 10.5f, 12.5f, 10.5f)
        curveTo(16.5f, 10.5f, 19.82f, 13.06f, 21.05f, 16.75f)
        lineTo(23.23f, 16f)
        curveTo(21.65f, 11.3f, 17.47f, 8f, 12.5f, 8f)
        close()
    }.build()

    val Redo: ImageVector = ImageVector.Builder(
        name = "Redo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(18.4f, 10.6f)
        curveTo(16.55f, 9f, 14.15f, 8f, 11.5f, 8f)
        curveTo(6.53f, 8f, 2.35f, 11.3f, 0.77f, 16f)
        lineTo(2.95f, 16.75f)
        curveTo(4.18f, 13.06f, 7.5f, 10.5f, 11.5f, 10.5f)
        curveTo(13.42f, 10.5f, 15.13f, 11.23f, 16.51f, 12.49f)
        lineTo(13f, 16f)
        horizontalLineTo(22f)
        verticalLineTo(7f)
        lineTo(18.4f, 10.6f)
        close()
    }.build()

    val Search: ImageVector = ImageVector.Builder(
        name = "Search",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(15.5f, 14f)
        horizontalLineTo(14.71f)
        lineTo(14.43f, 13.73f)
        curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
        curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
        reflectiveCurveTo(3f, 5.91f, 3f, 9.5f)
        reflectiveCurveTo(5.91f, 16f, 9.5f, 16f)
        curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
        lineTo(14f, 14.71f)
        verticalLineTo(15.5f)
        lineTo(19f, 20.49f)
        lineTo(20.49f, 19f)
        lineTo(15.5f, 14f)
        close()
        moveTo(9.5f, 14f)
        curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
        reflectiveCurveTo(7.01f, 5f, 9.5f, 5f)
        reflectiveCurveTo(14f, 7.01f, 14f, 9.5f)
        reflectiveCurveTo(11.99f, 14f, 9.5f, 14f)
        close()
    }.build()

    val Add: ImageVector = ImageVector.Builder(
        name = "Add",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(19f, 13f)
        horizontalLineTo(13f)
        verticalLineTo(19f)
        horizontalLineTo(11f)
        verticalLineTo(13f)
        horizontalLineTo(5f)
        verticalLineTo(11f)
        horizontalLineTo(11f)
        verticalLineTo(5f)
        horizontalLineTo(13f)
        verticalLineTo(11f)
        horizontalLineTo(19f)
        verticalLineTo(13f)
        close()
    }.build()

    val FolderOpen: ImageVector = ImageVector.Builder(
        name = "FolderOpen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(20f, 6f)
        horizontalLineTo(12f)
        lineTo(10f, 4f)
        horizontalLineTo(4f)
        curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
        lineTo(2f, 18f)
        curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
        horizontalLineTo(20f)
        curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
        verticalLineTo(8f)
        curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f)
        close()
        moveTo(20f, 18f)
        horizontalLineTo(4f)
        verticalLineTo(8f)
        horizontalLineTo(20f)
        verticalLineTo(18f)
        close()
    }.build()

    val Document: ImageVector = ImageVector.Builder(
        name = "Document",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(14f, 2f)
        horizontalLineTo(6f)
        curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
        verticalLineTo(20f)
        curveTo(4f, 21.1f, 4.89f, 22f, 6f, 22f)
        horizontalLineTo(18f)
        curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
        verticalLineTo(8f)
        lineTo(14f, 2f)
        close()
        moveTo(16f, 18f)
        horizontalLineTo(8f)
        verticalLineTo(16f)
        horizontalLineTo(16f)
        verticalLineTo(18f)
        close()
        moveTo(16f, 14f)
        horizontalLineTo(8f)
        verticalLineTo(12f)
        horizontalLineTo(16f)
        verticalLineTo(14f)
        close()
        moveTo(13f, 9f)
        verticalLineTo(3.5f)
        lineTo(18.5f, 9f)
        horizontalLineTo(13f)
        close()
    }.build()

    val EditDocument: ImageVector = ImageVector.Builder(
        name = "EditDocument",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(14f, 2f)
        horizontalLineTo(6f)
        curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
        verticalLineTo(20f)
        curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
        horizontalLineTo(18f)
        curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
        verticalLineTo(8f)
        lineTo(14f, 2f)
        close()
        moveTo(13f, 9f)
        verticalLineTo(3.5f)
        lineTo(18.5f, 9f)
        horizontalLineTo(13f)
        close()
        moveTo(16f, 16f)
        horizontalLineTo(8f)
        verticalLineTo(14f)
        horizontalLineTo(16f)
        verticalLineTo(16f)
        close()
        moveTo(12f, 12f)
        horizontalLineTo(8f)
        verticalLineTo(10f)
        horizontalLineTo(12f)
        verticalLineTo(12f)
        close()
    }.build()

    val Share: ImageVector = ImageVector.Builder(
        name = "Share",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(18f, 16.08f)
        curveToRelative(-0.76f, 0f, -1.44f, 0.3f, -1.96f, 0.77f)
        lineTo(8.91f, 12.7f)
        curveToRelative(0.05f, -0.23f, 0.09f, -0.46f, 0.09f, -0.7f)
        reflectiveCurveToRelative(-0.04f, -0.47f, -0.09f, -0.7f)
        lineTo(15.9f, 7.23f)
        curveTo(16.42f, 7.7f, 17.1f, 8f, 17.9f, 8f)
        curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
        reflectiveCurveTo(19.56f, 2f, 17.9f, 2f)
        reflectiveCurveTo(14.9f, 3.34f, 14.9f, 5f)
        curveToRelative(0f, 0.24f, 0.04f, 0.47f, 0.09f, 0.7f)
        lineTo(7.91f, 9.2f)
        curveTo(7.39f, 8.73f, 6.71f, 8.45f, 5.9f, 8.45f)
        curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
        reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
        curveToRelative(0.81f, 0f, 1.49f, -0.27f, 2.01f, -0.74f)
        lineToRelative(7.08f, 4.12f)
        curveToRelative(-0.05f, 0.21f, -0.09f, 0.43f, -0.09f, 0.67f)
        curveToRelative(0f, 1.6f, 1.3f, 2.9f, 2.9f, 2.9f)
        reflectiveCurveToRelative(2.9f, -1.3f, 2.9f, -2.9f)
        reflectiveCurveToRelative(-1.3f, -2.9f, -2.9f, -2.9f)
        close()
    }.build()

    val Print: ImageVector = ImageVector.Builder(
        name = "Print",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(19f, 8f)
        horizontalLineTo(5f)
        curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
        verticalLineToRelative(6f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(4f)
        horizontalLineToRelative(12f)
        verticalLineToRelative(-4f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(-6f)
        curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
        close()
        moveTo(16f, 19f)
        horizontalLineTo(8f)
        verticalLineToRelative(-5f)
        horizontalLineToRelative(8f)
        verticalLineTo(19f)
        close()
        moveTo(19f, 12f)
        curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
        reflectiveCurveToRelative(0.45f, -1f, 1f, -1f)
        reflectiveCurveToRelative(1f, 0.45f, 1f, 1f)
        reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f)
        close()
        moveTo(18f, 3f)
        horizontalLineTo(6f)
        verticalLineToRelative(4f)
        horizontalLineToRelative(12f)
        verticalLineTo(3f)
        close()
    }.build()

    val Settings: ImageVector = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(19.14f, 12.94f)
        curveToRelative(0.04f, -0.3f, 0.06f, -0.61f, 0.06f, -0.94f)
        reflectiveCurveToRelative(-0.02f, -0.64f, -0.06f, -0.94f)
        lineToRelative(2.03f, -1.58f)
        curveToRelative(0.18f, -0.14f, 0.23f, -0.41f, 0.12f, -0.61f)
        lineToRelative(-1.92f, -3.32f)
        curveToRelative(-0.12f, -0.22f, -0.37f, -0.29f, -0.59f, -0.22f)
        lineToRelative(-2.39f, 0.96f)
        curveToRelative(-0.5f, -0.38f, -1.03f, -0.7f, -1.62f, -0.94f)
        lineToRelative(-0.36f, -2.54f)
        curveTo(14.31f, 2.58f, 14.09f, 2f, 13.82f, 2f)
        horizontalLineTo(9.97f)
        curveToRelative(-0.27f, 0f, -0.49f, 0.58f, -0.53f, 0.82f)
        lineTo(9.08f, 5.36f)
        curveToRelative(-0.59f, 0.24f, -1.13f, 0.57f, -1.62f, 0.94f)
        lineToRelative(-2.39f, -0.96f)
        curveToRelative(-0.22f, -0.07f, -0.47f, 0f, -0.59f, 0.22f)
        lineTo(2.17f, 6.88f)
        curveToRelative(-0.11f, 0.2f, -0.06f, 0.47f, 0.12f, 0.61f)
        lineToRelative(2.03f, 1.58f)
        curveToRelative(-0.04f, 0.3f, -0.06f, 0.61f, -0.06f, 0.94f)
        reflectiveCurveToRelative(0.02f, 0.64f, 0.06f, 0.94f)
        lineTo(2.29f, 12.54f)
        curveToRelative(-0.18f, 0.14f, -0.23f, 0.41f, -0.12f, 0.61f)
        lineToRelative(1.92f, 3.32f)
        curveToRelative(0.12f, 0.22f, 0.37f, 0.29f, 0.59f, 0.22f)
        lineToRelative(2.39f, -0.96f)
        curveToRelative(0.5f, 0.38f, 1.03f, 0.7f, 1.62f, 0.94f)
        lineToRelative(0.36f, 2.54f)
        curveToRelative(0.05f, 0.24f, 0.27f, 0.82f, 0.53f, 0.82f)
        horizontalLineToRelative(3.85f)
        curveToRelative(0.27f, 0f, 0.49f, -0.58f, 0.53f, -0.82f)
        lineToRelative(0.36f, -2.54f)
        curveToRelative(0.59f, -0.24f, 1.13f, -0.56f, 1.62f, -0.94f)
        lineToRelative(2.39f, 0.96f)
        curveToRelative(0.22f, 0.07f, 0.47f, 0f, 0.59f, -0.22f)
        lineToRelative(1.92f, -3.32f)
        curveToRelative(0.12f, -0.22f, 0.07f, -0.47f, -0.12f, -0.61f)
        lineToRelative(-2.01f, -1.58f)
        close()
        moveTo(12f, 15.6f)
        curveToRelative(-1.98f, 0f, -3.6f, -1.62f, -3.6f, -3.6f)
        reflectiveCurveToRelative(1.62f, -3.6f, 3.6f, -3.6f)
        reflectiveCurveToRelative(3.6f, 1.62f, 3.6f, 3.6f)
        reflectiveCurveToRelative(-1.62f, 3.6f, -3.6f, 3.6f)
        close()
    }.build()

    val ChevronLeft: ImageVector = ImageVector.Builder(
        name = "ChevronLeft",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(15.41f, 7.41f)
        lineTo(14f, 6f)
        lineTo(8f, 12f)
        lineTo(14f, 18f)
        lineTo(15.41f, 16.59f)
        lineTo(10.83f, 12f)
        close()
    }.build()

    val ChevronRight: ImageVector = ImageVector.Builder(
        name = "ChevronRight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(10f, 6f)
        lineTo(8.59f, 7.41f)
        lineTo(13.17f, 12f)
        lineTo(8.59f, 16.59f)
        lineTo(10f, 18f)
        lineTo(16f, 12f)
        close()
    }.build()

    val ArrowUpward: ImageVector = ImageVector.Builder(
        name = "ArrowUpward",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(4f, 12f)
        lineTo(5.41f, 13.41f)
        lineTo(11f, 7.83f)
        verticalLineTo(20f)
        horizontalLineTo(13f)
        verticalLineTo(7.83f)
        lineTo(18.59f, 13.41f)
        lineTo(20f, 12f)
        lineTo(12f, 4f)
        close()
    }.build()

    val ArrowDownward: ImageVector = ImageVector.Builder(
        name = "ArrowDownward",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(20f, 12f)
        lineTo(18.59f, 10.59f)
        lineTo(13f, 16.17f)
        verticalLineTo(4f)
        horizontalLineTo(11f)
        verticalLineTo(16.17f)
        lineTo(5.41f, 10.59f)
        lineTo(4f, 12f)
        lineTo(12f, 20f)
        close()
    }.build()

    val Focus: ImageVector = ImageVector.Builder(
        name = "Focus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 8f)
        curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f)
        reflectiveCurveToRelative(1.79f, 4f, 4f, 4f)
        reflectiveCurveToRelative(4f, -1.79f, 4f, -4f)
        reflectiveCurveToRelative(-1.79f, -4f, -4f, -4f)
        close()
        moveTo(5f, 15f)
        lineTo(3f, 15f)
        verticalLineToRelative(4f)
        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(-2f)
        lineTo(5f, 19f)
        verticalLineToRelative(-4f)
        close()
        moveTo(5f, 5f)
        verticalLineToRelative(4f)
        lineTo(3f, 9f)
        lineTo(3f, 5f)
        curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(2f)
        lineTo(5f, 5f)
        close()
        moveTo(19f, 3f)
        curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
        verticalLineToRelative(4f)
        horizontalLineToRelative(-2f)
        lineTo(19f, 5f)
        horizontalLineToRelative(-4f)
        lineTo(15f, 3f)
        horizontalLineToRelative(4f)
        close()
        moveTo(19f, 19f)
        verticalLineToRelative(-4f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(4f)
        curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
        horizontalLineToRelative(-4f)
        verticalLineToRelative(-2f)
        horizontalLineToRelative(4f)
        close()
    }.build()
}

