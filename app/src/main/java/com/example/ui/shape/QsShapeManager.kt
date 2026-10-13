package com.example.ui.shape

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.PathParser
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

data class QsShapeItem(
    val id: String,
    val label: String,
    val description: String,
    val pathData: String,
    val viewportWidth: Float = 100f,
    val viewportHeight: Float = 100f,
    @DrawableRes val drawableRes: Int? = null,
    val isCustom: Boolean = false,
    val rawXml: String? = null
)

/**
 * Compose Shape implementation backed by Android Vector Path data.
 * Scales dynamically to any component dimensions.
 */
class VectorPathShape(
    val pathData: String,
    val viewportWidth: Float = 100f,
    val viewportHeight: Float = 100f
) : Shape {
    private val basePath: android.graphics.Path by lazy {
        try {
            PathParser.createPathFromPathData(pathData)
        } catch (_: Exception) {
            android.graphics.Path()
        }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, 0f, 0f))
        }
        val outPath = android.graphics.Path()
        val matrix = Matrix().apply {
            setScale(size.width / viewportWidth, size.height / viewportHeight)
        }
        basePath.transform(matrix, outPath)
        return Outline.Generic(outPath.asComposePath())
    }
}

/**
 * Drawable implementation backed by Android Vector Path data for traditional Views (like QsTileViewHelper).
 */
class QsPathDrawable(
    private val pathData: String,
    private val viewportWidth: Float = 100f,
    private val viewportHeight: Float = 100f,
    color: Int = Color.WHITE
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    private val basePath: android.graphics.Path by lazy {
        try {
            PathParser.createPathFromPathData(pathData)
        } catch (_: Exception) {
            android.graphics.Path()
        }
    }
    private val scaledPath = android.graphics.Path()

    fun setColor(color: Int) {
        if (paint.color != color) {
            paint.color = color
            invalidateSelf()
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        if (bounds.width() > 0 && bounds.height() > 0) {
            scaledPath.reset()
            val matrix = Matrix()
            matrix.setScale(bounds.width().toFloat() / viewportWidth, bounds.height().toFloat() / viewportHeight)
            basePath.transform(matrix, scaledPath)
            scaledPath.offset(bounds.left.toFloat(), bounds.top.toFloat())
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(scaledPath, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

object QsShapeManager {
    private const val PREFS_NAME = "qs_custom_shapes_prefs"
    private const val KEY_IMPORTED_SHAPES = "imported_shapes_json"

    // 13 Built-in shapes, backed by res/drawable/qs_shape_*.xml
    val BUILT_IN_SHAPES: List<QsShapeItem> = listOf(
        QsShapeItem(
            id = "circle",
            label = "Circle",
            description = "Classic round tile shape",
            pathData = "M 50,0 C 77.614,0 100,22.386 100,50 C 100,77.614 77.614,100 50,100 C 22.386,100 0,77.614 0,50 C 0,22.386 22.386,0 50,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_circle
        ),
        QsShapeItem(
            id = "squircle",
            label = "Squircle",
            description = "Rounded square with 14dp corners",
            pathData = "M 32,0 L 68,0 C 85.673,0 100,14.327 100,32 L 100,68 C 100,85.673 85.673,100 68,100 L 32,100 C 14.327,100 0,85.673 0,68 L 0,32 C 0,14.327 14.327,0 32,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_squircle
        ),
        QsShapeItem(
            id = "square",
            label = "Square",
            description = "Sharp square with 4dp corners",
            pathData = "M 8,0 L 92,0 C 96.418,0 100,3.582 100,8 L 100,92 C 100,96.418 96.418,100 92,100 L 8,100 C 3.582,100 0,96.418 0,92 L 0,8 C 0,3.582 3.582,0 8,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_square
        ),
        QsShapeItem(
            id = "teardrop",
            label = "Teardrop",
            description = "Asymmetric teardrop style",
            pathData = "M 48,0 L 88,0 C 94.627,0 100,5.373 100,12 L 100,52 C 100,78.51 78.51,100 52,100 L 48,100 C 21.49,100 0,78.51 0,52 L 0,48 C 0,21.49 21.49,0 48,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_teardrop
        ),
        QsShapeItem(
            id = "pill",
            label = "Pill",
            description = "Capsule pill shape",
            pathData = "M 40,0 L 60,0 C 82.091,0 100,17.909 100,40 L 100,60 C 100,82.091 82.091,100 60,100 L 40,100 C 17.909,100 0,82.091 0,60 L 0,40 C 0,17.909 17.909,0 40,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_pill
        ),
        QsShapeItem(
            id = "custom_1",
            label = "Leaf",
            description = "Leaf shape with diagonal rounded corners",
            pathData = "M 48,0 L 88,0 C 94.627,0 100,5.373 100,12 L 100,52 C 100,78.51 78.51,100 52,100 L 12,100 C 5.373,100 0,94.627 0,88 L 0,48 C 0,21.49 21.49,0 48,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_1
        ),
        QsShapeItem(
            id = "custom_2",
            label = "Inverted Leaf",
            description = "Inverted diagonal leaf shape",
            pathData = "M 12,0 L 52,0 C 78.51,0 100,21.49 100,48 L 100,88 C 100,94.627 94.627,100 88,100 L 48,100 C 21.49,100 0,78.51 0,52 L 0,12 C 0,5.373 5.373,0 12,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_2
        ),
        QsShapeItem(
            id = "custom_3",
            label = "Arch",
            description = "Arch / dome with rounded top corners",
            pathData = "M 48,0 L 52,0 C 78.51,0 100,21.49 100,48 L 100,88 C 100,94.627 94.627,100 88,100 L 12,100 C 5.373,100 0,94.627 0,88 L 0,48 C 0,21.49 21.49,0 48,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_3
        ),
        QsShapeItem(
            id = "custom_4",
            label = "Inverted Arch",
            description = "Inverted arch with rounded bottom corners",
            pathData = "M 12,0 L 88,0 C 94.627,0 100,5.373 100,12 L 100,52 C 100,78.51 78.51,100 52,100 L 48,100 C 21.49,100 0,78.51 0,52 L 0,12 C 0,5.373 5.373,0 12,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_4
        ),
        QsShapeItem(
            id = "custom_5",
            label = "Shield",
            description = "Shield / badge shape",
            pathData = "M 40,0 L 60,0 C 82.091,0 100,17.909 100,40 L 100,92 C 100,96.418 96.418,100 92,100 L 40,100 C 17.909,100 0,82.091 0,60 L 0,40 C 0,17.909 17.909,0 40,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_5
        ),
        QsShapeItem(
            id = "custom_6",
            label = "Pebble",
            description = "Organic pebble shape",
            pathData = "M 48,0 L 72,0 C 87.46,0 100,12.54 100,28 L 100,72 C 100,87.46 87.46,100 72,100 L 28,100 C 12.54,100 0,87.46 0,72 L 0,48 C 0,21.49 21.49,0 48,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_6
        ),
        QsShapeItem(
            id = "custom_7",
            label = "Flower",
            description = "Material You 4-petal flower shape",
            pathData = "M 50,0 C 65,0 75,10 75,25 C 75,35 85,40 95,45 C 100,47 100,53 95,55 C 85,60 75,65 75,75 C 75,90 65,100 50,100 C 35,100 25,90 25,75 C 25,65 15,60 5,55 C 0,53 0,47 5,45 C 15,40 25,35 25,25 C 25,10 35,0 50,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_7
        ),
        QsShapeItem(
            id = "custom_8",
            label = "Hexagon",
            description = "Rounded hexagon shape",
            pathData = "M 50,4 C 53,4 56,5.5 58,7 L 90,25.5 C 93.8,27.7 96,31.7 96,36 L 96,64 C 96,68.3 93.8,72.3 90,74.5 L 58,93 C 54,95.3 46,95.3 42,93 L 10,74.5 C 6.2,72.3 4,68.3 4,64 L 4,36 C 4,31.7 6.2,27.7 10,25.5 L 42,7 C 44,5.5 47,4 50,4 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_8
        )
    )

    private val _allShapes = MutableStateFlow<List<QsShapeItem>>(BUILT_IN_SHAPES)
    val allShapes: StateFlow<List<QsShapeItem>> = _allShapes.asStateFlow()

    private val shapeCache = ConcurrentHashMap<String, VectorPathShape>()

    fun init(context: Context) {
        loadImportedShapes(context)
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadImportedShapes(context: Context) {
        val jsonStr = getPrefs(context).getString(KEY_IMPORTED_SHAPES, null)
        val importedList = mutableListOf<QsShapeItem>()
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    importedList.add(
                        QsShapeItem(
                            id = obj.getString("id"),
                            label = obj.getString("label"),
                            description = obj.optString("description", "User imported XML shape"),
                            pathData = obj.getString("pathData"),
                            viewportWidth = obj.optDouble("viewportWidth", 100.0).toFloat(),
                            viewportHeight = obj.optDouble("viewportHeight", 100.0).toFloat(),
                            isCustom = true,
                            rawXml = obj.optString("rawXml", null)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _allShapes.value = BUILT_IN_SHAPES + importedList
    }

    private fun saveImportedShapes(context: Context, imported: List<QsShapeItem>) {
        val array = JSONArray()
        imported.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("label", item.label)
                put("description", item.description)
                put("pathData", item.pathData)
                put("viewportWidth", item.viewportWidth.toDouble())
                put("viewportHeight", item.viewportHeight.toDouble())
                put("rawXml", item.rawXml)
            }
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_IMPORTED_SHAPES, array.toString()).apply()
        _allShapes.value = BUILT_IN_SHAPES + imported
    }

    /**
     * Parses an XML Vector Drawable string and returns a QsShapeItem.
     * Throws IllegalArgumentException on invalid XML or missing pathData.
     */
    fun parseXmlDrawable(xmlContent: String, fallbackLabel: String = "Imported Shape"): QsShapeItem {
        val trimmed = xmlContent.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("XML content cannot be empty")
        }

        // Viewport dimensions
        val vpWidthRegex = Regex("""(?:android:)?viewportWidth\s*=\s*["']([0-9.]+)["']""")
        val vpHeightRegex = Regex("""(?:android:)?viewportHeight\s*=\s*["']([0-9.]+)["']""")
        val vpWidth = vpWidthRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 100f
        val vpHeight = vpHeightRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 100f

        // Path data
        val pathRegex = Regex("""(?:android:pathData|pathData|\bd)\s*=\s*["']([^"']+)["']""")
        val pathMatches = pathRegex.findAll(trimmed).map { it.groupValues[1].trim() }.filter { it.isNotEmpty() }.toList()

        if (pathMatches.isEmpty()) {
            throw IllegalArgumentException("No pathData or vector path found in the provided XML")
        }

        val combinedPath = pathMatches.joinToString(" ")

        // Validate path with PathParser
        try {
            PathParser.createPathFromPathData(combinedPath)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid path data syntax in XML: ${e.message}")
        }

        // Try extracting label from android:name
        val nameRegex = Regex("""(?:android:)?name\s*=\s*["']([^"']+)["']""")
        val detectedName = nameRegex.find(trimmed)?.groupValues?.get(1)?.trim()
        val label = if (!detectedName.isNullOrBlank() && detectedName.length <= 25) {
            detectedName
        } else {
            fallbackLabel
        }

        val id = "imported_" + System.currentTimeMillis()

        return QsShapeItem(
            id = id,
            label = label,
            description = "User imported XML drawable shape",
            pathData = combinedPath,
            viewportWidth = vpWidth,
            viewportHeight = vpHeight,
            isCustom = true,
            rawXml = trimmed
        )
    }

    fun importShapeFromXml(
        context: Context,
        xmlContent: String,
        customLabel: String? = null
    ): Result<QsShapeItem> {
        return try {
            val labelToUse = customLabel?.trim()?.takeIf { it.isNotEmpty() } ?: "Custom Shape"
            val shape = parseXmlDrawable(xmlContent, labelToUse)
            val finalShape = if (!customLabel.isNullOrBlank()) shape.copy(label = customLabel.trim()) else shape

            val currentCustom = _allShapes.value.filter { it.isCustom }
            saveImportedShapes(context, currentCustom + finalShape)
            Result.success(finalShape)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun importShapeFromUri(
        context: Context,
        uri: Uri,
        customLabel: String? = null
    ): Result<QsShapeItem> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalArgumentException("Unable to read selected file"))
            val xml = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            val derivedName = customLabel?.takeIf { it.isNotBlank() } ?: run {
                var fileName: String? = null
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }
                fileName?.removeSuffix(".xml") ?: "Imported Shape"
            }

            importShapeFromXml(context, xml, derivedName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteImportedShape(context: Context, shapeId: String): Boolean {
        val currentCustom = _allShapes.value.filter { it.isCustom }
        val updated = currentCustom.filterNot { it.id == shapeId }
        if (updated.size != currentCustom.size) {
            saveImportedShapes(context, updated)
            shapeCache.remove(shapeId)
            return true
        }
        return false
    }

    fun getShapeItem(shapeId: String?): QsShapeItem {
        val shapes = _allShapes.value
        return shapes.firstOrNull { it.id.equals(shapeId, ignoreCase = true) }
            ?: shapes.firstOrNull { it.id == "circle" }
            ?: BUILT_IN_SHAPES.first()
    }

    fun getComposeShape(shapeId: String?): Shape {
        val item = getShapeItem(shapeId)
        return shapeCache.getOrPut(item.id) {
            VectorPathShape(item.pathData, item.viewportWidth, item.viewportHeight)
        }
    }

    fun createDrawable(shapeId: String?, color: Int = Color.WHITE): QsPathDrawable {
        val item = getShapeItem(shapeId)
        return QsPathDrawable(item.pathData, item.viewportWidth, item.viewportHeight, color)
    }
}
