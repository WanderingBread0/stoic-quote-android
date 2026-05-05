package com.orion.stoicquote

import android.content.Context
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Quote(val text: String, val author: String, val source: String)

object QuoteRepo {
    @Volatile private var cached: List<Quote>? = null

    fun load(context: Context): List<Quote> {
        cached?.let { return it }
        val raw = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(raw)
        val list = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Quote(
                text = o.optString("text"),
                author = o.optString("author"),
                source = o.optString("source")
            )
        }
        cached = list
        return list
    }

    fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // djb2 — same as the desklet so the quote-of-the-day matches across platforms
    fun djb2(s: String): Long {
        var h = 5381L
        for (c in s) {
            h = ((h shl 5) + h + c.code.toLong()) and 0xFFFFFFFFL
        }
        return h
    }

    fun quoteFor(context: Context, dateStr: String, manualOffset: Int): Quote {
        val quotes = load(context)
        if (quotes.isEmpty()) return Quote("", "Unknown", "")
        val base = (djb2(dateStr) % quotes.size).toInt()
        val idx = ((base + manualOffset) % quotes.size + quotes.size) % quotes.size
        return quotes[idx]
    }
}
