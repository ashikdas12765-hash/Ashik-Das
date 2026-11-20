package com.example.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Runtime locale manager for switching the application language dynamically
 * between English ("en") and Bengali ("bn") without forcing an Activity restart.
 *
 * Updates JVM default [Locale], constructs localized [Configuration] / [Context]
 * instances for Android resource lookups (`values/` and `values-bn/`), and exposes
 * a reactive [currentLanguage] [StateFlow] for Compose UI recomposition.
 */
object LocaleManager {

    private const val PREFS_NAME = "friendzone_locale_prefs"
    private const val KEY_LANGUAGE_CODE = "active_language_code"

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)

    /** Reactive stream of the currently active [AppLanguage]. */
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    /** The active JVM [Locale] corresponding to [currentLanguage]. */
    val currentLocale: Locale
        get() = toLocale(_currentLanguage.value)

    /** Converts an [AppLanguage] to its standard BCP-47 [Locale]. */
    fun toLocale(language: AppLanguage): Locale = Locale.forLanguageTag(language.code)

    /** Resolves an [AppLanguage] from any BCP-47 language tag (e.g. "bn", "bn-BD", "en-US"). */
    fun fromLanguageTag(tag: String?): AppLanguage {
        if (tag.isNullOrBlank()) return AppLanguage.ENGLISH
        val normalized = tag.trim().lowercase(Locale.ROOT)
        return if (normalized == AppLanguage.BENGALI.code || normalized.startsWith("${AppLanguage.BENGALI.code}-") || normalized.startsWith("${AppLanguage.BENGALI.code}_")) {
            AppLanguage.BENGALI
        } else {
            AppLanguage.ENGLISH
        }
    }

    /** Returns the opposite language to toggle between English and Bengali. */
    fun nextLanguage(current: AppLanguage = _currentLanguage.value): AppLanguage = when (current) {
        AppLanguage.ENGLISH -> AppLanguage.BENGALI
        AppLanguage.BENGALI -> AppLanguage.ENGLISH
    }

    /** Reads the synchronously cached language preference for early Activity lifecycle hooks. */
    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_LANGUAGE_CODE, null)
        return if (savedCode != null) {
            fromLanguageTag(savedCode)
        } else {
            _currentLanguage.value
        }
    }

    /** Persists the active language code synchronously for instant availability on next cold start. */
    private fun persistLanguageSync(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE_CODE, language.code)
            .apply()
    }

    /** Creates a copy of [baseConfiguration] configured with the requested [language] locale. */
    fun createLocalizedConfiguration(
        baseConfiguration: Configuration,
        language: AppLanguage
    ): Configuration {
        val locale = toLocale(language)
        return Configuration(baseConfiguration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
    }

    /**
     * Wraps a [Context] with a localized [Configuration] for [language] so that
     * Android resource calls (`getString`, `stringResource`, plurals, etc.) resolve
     * from `values/` or `values-bn/` dynamically at runtime.
     */
    fun createLocalizedContext(context: Context, language: AppLanguage): Context {
        val localizedConfig = createLocalizedConfiguration(context.resources.configuration, language)
        return context.createConfigurationContext(localizedConfig)
    }

    /**
     * Applies [language] dynamically at runtime:
     * 1. Updates [currentLanguage] state.
     * 2. Sets JVM [Locale.setDefault] so formatters (dates, numbers, collation) match.
     * 3. Saves synchronously for cold-start base context wrapping.
     * 4. Returns a localized [Context] backed by the updated [Configuration].
     */
    fun applyLocale(context: Context, language: AppLanguage): Context {
        val locale = toLocale(language)
        Locale.setDefault(locale)
        _currentLanguage.value = language
        persistLanguageSync(context, language)
        return createLocalizedContext(context, language)
    }

    /**
     * Toggles the application language between English and Bengali dynamically at runtime
     * and returns the newly active [AppLanguage].
     */
    fun toggleLanguage(context: Context): AppLanguage {
        val target = nextLanguage(_currentLanguage.value)
        applyLocale(context, target)
        return target
    }

    /**
     * Synchronizes [LocaleManager] with an externally loaded preference (e.g., from DataStore)
     * without toggling.
     */
    fun syncLanguage(context: Context, language: AppLanguage) {
        if (_currentLanguage.value != language || Locale.getDefault().language != language.code) {
            applyLocale(context, language)
        }
    }

    /**
     * Convenience wrapper for [android.app.Activity.attachBaseContext] so the app starts
     * immediately in the user's last selected language.
     */
    fun wrapBaseContext(baseContext: Context): Context {
        val saved = getSavedLanguage(baseContext)
        _currentLanguage.value = saved
        Locale.setDefault(toLocale(saved))
        return createLocalizedContext(baseContext, saved)
    }
}

/** CompositionLocal exposing the currently active [AppLanguage] across the Compose tree. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }
