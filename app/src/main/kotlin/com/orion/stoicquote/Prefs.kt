package com.orion.stoicquote

import android.content.Context

object Prefs {
    private const val FILE = "stoic_quote_prefs"
    private const val KEY_OFFSET = "manualOffset"
    private const val KEY_LAST_DATE = "lastDate"
    private const val KEY_SHOW_SOURCE = "showSource"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun manualOffset(ctx: Context): Int {
        val p = prefs(ctx)
        val today = QuoteRepo.todayString()
        if (p.getString(KEY_LAST_DATE, null) != today) {
            // Day changed — reset offset to 0
            p.edit().putString(KEY_LAST_DATE, today).putInt(KEY_OFFSET, 0).apply()
            return 0
        }
        return p.getInt(KEY_OFFSET, 0)
    }

    fun advance(ctx: Context) {
        val p = prefs(ctx)
        val today = QuoteRepo.todayString()
        val current = if (p.getString(KEY_LAST_DATE, null) == today) p.getInt(KEY_OFFSET, 0) else 0
        p.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_OFFSET, current + 1)
            .apply()
    }

    fun showSource(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_SHOW_SOURCE, true)
    fun setShowSource(ctx: Context, value: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_SHOW_SOURCE, value).apply()
    }
}
