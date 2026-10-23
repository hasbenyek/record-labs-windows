package com.recordlabs.desktop.data

import com.ibm.icu.text.Transliterator
import com.atilika.kuromoji.ipadic.Tokenizer
import java.util.concurrent.ConcurrentHashMap

/** Local pronunciation text matching Android's per-line romanization model. */
object WindowsRomanizer {
    private val nonLatinLetter = Regex("[\\p{L}&&[^\\p{IsLatin}]]")
    private val cache = ConcurrentHashMap<String, String>()
    private val genericTransliterator by lazy {
        Transliterator.getInstance("Any-Latin; NFD; [:Nonspacing Mark:] Remove; NFC")
    }
    private val kanaTransliterator by lazy {
        Transliterator.getInstance("Katakana-Latin; Hiragana-Latin; NFD; [:Nonspacing Mark:] Remove; NFC")
    }
    private val japaneseTokenizer by lazy { Tokenizer() }
    private val japaneseScript = Regex("[\\p{IsHiragana}\\p{IsKatakana}]")

    fun romanize(text: String): String? = cache.getOrPut(text) {
        if (!nonLatinLetter.containsMatchIn(text)) return@getOrPut ""
        runCatching {
            if (japaneseScript.containsMatchIn(text)) romanizeJapanese(text)
            else genericTransliterator.transliterate(text).trim()
        }
            .getOrNull()
            ?.takeIf { it.isNotBlank() && !it.equals(text.trim(), ignoreCase = true) }
            .orEmpty()
    }.takeIf(String::isNotBlank)

    private fun romanizeJapanese(text: String): String = japaneseTokenizer.tokenize(text)
        .joinToString(" ") { token ->
            val reading = token.reading.takeUnless { it.isNullOrBlank() || it == "*" }
            if (reading != null) kanaTransliterator.transliterate(reading)
            else kanaTransliterator.transliterate(token.surface)
        }
        .replace(Regex("\\s+([、。！？,.!?])"), "$1")
        .replace(Regex("\\s+"), " ")
        .trim()
}
