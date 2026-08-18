package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jotter_security_prefs", Context.MODE_PRIVATE)

    private val unlockedNoteIds = mutableSetOf<Long>()

    fun hasMasterPin(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun setMasterPin(pin: String): Boolean {
        if (pin.length != 4 || !pin.all { it.isDigit() }) return false
        val hash = hashPin(pin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
        return true
    }

    fun verifyMasterPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashPin(pin)
    }

    fun isNoteUnlocked(noteId: Long): Boolean {
        return unlockedNoteIds.contains(noteId)
    }

    fun markNoteUnlocked(noteId: Long) {
        unlockedNoteIds.add(noteId)
    }

    fun lockAllNotes() {
        unlockedNoteIds.clear()
    }

    fun removeMasterPin() {
        prefs.edit().remove(KEY_PIN_HASH).apply()
        unlockedNoteIds.clear()
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "master_pin_hash"
    }
}
