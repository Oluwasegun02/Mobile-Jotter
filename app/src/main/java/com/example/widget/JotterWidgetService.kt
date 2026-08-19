package com.example.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.NoteEntity
import com.example.model.NoteType
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JotterWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return JotterWidgetRemoteViewsFactory(applicationContext)
    }
}

class JotterWidgetRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var pinnedNotes: List<NoteEntity> = emptyList()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            val database = AppDatabase.getDatabase(context)
            runBlocking {
                val allNotes = database.noteDao().getAllActiveNotesSync()
                pinnedNotes = allNotes.filter { it.isPinned }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            pinnedNotes = emptyList()
        }
    }

    override fun onDestroy() {
        pinnedNotes = emptyList()
    }

    override fun getCount(): Int = pinnedNotes.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_pinned_item)
        if (position >= pinnedNotes.size) return views

        val note = pinnedNotes[position]

        // Set type icon
        val (icon, typeName) = when (note.noteType) {
            NoteType.DIARY.name -> "📖" to "Diary"
            NoteType.CHECKLIST.name -> "✓" to "Checklist"
            NoteType.AUDIO.name -> "🎙" to "Voice"
            NoteType.SKETCH.name -> "🎨" to "Sketch"
            else -> "📝" to "Note"
        }
        views.setTextViewText(R.id.widget_item_type_icon, icon)

        // Set title
        val titleText = if (note.title.isNotBlank()) {
            note.title
        } else if (note.noteType == NoteType.DIARY.name) {
            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.createdAt))
            "Diary • $dateStr"
        } else {
            "Untitled Note"
        }
        views.setTextViewText(R.id.widget_item_title, titleText)

        // Set snippet preview
        val snippetText = when {
            note.content.isNotBlank() -> note.content.take(80).replace("\n", " ")
            note.noteType == NoteType.CHECKLIST.name -> "Checklist items..."
            note.noteType == NoteType.SKETCH.name -> "Hand-drawn sketch entry"
            note.noteType == NoteType.AUDIO.name -> "Voice recording memo (${note.audioDurationSeconds}s)"
            else -> "No additional content"
        }
        views.setTextViewText(R.id.widget_item_snippet, snippetText)

        // Set folder
        views.setTextViewText(R.id.widget_item_folder, "${note.folder} • $typeName")

        // Set date
        val dateText = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(note.updatedAt))
        views.setTextViewText(R.id.widget_item_date, dateText)

        // Fill-in Intent for item click
        val fillInIntent = Intent().apply {
            putExtra("EXTRA_NOTE_ID", note.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return pinnedNotes.getOrNull(position)?.id ?: position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
