package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.NoteType
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    /**
     * Serializes all notes to a formatted JSON string
     */
    fun exportToJson(notes: List<NoteEntity>): String {
        val rootObj = JSONObject()
        rootObj.put("appName", "Jotter")
        rootObj.put("version", 1)
        rootObj.put("exportTimestamp", System.currentTimeMillis())
        rootObj.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        rootObj.put("notesCount", notes.size)

        val notesArray = JSONArray()
        for (note in notes) {
            val noteObj = JSONObject()
            noteObj.put("id", note.id)
            noteObj.put("title", note.title)
            noteObj.put("content", note.content)
            noteObj.put("noteType", note.noteType)
            noteObj.put("folder", note.folder)

            val tagsArray = JSONArray()
            note.tags.forEach { tagsArray.put(it) }
            noteObj.put("tags", tagsArray)

            noteObj.put("colorIndex", note.colorIndex)
            noteObj.put("fontStyle", note.fontStyle)
            noteObj.put("fontSize", note.fontSize.toDouble())
            noteObj.put("isPinned", note.isPinned)
            noteObj.put("isFavorite", note.isFavorite)
            noteObj.put("isArchived", note.isArchived)
            noteObj.put("isLocked", note.isLocked)
            noteObj.put("checklistJson", note.checklistJson)
            noteObj.put("sketchDataJson", note.sketchDataJson)
            noteObj.put("audioDurationSeconds", note.audioDurationSeconds)
            noteObj.put("createdAt", note.createdAt)
            noteObj.put("updatedAt", note.updatedAt)

            notesArray.put(noteObj)
        }

        rootObj.put("notes", notesArray)
        return rootObj.toString(2)
    }

    /**
     * Parses JSON string back into a list of NoteEntity
     */
    fun parseJsonBackup(jsonString: String): List<NoteEntity> {
        val list = mutableListOf<NoteEntity>()
        try {
            val trimmed = jsonString.trim()
            val notesArray: JSONArray = if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                root.optJSONArray("notes") ?: JSONArray()
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                return emptyList()
            }

            for (i in 0 until notesArray.length()) {
                val obj = notesArray.getJSONObject(i)
                val tagsList = mutableListOf<String>()
                val tagsArray = obj.optJSONArray("tags")
                if (tagsArray != null) {
                    for (t in 0 until tagsArray.length()) {
                        tagsList.add(tagsArray.getString(t))
                    }
                }

                val note = NoteEntity(
                    id = 0L, // Auto-generate new IDs on import or overwrite
                    title = obj.optString("title", ""),
                    content = obj.optString("content", ""),
                    noteType = obj.optString("noteType", NoteType.TEXT.name),
                    folder = obj.optString("folder", "General"),
                    tags = tagsList,
                    colorIndex = obj.optInt("colorIndex", 0),
                    fontStyle = obj.optString("fontStyle", "SANS"),
                    fontSize = obj.optDouble("fontSize", 16.0).toFloat(),
                    isPinned = obj.optBoolean("isPinned", false),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    isArchived = obj.optBoolean("isArchived", false),
                    isLocked = obj.optBoolean("isLocked", false),
                    isDeleted = false,
                    checklistJson = obj.optString("checklistJson", ""),
                    sketchDataJson = obj.optString("sketchDataJson", ""),
                    audioFilePath = null, // Audio files are local-only; duration is saved
                    audioDurationSeconds = obj.optInt("audioDurationSeconds", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
                list.add(note)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Serializes all notes to human-readable text document
     */
    fun exportToPlainText(notes: List<NoteEntity>): String {
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val exportDate = sdf.format(Date())

        sb.append("=========================================\n")
        sb.append("         JOTTER & DIARY BACKUP           \n")
        sb.append("=========================================\n")
        sb.append("Export Date: $exportDate\n")
        sb.append("Total Entries: ${notes.size}\n")
        sb.append("=========================================\n\n")

        val diaries = notes.filter { it.noteType == NoteType.DIARY.name }
        val standardNotes = notes.filter { it.noteType != NoteType.DIARY.name }

        if (diaries.isNotEmpty()) {
            sb.append("#########################################\n")
            sb.append("         📖 DAILY LIVING DIARIES (${diaries.size})\n")
            sb.append("#########################################\n\n")

            diaries.forEachIndexed { index, diary ->
                sb.append("-----------------------------------------\n")
                sb.append("[DIARY #${index + 1}] ${if (diary.title.isNotBlank()) diary.title else "Daily Living Entry"}\n")
                sb.append("Date: ${sdf.format(Date(diary.createdAt))}\n")
                if (diary.tags.isNotEmpty()) {
                    sb.append("Tags: ${diary.tags.joinToString(", ") { "#$it" }}\n")
                }
                sb.append("-----------------------------------------\n")
                sb.append("${diary.content}\n\n")
            }
        }

        if (standardNotes.isNotEmpty()) {
            sb.append("#########################################\n")
            sb.append("         📝 JOTS & NOTES (${standardNotes.size})\n")
            sb.append("#########################################\n\n")

            standardNotes.forEachIndexed { index, note ->
                sb.append("-----------------------------------------\n")
                sb.append("[NOTE #${index + 1}] ${if (note.title.isNotBlank()) note.title else "Untitled Note"}\n")
                sb.append("Type: ${note.noteType} | Folder: ${note.folder}\n")
                sb.append("Date: ${sdf.format(Date(note.createdAt))}\n")
                if (note.tags.isNotEmpty()) {
                    sb.append("Tags: ${note.tags.joinToString(", ") { "#$it" }}\n")
                }
                if (note.isPinned) sb.append("📌 Pinned Note\n")
                sb.append("-----------------------------------------\n")

                when (note.noteType) {
                    NoteType.CHECKLIST.name -> {
                        val items = note.parseChecklist()
                        if (items.isNotEmpty()) {
                            items.forEach { item ->
                                val check = if (item.isDone) "[x]" else "[ ]"
                                sb.append("$check ${item.text}\n")
                            }
                        } else {
                            sb.append("${note.content}\n")
                        }
                    }
                    else -> {
                        sb.append("${note.content}\n")
                    }
                }
                sb.append("\n")
            }
        }

        return sb.toString()
    }

    /**
     * Reads text content from a content URI
     */
    fun readTextFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: ""
    }

    /**
     * Writes text content to a content URI
     */
    fun writeTextToUri(context: Context, uri: Uri, text: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(text)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
