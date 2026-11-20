package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import com.example.model.AppPreferences
import com.example.model.PrivacySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.friendZoneDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "friendzone_preferences"
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val PRIVATE_ACCOUNT = booleanPreferencesKey("privacy_private_account")
        val ACTIVITY_STATUS = booleanPreferencesKey("privacy_activity_status")
        val ALLOW_TAGGING = booleanPreferencesKey("privacy_allow_tagging")
    }

    val preferencesFlow: Flow<AppPreferences> = context.friendZoneDataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.code
        val language = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.ENGLISH
        AppPreferences(
            language = language,
            isDarkMode = prefs[Keys.DARK_MODE] ?: false,
            privacy = PrivacySettings(
                isPrivateAccount = prefs[Keys.PRIVATE_ACCOUNT] ?: false,
                showActivityStatus = prefs[Keys.ACTIVITY_STATUS] ?: true,
                allowTagging = prefs[Keys.ALLOW_TAGGING] ?: true
            )
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.friendZoneDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.friendZoneDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun updatePrivacy(transform: (PrivacySettings) -> PrivacySettings) {
        context.friendZoneDataStore.edit { prefs ->
            val current = PrivacySettings(
                isPrivateAccount = prefs[Keys.PRIVATE_ACCOUNT] ?: false,
                showActivityStatus = prefs[Keys.ACTIVITY_STATUS] ?: true,
                allowTagging = prefs[Keys.ALLOW_TAGGING] ?: true
            )
            val updated = transform(current)
            prefs[Keys.PRIVATE_ACCOUNT] = updated.isPrivateAccount
            prefs[Keys.ACTIVITY_STATUS] = updated.showActivityStatus
            prefs[Keys.ALLOW_TAGGING] = updated.allowTagging
        }
    }
}
