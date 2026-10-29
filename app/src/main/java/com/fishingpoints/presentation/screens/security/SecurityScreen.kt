package com.fishingpoints.presentation.screens.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Безопасность")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
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

                Text("1. \uD83C\uDF0A Безопасность на воде\n" +
                        "\n" +
                        "    Всегда надевайте спасательный жилет при рыбалке с лодки.\n" +
                        "    Не выходите на воду в плохую погоду, во время грозы или сильного ветра.\n" +
                        "    Не перегружайте лодку и не вставайте резко во время движения.\n" +
                        "    Не рыбачьте в состоянии алкогольного опьянения.\n" +
                        "    Следите за уровнем воды и силой течения.\n" +
                        "    Не подходите близко к обрывистым и скользким берегам.\n" +
                        "\n" +
                        "2. ☀\uFE0F Защита от солнца и жары\n" +
                        "\n" +
                        "    Надевайте головной убор и солнцезащитные очки.\n" +
                        "    Используйте солнцезащитный крем.\n" +
                        "    Берите с собой достаточное количество питьевой воды.\n" +
                        "    В жаркую погоду делайте перерывы в тени.\n" +
                        "\n" +
                        "3. \uD83E\uDE9D Осторожность со снастями\n" +
                        "\n" +
                        "    Забрасывайте удочку, убедившись, что рядом нет людей.\n" +
                        "    Обращайтесь с крючками осторожно.\n" +
                        "    Храните острые снасти в специальных чехлах.\n" +
                        "    Не оставляйте крючки и леску на земле, чтобы не травмировать людей и животных.\n" +
                        "    При извлечении крючка из рыбы используйте специальные щипцы.\n" +
                        "\n" +
                        "4. ⛈\uFE0F Погода и природные условия\n" +
                        "\n" +
                        "    Перед рыбалкой проверьте прогноз погоды.\n" +
                        "    При приближении грозы немедленно прекратите рыбалку и уйдите от воды.\n" +
                        "    Не прячьтесь от грозы под одинокими высокими деревьями.\n" +
                        "    Остерегайтесь скользких камней, крутых склонов и густой растительности.\n" +
                        "    Используйте фонарь, если рыбачите в темноте.\n" +
                        "\n" +
                        "5. \uD83D\uDC0D Насекомые и дикие животные\n" +
                        "\n" +
                        "    Используйте репелленты от комаров и клещей.\n" +
                        "    Носите закрытую одежду и подходящую обувь.\n" +
                        "    После рыбалки осмотрите тело на наличие клещей.\n" +
                        "    Не приближайтесь к диким животным и не пытайтесь их кормить.\n" +
                        "\n" +
                        "6. \uD83E\uDDF0 Что взять с собой\n" +
                        "\n" +
                        "Перед поездкой подготовьте:\n" +
                        "\n" +
                        "    Заряженный мобильный телефон.\n" +
                        "    Аптечку первой помощи.\n" +
                        "    Питьевую воду и еду.\n" +
                        "    Фонарь и запасные батарейки.\n" +
                        "    Спасательный жилет, если рыбачите с лодки.\n" +
                        "    Средства связи и навигации, если отправляетесь в удалённое место.\n" +
                        "\n" +
                        "7. \uD83D\uDEA8 Что делать в чрезвычайной ситуации\n" +
                        "\n" +
                        "    Сообщите близким, где вы будете рыбачить и когда планируете вернуться.\n" +
                        "    При опасности звоните в экстренную службу. В большинстве стран номер — 112.\n" +
                        "    Если человек тонет, по возможности бросьте ему спасательный круг или другой плавучий предмет. Не прыгайте в воду без подготовки, чтобы спасти его.\n" +
                        "    При серьёзной травме окажите первую помощь и вызовите спасателей.\n" +
                        "\n" +
                        "✅ Главное правило\n" +
                        "\n" +
                        "Ваша безопасность важнее любого улова! Заранее планируйте рыбалку, учитывайте погодные условия, используйте защитное снаряжение и не рискуйте жизнью ради рыбы.",
                      style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }

}