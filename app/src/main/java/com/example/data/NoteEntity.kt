package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ChecklistItem
import com.example.model.NoteType
import com.example.model.SketchPoint
import com.example.model.SketchStroke
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val noteType: String = NoteType.TEXT.name,
    val folder: String = "General",
    val tags: List<String> = emptyList(), // Direct List<String> field for tags
    val colorIndex: Int = 0,
    val fontStyle: String = "SANS",
    val fontSize: Float = 16f,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val isDeleted: Boolean = false,
    val reminderEpochMillis: Long? = null,
    val checklistJson: String = "",
    val sketchDataJson: String = "",
    val audioFilePath: String? = null,
    val audioDurationSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun parseChecklist(): List<ChecklistItem> {
        if (checklistJson.isBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(checklistJson)
            val list = mutableListOf<ChecklistItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ChecklistItem(
                        id = obj.optString("id", i.toString()),
                        text = obj.optString("text", ""),
                        isDone = obj.optBoolean("isDone", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseSketchStrokes(): List<SketchStroke> {
        if (sketchDataJson.isBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(sketchDataJson)
            val strokes = mutableListOf<SketchStroke>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val colorHex = obj.optLong("colorHex", 0xFF000000)
                val strokeWidth = obj.optDouble("strokeWidth", 6.0).toFloat()
                val isEraser = obj.optBoolean("isEraser", false)
                val pointsArray = obj.optJSONArray("points") ?: JSONArray()
                val points = mutableListOf<SketchPoint>()
                for (j in 0 until pointsArray.length()) {
                    val pObj = pointsArray.getJSONObject(j)
                    points.add(
                        SketchPoint(
                            x = pObj.optDouble("x", 0.0).toFloat(),
                            y = pObj.optDouble("y", 0.0).toFloat()
                        )
                    )
                }
                strokes.add(SketchStroke(points, colorHex, strokeWidth, isEraser))
            }
            strokes
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Helper to return tags cleanly
     */
    fun getTagList(): List<String> = tags

    companion object {
        fun serializeChecklist(items: List<ChecklistItem>): String {
            val jsonArray = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("text", item.text)
                    put("isDone", item.isDone)
                }
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun serializeSketchStrokes(strokes: List<SketchStroke>): String {
            val jsonArray = JSONArray()
            for (stroke in strokes) {
                val strokeObj = JSONObject().apply {
                    put("colorHex", stroke.colorHex)
                    put("strokeWidth", stroke.strokeWidth.toDouble())
                    put("isEraser", stroke.isEraser)
                    val pointsArray = JSONArray()
                    for (pt in stroke.points) {
                        val ptObj = JSONObject().apply {
                            put("x", pt.x.toDouble())
                            put("y", pt.y.toDouble())
                        }
                        pointsArray.put(ptObj)
                    }
                    put("points", pointsArray)
                }
                jsonArray.put(strokeObj)
            }
            return jsonArray.toString()
        }
    }
}
