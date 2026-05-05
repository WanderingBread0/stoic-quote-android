package com.orion.stoicquote

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Stone = Color(0xFF1C1C1E)
private val Gold = Color(0xFFC9A14A)
private val SoftCream = Color(0xFFE8E4D8)
private val MutedGold = Color(0xFFA88A3F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Stone,
                    surface = Stone,
                    primary = Gold,
                    onBackground = SoftCream,
                    onSurface = SoftCream
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Stone) {
                    QuoteScreen()
                }
            }
        }
    }
}

@Composable
private fun QuoteScreen() {
    val context = LocalContext.current
    var offset by remember { mutableIntStateOf(Prefs.manualOffset(context)) }
    var showSource by remember { mutableStateOf(Prefs.showSource(context)) }

    val q = QuoteRepo.quoteFor(context, QuoteRepo.todayString(), offset)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Stone)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Prefs.advance(context)
                    offset = Prefs.manualOffset(context)
                    refreshAllWidgets(context)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "“${q.text}”",
                    color = SoftCream,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "— ${q.author}",
                    color = Gold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif
                )
                if (showSource && q.source.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = q.source,
                        color = MutedGold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Show source", color = SoftCream, fontSize = 14.sp)
            Switch(
                checked = showSource,
                onCheckedChange = {
                    showSource = it
                    Prefs.setShowSource(context, it)
                    refreshAllWidgets(context)
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tap the quote to see another. Resets at midnight.",
            color = MutedGold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(PaddingValues(horizontal = 8.dp))
        )
    }
}

private fun refreshAllWidgets(context: android.content.Context) {
    val mgr = AppWidgetManager.getInstance(context)
    val ids = mgr.getAppWidgetIds(ComponentName(context, StoicQuoteWidget::class.java))
    if (ids.isEmpty()) return
    val intent = Intent(context, StoicQuoteWidget::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    }
    context.sendBroadcast(intent)
}
