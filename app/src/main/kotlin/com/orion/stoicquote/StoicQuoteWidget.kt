package com.orion.stoicquote

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import java.util.Calendar

class StoicQuoteWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            renderWidget(context, appWidgetManager, id)
        }
        scheduleMidnightUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADVANCE) {
            Prefs.advance(context)
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, StoicQuoteWidget::class.java))
            for (id in ids) renderWidget(context, mgr, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMidnightUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelMidnightUpdate(context)
    }

    private fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val offset = Prefs.manualOffset(context)
        val q = QuoteRepo.quoteFor(context, QuoteRepo.todayString(), offset)
        val showSource = Prefs.showSource(context)

        val views = RemoteViews(context.packageName, R.layout.widget)
        views.setTextViewText(R.id.quote_text, "“${q.text}”")
        views.setTextViewText(R.id.quote_author, "— ${q.author}")
        views.setTextViewText(R.id.quote_source, q.source)
        views.setViewVisibility(
            R.id.quote_source,
            if (showSource && q.source.isNotEmpty())
                android.view.View.VISIBLE else android.view.View.GONE
        )

        // Tapping anywhere on the widget advances to the next quote
        val advanceIntent = Intent(context, StoicQuoteWidget::class.java).apply {
            action = ACTION_ADVANCE
        }
        val advancePending = PendingIntent.getBroadcast(
            context,
            0,
            advanceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, advancePending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun scheduleMidnightUpdate(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, StoicQuoteWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, StoicQuoteWidget::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        val pending = PendingIntent.getBroadcast(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmMgr.setRepeating(
            AlarmManager.RTC,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    private fun cancelMidnightUpdate(context: Context) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StoicQuoteWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val pending = PendingIntent.getBroadcast(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmMgr.cancel(pending)
    }

    companion object {
        const val ACTION_ADVANCE = "com.orion.stoicquote.ACTION_ADVANCE"
    }
}
