package com.fishingpoints.presentation.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val version = BuildConfig.VERSION_NAME // Версия приложения

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("О приложении")
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, null)
                    }
                }
            )
        }
    )

    { padding ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp)
            )
            {
                Text("Это помощник рыбака, со всей необходимой аналитикой для успешной рыбалки!")
                Text(
                    "\nФункциональность приложения:\n" +
                            "✅ Прогноз погоды - температура, давление, ветер, облачность, осадки.\n" +
                            "✅ Влияние давления - анализ тренда и рекомендации.\n" +
                            "✅ Лунный календарь - фазы луны и их влияние на клев.\n" +
                            "✅ Время суток - рекомендации по лучшему времени для рыбалки.\n" +
                            "✅ Время года - сезонные рекомендации.\n" +
                            "✅ Виды рыб - база данных с описанием, снастями, наживками.\n" +
                            "✅ Рекомендации по снастям - что использовать в текущих условиях.\n" +
                            "✅ Рекомендации по наживке - какая наживка лучше.\n" +
                            "✅ Прикормка - состав, количество, время.\n" +
                            "✅ Ароматизаторы - какие ароматы использовать.\n" +
                            "✅ Дневник рыбалки - сохранение записей об уловах.\n" +
                            "✅ Места рыбалок - наименования и координаты.\n" +
                            "✅ Карта - места рыбалок с уловами, построение " +
                            "маршрута от текущего местоположения до выбранного места. \n" +
                            "✅ Советы рыбака - персонализированные рекомендации.\n" +
                            "✅ Статистика - анализ рыбалок. \n" +
                            "✅ Экипировка на рыбалку. \n" +
                            "✅ Безопасность на рыбалке. \n"
                )
                Text("Автор приложения - ИИ ChatGPT и Н.А.Черный\n")
                Text(version)
            }
        }
    }

}