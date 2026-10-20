package com.seniorenlauncher.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import java.util.Locale

class LocalizedContext(
    private val baseContext: Context,
    private val configContext: Context
) : ContextWrapper(baseContext),
    ActivityResultRegistryOwner,
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    override fun getResources(): Resources {
        return configContext.resources
    }

    override val activityResultRegistry: ActivityResultRegistry
        get() = (baseContext as? ActivityResultRegistryOwner)?.activityResultRegistry
            ?: throw IllegalStateException("baseContext does not implement ActivityResultRegistryOwner")

    override val lifecycle: Lifecycle
        get() = (baseContext as? LifecycleOwner)?.lifecycle
            ?: throw IllegalStateException("baseContext does not implement LifecycleOwner")

    override val viewModelStore: ViewModelStore
        get() = (baseContext as? ViewModelStoreOwner)?.viewModelStore
            ?: throw IllegalStateException("baseContext does not implement ViewModelStoreOwner")

    override val savedStateRegistry: SavedStateRegistry
        get() = (baseContext as? SavedStateRegistryOwner)?.savedStateRegistry
            ?: throw IllegalStateException("baseContext does not implement SavedStateRegistryOwner")
}

object LocaleHelper {
    fun setLocale(ctx: Context, lang: String): Context {
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        val config = Configuration(ctx.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val configContext = ctx.createConfigurationContext(config)
        return LocalizedContext(ctx, configContext)
    }

    val SUPPORTED = mapOf(
        "nl" to "Nederlands",
        "fr" to "Français",
        "en" to "English",
        "de" to "Deutsch",
        "tr" to "Türkçe",
        "ar" to "العربية"
    )
}
