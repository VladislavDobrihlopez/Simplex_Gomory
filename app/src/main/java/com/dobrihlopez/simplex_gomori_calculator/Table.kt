package com.dobrihlopez.simplex_gomori_calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Компонент для отображения "таблицы" ввода:
 *  - запасы материалов
 *  - расход материалов на игрушки
 *  - доход с игрушек
 */
@Composable
fun InputTable(
    uiState: MainViewState,
    onValueChange: (InputField, String) -> Unit,
) {
    // Для удобства разобьём поля по смыслу

    // Запасы материалов
    Text("Запасы материалов", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(label = "Ткань", value = uiState.fabricSupply.toString()) {
            onValueChange(InputField.FabricSupply, it)
        }
        TableCell(label = "Синтепон", value = uiState.syntheponSupply.toString()) {
            onValueChange(InputField.SyntheponSupply, it)
        }
        TableCell(label = "Мех", value = uiState.furSupply.toString()) {
            onValueChange(InputField.FurSupply, it)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Расход материалов на игрушку 1
    Text("Расход (игрушка 1)", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(label = "Ткань", value = uiState.fabricToy1.toString()) {
            onValueChange(InputField.FabricToy1, it)
        }
        TableCell(label = "Синтепон", value = uiState.syntheponToy1.toString()) {
            onValueChange(InputField.SyntheponToy1, it)
        }
        TableCell(label = "Мех", value = uiState.furToy1.toString()) {
            onValueChange(InputField.FurToy1, it)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Расход материалов на игрушку 2
    Text("Расход (игрушка 2)", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(label = "Ткань", value = uiState.fabricToy2.toString()) {
            onValueChange(InputField.FabricToy2, it)
        }
        TableCell(label = "Синтепон", value = uiState.syntheponToy2.toString()) {
            onValueChange(InputField.SyntheponToy2, it)
        }
        TableCell(label = "Мех", value = uiState.furToy2.toString()) {
            onValueChange(InputField.FurToy2, it)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Доход
    Text("Доход (за 1 игрушку)", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell(label = "Игрушка 1", value = uiState.incomeToy1.toString()) {
            onValueChange(InputField.IncomeToy1, it)
        }
        TableCell(label = "Игрушка 2", value = uiState.incomeToy2.toString()) {
            onValueChange(InputField.IncomeToy2, it)
        }
    }
}

/**
 * Ячейка "таблицы" для ввода числового значения.
 */
@Composable
fun RowScope.TableCell(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier
        .weight(1f)
        .padding(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}