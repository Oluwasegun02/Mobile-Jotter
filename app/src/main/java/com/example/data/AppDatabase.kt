package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.ChecklistItem
import com.example.model.NoteType
import com.example.model.SketchPoint
import com.example.model.SketchStroke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [NoteEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jotter_notes_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialNotes(getDatabase(context).noteDao())
                }
            }

            private suspend fun populateInitialNotes(dao: NoteDao) {
                // 1. Welcome Note (Text)
                dao.insertNote(
                    NoteEntity(
                        title = "Welcome to Jotter 📒",
                        content = "Your fast, offline-ready companion for thoughts, tasks, voice memos, and sketches.\n\n✨ Features to explore:\n• Instant search by keyword & tags\n• Rich checklists with progress tracking\n• Creative sketch pad with colors & eraser\n• Audio voice memos & speech capture\n• Lock notes with 4-digit PIN\n• Dark/Light modes, fonts & pastel color themes\n• Swipe left to archive, swipe right to delete\n• Set reminder alerts for your tasks\n\nTap this card to edit or try adding a new jot below!",
                        noteType = NoteType.TEXT.name,
                        folder = "Ideas",
                        tags = listOf("welcome", "tips", "intro"),
                        colorIndex = 1, // Lemon
                        isPinned = true,
                        isFavorite = true,
                        createdAt = System.currentTimeMillis() - 60000,
                        updatedAt = System.currentTimeMillis() - 60000
                    )
                )

                // 2. Sample Checklist
                val sampleChecklist = listOf(
                    ChecklistItem("1", "Explore different note color themes", true),
                    ChecklistItem("2", "Create a sketch drawing note", false),
                    ChecklistItem("3", "Record a quick voice memo", false),
                    ChecklistItem("4", "Set a reminder alert on a task", false),
                    ChecklistItem("5", "Lock a private note with a PIN", false)
                )
                dao.insertNote(
                    NoteEntity(
                        title = "Today's Quick Checklist 🎯",
                        content = "Key daily goals and productivity targets",
                        noteType = NoteType.CHECKLIST.name,
                        folder = "Personal",
                        tags = listOf("todo", "daily", "goals"),
                        colorIndex = 2, // Mint
                        isPinned = true,
                        checklistJson = NoteEntity.serializeChecklist(sampleChecklist),
                        createdAt = System.currentTimeMillis() - 120000,
                        updatedAt = System.currentTimeMillis() - 120000
                    )
                )

                // 3. Sample Sketch Note
                val sampleStrokes = listOf(
                    SketchStroke(
                        points = listOf(
                            SketchPoint(100f, 180f),
                            SketchPoint(150f, 100f),
                            SketchPoint(200f, 180f),
                            SketchPoint(100f, 180f)
                        ),
                        colorHex = 0xFF6366F1,
                        strokeWidth = 8f
                    ),
                    SketchStroke(
                        points = listOf(
                            SketchPoint(240f, 140f),
                            SketchPoint(320f, 140f),
                            SketchPoint(280f, 220f),
                            SketchPoint(240f, 140f)
                        ),
                        colorHex = 0xFFEC4899,
                        strokeWidth = 8f
                    )
                )
                dao.insertNote(
                    NoteEntity(
                        title = "Design Brainstorm & Wireframe ✏️",
                        content = "Concept sketch for product architecture and layout.",
                        noteType = NoteType.SKETCH.name,
                        folder = "Work",
                        tags = listOf("design", "sketch", "mockup"),
                        colorIndex = 3, // Lavender
                        sketchDataJson = NoteEntity.serializeSketchStrokes(sampleStrokes),
                        createdAt = System.currentTimeMillis() - 180000,
                        updatedAt = System.currentTimeMillis() - 180000
                    )
                )

                // 4. Sample Voice Note
                dao.insertNote(
                    NoteEntity(
                        title = "Voice Memo: Project Ideas 🎙️",
                        content = "Audio recording for the weekend sprint: review UI interactions, verify gesture response, and test reminder notifications.",
                        noteType = NoteType.AUDIO.name,
                        folder = "Study",
                        tags = listOf("voice", "audio", "sprint"),
                        colorIndex = 4, // Rose
                        audioDurationSeconds = 42,
                        createdAt = System.currentTimeMillis() - 240000,
                        updatedAt = System.currentTimeMillis() - 240000
                    )
                )
            }
        }
    }
}
