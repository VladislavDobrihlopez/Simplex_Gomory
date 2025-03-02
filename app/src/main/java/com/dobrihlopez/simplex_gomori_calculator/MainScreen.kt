package com.dobrihlopez.simplex_gomori_calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import kotlin.math.roundToInt

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState = viewModel.uiState.collectAsState()

    val containerScrollState = rememberScrollState()

    // Если нет ошибок, при обновлении maxIncome скроллим вниз, чтобы увидеть результаты и таблицы
    LaunchedEffect(key1 = uiState.value.maxIncome) {
        if (uiState.value.validationErrors.isEmpty()) {
            containerScrollState.animateScrollTo(containerScrollState.maxValue)
        }
    }

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

            // Таблица ввода
            InputTable(
                uiState = uiState.value,
                onValueChange = { field, newValue ->
                    viewModel.onFieldChange(field, newValue)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.value.isLoaderShown) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    progress = 0.75f
                )
            }

            // Кнопка "Рассчитать"
            Button(
                onClick = { viewModel.onCalculateClick() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Рассчитать")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Вывод результатов
            if (uiState.value.solutionFound) {
                Text("Максимальный доход: ${uiState.value.maxIncome.roundToInt()}", style = MaterialTheme.typography.titleSmall)
                Text("Игрушек вида 1: ${uiState.value.x1.roundToInt()}", style = MaterialTheme.typography.titleSmall)
                Text("Игрушек вида 2: ${uiState.value.x2.roundToInt()}", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Выводим все промежуточные таблицы
            val snapshots = uiState.value.snapshots
            if (snapshots.isNotEmpty()) {
                Text("Промежуточные шаги решения:", style = MaterialTheme.typography.titleMedium)
                snapshots.forEachIndexed { index, snapshot ->
                    SnapshotTableView(snapshot = snapshot, index = index)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Диалог с ошибками
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