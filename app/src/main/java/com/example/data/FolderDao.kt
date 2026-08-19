package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("UPDATE folders SET name = :newName, colorHex = :newColorHex WHERE name = :oldName")
    suspend fun renameFolder(oldName: String, newName: String, newColorHex: Long? = null)

    @Query("UPDATE folders SET name = :newName WHERE name = :oldName")
    suspend fun renameFolderNameOnly(oldName: String, newName: String)

    @Query("UPDATE folders SET colorHex = :colorHex WHERE name = :name")
    suspend fun updateFolderColor(name: String, colorHex: Long)

    @Query("DELETE FROM folders WHERE name = :name")
    suspend fun deleteFolderByName(name: String)

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int
}
