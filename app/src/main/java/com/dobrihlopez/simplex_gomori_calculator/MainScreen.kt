package com.dobrihlopez.simplex_gomori_calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    val containerScrollState = rememberScrollState()
    LaunchedEffect(key1 = uiState.value.maxIncome) {
        if (uiState.value.validationErrors.isEmpty()) {
            containerScrollState.animateScrollTo(containerScrollState.maxValue)
        }
    }

    // Состояние для диалога об ошибках
    val errorDialogVisible = uiState.value.validationErrors.isNotEmpty()

    Scaffold(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(containerScrollState)
        ) {
            Text(
                text = "Введите данные для расчёта максимального дохода:",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Таблица ввода (запасы, расход, доход)
            InputTable(
                uiState = uiState.value,
                onValueChange = { field, newValue ->
                    viewModel.onFieldChange(field, newValue)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Рассчитать"
            Button(
                onClick = { viewModel.onCalculateClick() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Рассчитать")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Вывод результатов (только если решение найдено)
            if (uiState.value.solutionFound) {
                Text(
                    "Максимальный доход: ${uiState.value.maxIncome}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Оптимальное количество игрушек вида 1: ${uiState.value.x1}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Оптимальное количество игрушек вида 2: ${uiState.value.x2}",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        // Диалог с ошибками валидации
        if (errorDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.clearErrors() },
                title = { Text("Ошибка ввода данных") },
                text = {
                    Column {
                        uiState.value.validationErrors.forEach { errorMsg ->
                            Text(text = "• $errorMsg")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearErrors() }) {
                        Text("Окей")
                    }
                }
            )
        }
    }
}