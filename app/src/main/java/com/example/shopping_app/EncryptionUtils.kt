package com.example.shopping_app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE
import androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS


class EncryptionUtils(context: Context) {
    companion object {
        const val SharedPreferencesEmailKey = "email"
        const val SharedPreferencesPasswordKey = "password"
    }
    // Specify the key specification
    private val spec = KeyGenParameterSpec.Builder(
        DEFAULT_MASTER_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(DEFAULT_AES_GCM_MASTER_KEY_SIZE)
        .build()

    // Generate the master key
    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(spec)
        .build()

    // Create the encrypted SharedPreferences
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        "secure_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save encrypted data to SharedPreferences
    fun saveEncryptedData(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    // Remove data from SharedPreferences
    fun removeData(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // Get decrypted data from SharedPreferences
    fun getDecryptedData(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    // check key
    fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}

