package com.dobrihlopez.simplex_gomori_calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Компонент для отображения одного "снимка" симплекс-таблицы.
 */
@Composable
fun SnapshotTableView(snapshot: TableSnapshot, index: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Шаг ${index + 1}: ${snapshot.stepTitle}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Выведем табличку: первая строка - colVars,
        // потом каждая строка - rowVars[i] и значения из matrix[i].
        // Для упрощения делаем текстом в нескольких Row.

        // Шапка (colVars)
        Row {
            Text("      ", modifier = Modifier.width(50.dp)) // под rowVar
            snapshot.colVars.forEach { col ->
                Text(
                    text = col,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Разделитель
        Row {
            Text("------", modifier = Modifier.width(50.dp))
            snapshot.colVars.forEach { _ ->
                Text("------", modifier = Modifier.width(60.dp))
            }
        }

        // Основные строки
        snapshot.matrix.forEachIndexed { i, rowValues ->
            Row {
                val rowName = snapshot.rowVars[i]
                Text(rowName, modifier = Modifier.width(50.dp))
                rowValues.forEach { value ->
                    Text(
                        text = String.format("%.2f", value),
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}