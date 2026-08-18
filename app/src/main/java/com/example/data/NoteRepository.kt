package com.example.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val activeNotes: Flow<List<NoteEntity>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val deletedNotes: Flow<List<NoteEntity>> = noteDao.getDeletedNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val allFolders: Flow<List<String>> = noteDao.getAllFolders()
    val activeNotesCount: Flow<Int> = noteDao.getActiveNotesCount()
    val deletedNotesCount: Flow<Int> = noteDao.getDeletedNotesCount()

    fun getNotesByFolder(folder: String): Flow<List<NoteEntity>> =
        noteDao.getNotesByFolder(folder)

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchNotes(query)

    fun getNoteById(id: Long): Flow<NoteEntity?> =
        noteDao.getNoteById(id)

    suspend fun getNoteByIdSync(id: Long): NoteEntity? =
        noteDao.getNoteByIdSync(id)

    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            noteDao.insertNote(note)
        } else {
            noteDao.updateNote(note)
            note.id
        }
    }

    suspend fun moveToTrash(id: Long) = noteDao.moveToTrash(id)

    suspend fun restoreFromTrash(id: Long) = noteDao.restoreFromTrash(id)

    suspend fun setArchived(id: Long, isArchived: Boolean) = noteDao.setArchived(id, isArchived)

    suspend fun setPinned(id: Long, isPinned: Boolean) = noteDao.setPinned(id, isPinned)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = noteDao.setFavorite(id, isFavorite)

    suspend fun deletePermanently(id: Long) = noteDao.deleteNotePermanent(id)

    suspend fun emptyTrash() = noteDao.emptyTrash()
}
