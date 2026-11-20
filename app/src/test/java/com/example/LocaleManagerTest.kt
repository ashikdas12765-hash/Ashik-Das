package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.i18n.AppLanguage
import com.example.i18n.LocaleManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocaleManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        LocaleManager.applyLocale(context, AppLanguage.ENGLISH)
    }

    @Test
    fun applyLocale_switchesAndroidResourcesBetweenEnglishAndBengali() {
        val enContext = LocaleManager.applyLocale(context, AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, LocaleManager.currentLanguage.value)
        assertEquals("en", Locale.getDefault().language)
        assertEquals(
            "Switch language between English and Bengali",
            enContext.getString(R.string.language_toggle_cd)
        )

        val bnContext = LocaleManager.applyLocale(context, AppLanguage.BENGALI)
        assertEquals(AppLanguage.BENGALI, LocaleManager.currentLanguage.value)
        assertEquals("bn", Locale.getDefault().language)
        assertEquals(
            "ভাষা পরিবর্তন করুন (English / বাংলা)",
            bnContext.getString(R.string.language_toggle_cd)
        )
    }

    @Test
    fun toggleLanguage_alternatesBetweenEnglishAndBengaliAndPersists() {
        val firstToggle = LocaleManager.toggleLanguage(context)
        assertEquals(AppLanguage.BENGALI, firstToggle)
        assertEquals(AppLanguage.BENGALI, LocaleManager.getSavedLanguage(context))

        val secondToggle = LocaleManager.toggleLanguage(context)
        assertEquals(AppLanguage.ENGLISH, secondToggle)
        assertEquals(AppLanguage.ENGLISH, LocaleManager.getSavedLanguage(context))
    }

    @Test
    fun fromLanguageTag_resolvesRegionalVariants() {
        assertEquals(AppLanguage.BENGALI, LocaleManager.fromLanguageTag("bn"))
        assertEquals(AppLanguage.BENGALI, LocaleManager.fromLanguageTag("bn-BD"))
        assertEquals(AppLanguage.BENGALI, LocaleManager.fromLanguageTag("bn_IN"))
        assertEquals(AppLanguage.ENGLISH, LocaleManager.fromLanguageTag("en-US"))
        assertEquals(AppLanguage.ENGLISH, LocaleManager.fromLanguageTag(null))
    }
}
