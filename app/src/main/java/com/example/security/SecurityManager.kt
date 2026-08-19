package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jotter_security_prefs", Context.MODE_PRIVATE)

    private val unlockedNoteIds = mutableSetOf<Long>()
    private var isSessionUnlocked: Boolean = false

    fun hasMasterPin(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun isAppLockEnabled(): Boolean {
        // App lock is enabled if a master PIN exists and startup lock hasn't been explicitly disabled
        return hasMasterPin() && prefs.getBoolean(KEY_STARTUP_LOCK_ENABLED, true)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STARTUP_LOCK_ENABLED, enabled).apply()
    }

    fun isAppUnlocked(): Boolean {
        if (!isAppLockEnabled()) return true
        return isSessionUnlocked
    }

    fun unlockApp(pin: String): Boolean {
        if (verifyMasterPin(pin)) {
            isSessionUnlocked = true
            return true
        }
        return false
    }

    fun lockApp() {
        isSessionUnlocked = false
        unlockedNoteIds.clear()
    }

    fun setMasterPin(pin: String): Boolean {
        if (pin.length != 4 || !pin.all { it.isDigit() }) return false
        val hash = hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_STARTUP_LOCK_ENABLED, true)
            .apply()
        isSessionUnlocked = true
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
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_STARTUP_LOCK_ENABLED, false)
            .apply()
        unlockedNoteIds.clear()
        isSessionUnlocked = true
    }

    private fun hashPin(pin: String): String {
        val salted = "jotter_secure_salt_v1_$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "master_pin_hash"
        private const val KEY_STARTUP_LOCK_ENABLED = "startup_lock_enabled"
    }
}
