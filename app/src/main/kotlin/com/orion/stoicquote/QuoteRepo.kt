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

    // ── Deterministic shuffle ──────────────────────────────────────────
    // Each cycle of N days (N = number of quotes) gets its own shuffled
    // order of every quote index, so all N quotes are shown exactly once
    // before the order reshuffles for the next cycle. Manual advance
    // walks forward through that same shuffled order, so tapping through
    // quotes no longer walks long runs of the same author back-to-back
    // the way a flat (date-hash + offset) % length index did — quotes.json
    // groups entries by author in big blocks.
    //
    // Must stay byte-for-byte identical to desklet.js's JS port so the
    // phone and desktop agree on the same quote for the same date.

    // Days since 1970-01-01, proleptic Gregorian, pure integer math
    // (Howard Hinnant's days_from_civil) — avoids any Date/timezone API
    // so both platforms agree exactly.
    private fun daysFromCivil(y0: Int, m: Int, d: Int): Long {
        val y = if (m <= 2) y0 - 1 else y0
        val era = Math.floorDiv(if (y >= 0) y else y - 399, 400)
        val yoe = y - era * 400
        val doy = (153 * (m + (if (m > 2) -3 else 9)) + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era.toLong() * 146097 + doe - 719468
    }

    private fun xorshift32(state: Int): Int {
        var s = state
        s = s xor (s shl 13)
        s = s xor (s ushr 17)
        s = s xor (s shl 5)
        return s
    }

    private fun shuffledIndices(n: Int, cycleSeed: Int): IntArray {
        var state = cycleSeed xor -0x61C88647 // same bit pattern as JS's 0x9E3779B9
        if (state == 0) state = 1
        repeat(10) { state = xorshift32(state) } // warm up

        val arr = IntArray(n) { it }
        for (i in n - 1 downTo 1) {
            state = xorshift32(state)
            val unsigned = state.toLong() and 0xFFFFFFFFL
            val j = (unsigned % (i + 1)).toInt()
            val tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp
        }
        return arr
    }

    private fun quoteIndexForDate(dateStr: String, manualOffset: Int, n: Int): Int {
        val (y, m, d) = dateStr.split("-").map { it.toInt() }
        val dayNumber = daysFromCivil(y, m, d)
        val cycleNumber = Math.floorDiv(dayNumber, n.toLong())
        val positionInCycle = (((dayNumber % n) + n) % n).toInt()
        val order = shuffledIndices(n, cycleNumber.toInt())
        return order[(positionInCycle + manualOffset).mod(n)]
    }

    fun quoteFor(context: Context, dateStr: String, manualOffset: Int): Quote {
        val quotes = load(context)
        if (quotes.isEmpty()) return Quote("", "Unknown", "")
        val idx = quoteIndexForDate(dateStr, manualOffset, quotes.size)
        return quotes[idx]
    }
}
