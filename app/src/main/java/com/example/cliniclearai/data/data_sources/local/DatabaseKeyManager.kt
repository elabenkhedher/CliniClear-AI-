package com.example.cliniclearai.data.data_sources.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire sécurisé de clé de chiffrement pour la base de données Room (SQLCipher).
 *
 * Fonctionnement :
 * 1. Génère une clé aléatoire robuste de 256 bits (32 octets) à l'aide de [SecureRandom] lors du premier lancement.
 * 2. Protège cette clé via le matériel sécurisé Android KeyStore en utilisant l'algorithme AES/GCM/NoPadding.
 * 3. Stocke le vecteur d'initialisation (IV) et le texte chiffré dans des SharedPreferences privées.
 * 4. Déchiffre la clé à la volée à l'ouverture de la base sans jamais coder de mot de passe en dur.
 * 5. Gère la transition depuis les anciennes bases de données non chiffrées pour éviter tout crash.
 */
@Singleton
class DatabaseKeyManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "DatabaseKeyManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "cliniclear_db_master_key"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PREFS_NAME = "cliniclear_db_security_prefs"
        private const val PREF_KEY_IV = "encrypted_db_iv"
        private const val PREF_KEY_CIPHERTEXT = "encrypted_db_ciphertext"
        private const val KEY_SIZE_BYTES = 32 // 256 bits
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var cachedPassphrase: ByteArray? = null

    /**
     * Récupère ou génère de façon sécurisée la clé de chiffrement (passphrase) de 256 bits pour SQLCipher.
     */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        cachedPassphrase?.let { return it }

        val storedIvBase64 = prefs.getString(PREF_KEY_IV, null)
        val storedCiphertextBase64 = prefs.getString(PREF_KEY_CIPHERTEXT, null)

        if (!storedIvBase64.isNullOrBlank() && !storedCiphertextBase64.isNullOrBlank()) {
            try {
                val iv = Base64.decode(storedIvBase64, Base64.NO_WRAP)
                val ciphertext = Base64.decode(storedCiphertextBase64, Base64.NO_WRAP)
                val secretKey = getOrCreateMasterKey()

                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                val decryptedPassphrase = cipher.doFinal(ciphertext)
                cachedPassphrase = decryptedPassphrase
                Log.i(TAG, "Clé de base de données déchiffrée avec succès via Android KeyStore.")
                return decryptedPassphrase
            } catch (e: Exception) {
                Log.e(TAG, "Échec du déchiffrement de la clé existante. Régénération sécurisée...", e)
            }
        }

        // Première exécution ou échec de récupération : génération d'une clé de 256 bits
        val newPassphrase = ByteArray(KEY_SIZE_BYTES).apply {
            SecureRandom().nextBytes(this)
        }

        try {
            val secretKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedPassphrase = cipher.doFinal(newPassphrase)

            prefs.edit()
                .putString(PREF_KEY_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .putString(PREF_KEY_CIPHERTEXT, Base64.encodeToString(encryptedPassphrase, Base64.NO_WRAP))
                .apply()

            Log.i(TAG, "Nouvelle clé de 256 bits générée et chiffrée avec succès dans Android KeyStore.")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chiffrement de la nouvelle clé dans KeyStore", e)
        }

        cachedPassphrase = newPassphrase
        return newPassphrase
    }

    /**
     * Récupère ou génère la clé maître AES-256 dans Android KeyStore.
     */
    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(parameterSpec)
            return keyGenerator.generateKey()
        }

        val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * Supprime proprement les fichiers de l'ancienne base non chiffrée afin d'éviter
     * toute tentative d'ouverture invalide ou incompatibilité au premier lancement post-migration.
     */
    fun cleanupLegacyUnencryptedDatabase(legacyDbName: String) {
        try {
            val legacyDbFile = context.getDatabasePath(legacyDbName)
            if (legacyDbFile.exists()) {
                Log.w(TAG, "Nettoyage de l'ancienne base non chiffrée : ${legacyDbFile.absolutePath}")
                context.deleteDatabase(legacyDbName)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Avertissement lors du nettoyage de l'ancienne base legacy", e)
        }
    }
}
