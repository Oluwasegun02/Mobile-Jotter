package com.example.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {

    val activeNotes: Flow<List<NoteEntity>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val deletedNotes: Flow<List<NoteEntity>> = noteDao.getDeletedNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val allDbFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
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

    suspend fun moveNoteToFolder(noteId: Long, folderName: String) {
        noteDao.moveNoteToFolder(noteId, folderName)
    }

    suspend fun createFolder(name: String, colorHex: Long = 0xFF3B82F6): Long {
        val existing = folderDao.getFolderByName(name)
        return if (existing != null) {
            existing.id
        } else {
            folderDao.insertFolder(FolderEntity(name = name, colorHex = colorHex))
        }
    }

    suspend fun renameFolder(oldName: String, newName: String, newColorHex: Long? = null) {
        if (oldName.equals(newName, ignoreCase = true) && newColorHex != null) {
            folderDao.updateFolderColor(oldName, newColorHex)
            return
        }
        if (newColorHex != null) {
            folderDao.renameFolder(oldName, newName, newColorHex)
        } else {
            folderDao.renameFolderNameOnly(oldName, newName)
        }
        noteDao.renameFolderInNotes(oldName, newName)
    }

    suspend fun deleteFolder(folderName: String, destinationFolder: String = "General") {
        noteDao.moveAllNotesInFolder(folderName, destinationFolder)
        folderDao.deleteFolderByName(folderName)
    }

    suspend fun moveToTrash(id: Long) = noteDao.moveToTrash(id)

    suspend fun restoreFromTrash(id: Long) = noteDao.restoreFromTrash(id)

    suspend fun setArchived(id: Long, isArchived: Boolean) = noteDao.setArchived(id, isArchived)

    suspend fun setPinned(id: Long, isPinned: Boolean) = noteDao.setPinned(id, isPinned)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = noteDao.setFavorite(id, isFavorite)

    suspend fun getAllNotesForBackup(): List<NoteEntity> = noteDao.getAllActiveNotesSync()

    suspend fun restoreNotes(notes: List<NoteEntity>, overwrite: Boolean = false) {
        if (overwrite) {
            noteDao.clearAllNotes()
        }
        noteDao.insertNotes(notes)
    }

    suspend fun deletePermanently(id: Long) = noteDao.deleteNotePermanent(id)

    suspend fun emptyTrash() = noteDao.emptyTrash()
}
