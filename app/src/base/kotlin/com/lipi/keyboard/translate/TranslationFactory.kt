package com.lipi.keyboard.translate

import android.content.Context

object TranslationFactory {
    fun createProvider(context: Context): TranslationProvider {
        return BaseTranslationProvider()
    }
}
