package com.kable.androidsample.ui

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juul.kable.Advertisement


@Composable
fun ScannerView(
    modifier: Modifier = Modifier,
    advertisement:List<Advertisement>,
    connect: (Advertisement) -> Unit
) {
    Column(
        modifier =modifier,
    ) {
        advertisement.forEach {
            //mix peripheral name and name only iths exist of not unknown
            val title = "Name ${it.name ?: "Unknown"}"

            AdvRow(
                name = title,
            ) {
                connect(it)
            }
        }
    }
}


@Composable
fun AdvRow(name: String, connect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            onClick = connect,
        ) {
            Text("Connect")
        }
        Spacer(Modifier.width(20.dp))
    }
}
