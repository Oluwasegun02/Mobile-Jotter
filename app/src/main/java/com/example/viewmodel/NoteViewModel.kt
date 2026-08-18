package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.NoteRepository

/**
 * NoteViewModel provides state management and business logic for Jotter notes.
 * It manages note states, exposes reactive StateFlow streams for UI observation,
 * coordinates Room database operations via repository dependency injection,
 * and handles rich note interactions (checklists, voice memos, sketches, PIN locks).
 */
typealias NoteViewModel = JotterViewModel

/**
 * Factory for creating NoteViewModel instances with dependency-injected repository.
 */
class NoteViewModelFactory(
    private val application: Application,
    private val repository: NoteRepository = NoteRepository(AppDatabase.getDatabase(application).noteDao())
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JotterViewModel::class.java)) {
            return JotterViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
