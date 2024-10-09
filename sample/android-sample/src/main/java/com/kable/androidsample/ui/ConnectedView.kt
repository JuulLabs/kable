package com.kable.androidsample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juul.kable.Advertisement
import com.juul.kable.Characteristic
import com.juul.kable.DiscoveredService
import com.juul.kable.ServicesDiscoveredPeripheral


@Composable
fun ConnectedView(
    modifier: Modifier = Modifier,
    services: List<DiscoveredService>,
    selectedService: DiscoveredService? = null,
    select: (DiscoveredService) -> Unit = {},
    readCharacteristic: (Characteristic) -> Unit = { },
) {
    Column(
        modifier = modifier,
    ) {
        services.forEach {
            ServiceRow(
                it.serviceUuid.toString(),
                select = {
                    select(it)
                },
            )
            if (selectedService == it) {
                it.characteristics.forEach {
                    Row(modifier = Modifier.fillMaxWidth()
                        .background(Color.Blue.copy(alpha = 0.3f))) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = it.characteristicUuid.toString().plus(" - ").plus(it.properties.toString())
                        )
                        Spacer(Modifier.width(20.dp))
                        Button(
                            {
                                readCharacteristic(it)
                            },
                        ) {
                            Text("Read")
                        }
                        Spacer(Modifier.width(20.dp))
                    }
                }

            }
        }
    }
}


@Composable
fun ServiceRow(
    name: String,
    select: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.3f)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(20.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.width(20.dp))
        Button(
            onClick = { select() },
        ) {
            Text("Select")
        }
        Spacer(Modifier.width(20.dp))
    }
}
