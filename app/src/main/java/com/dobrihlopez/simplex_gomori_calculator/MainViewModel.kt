package com.dobrihlopez.simplex_gomori_calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel, в котором храним:
 *  - состояние экрана (MainViewState),
 *  - логику валидации,
 *  - вызов симплекс-метода,
 *  - обновление состояния (UI).
 */
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewState())
    val uiState: StateFlow<MainViewState> = _uiState

    /**
     * Обработка изменения текстовых полей.
     */
    fun onFieldChange(field: InputField, newValue: String) {
        _uiState.update { current ->
            when(field) {
                InputField.FabricSupply -> current.copy(fabricSupply = newValue.toDoubleOrNull() ?: -1.0)
                InputField.SyntheponSupply -> current.copy(syntheponSupply = newValue.toDoubleOrNull() ?: -1.0)
                InputField.FurSupply -> current.copy(furSupply = newValue.toDoubleOrNull() ?: -1.0)

                InputField.FabricToy1 -> current.copy(fabricToy1 = newValue.toDoubleOrNull() ?: -1.0)
                InputField.SyntheponToy1 -> current.copy(syntheponToy1 = newValue.toDoubleOrNull() ?: -1.0)
                InputField.FurToy1 -> current.copy(furToy1 = newValue.toDoubleOrNull() ?: -1.0)

                InputField.FabricToy2 -> current.copy(fabricToy2 = newValue.toDoubleOrNull() ?: -1.0)
                InputField.SyntheponToy2 -> current.copy(syntheponToy2 = newValue.toDoubleOrNull() ?: -1.0)
                InputField.FurToy2 -> current.copy(furToy2 = newValue.toDoubleOrNull() ?: -1.0)

                InputField.IncomeToy1 -> current.copy(incomeToy1 = newValue.toDoubleOrNull() ?: -1.0)
                InputField.IncomeToy2 -> current.copy(incomeToy2 = newValue.toDoubleOrNull() ?: -1.0)
            }.copy(
                // При любом вводе сбрасываем старые результаты
                x1 = 0.0,
                x2 = 0.0,
                maxIncome = 0.0,
                solutionFound = false,
                validationErrors = emptyList()
            )
        }
    }

    /**
     * Кнопка "Рассчитать" - сначала валидация, потом вызов решения.
     */
    fun onCalculateClick() {
        val errors = validateInputs(_uiState.value)
        if (errors.isNotEmpty()) {
            // Если есть ошибки, показываем их
            _uiState.update { it.copy(validationErrors = errors) }
        } else {
            // Если нет ошибок, запускаем решение
            solveILP(_uiState.value)
        }
    }

    /**
     * Удаляем все ошибки (закрываем диалог).
     */
    fun clearErrors() {
        _uiState.update { it.copy(validationErrors = emptyList()) }
    }

    /**
     * Проверка, что все введённые поля >= 0.
     */
    private fun validateInputs(state: MainViewState): List<String> {
        val errors = mutableListOf<String>()

        fun checkNonNegative(value: Double, fieldName: String) {
            if (value < 0) {
                errors += "$fieldName не может быть отрицательным или некорректным"
            }
        }

        checkNonNegative(state.fabricSupply, "Запас ткани")
        checkNonNegative(state.syntheponSupply, "Запас синтепона")
        checkNonNegative(state.furSupply, "Запас меха")

        checkNonNegative(state.fabricToy1, "Расход ткани (игрушка 1)")
        checkNonNegative(state.syntheponToy1, "Расход синтепона (игрушка 1)")
        checkNonNegative(state.furToy1, "Расход меха (игрушка 1)")

        checkNonNegative(state.fabricToy2, "Расход ткани (игрушка 2)")
        checkNonNegative(state.syntheponToy2, "Расход синтепона (игрушка 2)")
        checkNonNegative(state.furToy2, "Расход меха (игрушка 2)")

        checkNonNegative(state.incomeToy1, "Доход (игрушка 1)")
        checkNonNegative(state.incomeToy2, "Доход (игрушка 2)")

        return errors
    }

    /**
     * Запуск целочисленного ILP-решения (упрощённый пример).
     * Используем код из объекта [SwappingColumnsRowsExample], адаптируя под пользовательские данные.
     */
    private fun solveILP(state: MainViewState) {
        val st = buildTableFromState(state)

        // Запускаем целочисленный симплекс
        val (success, message) = SwappingColumnsRowsExample.solveIntegerSimplex(st)
        if (!success) {
            // Если нет решения / неограничено / др. ошибка
            _uiState.update {
                it.copy(
                    validationErrors = listOf("Решение не найдено: $message"),
                    solutionFound = false
                )
            }
            return
        }

        // Извлекаем результат
        val (x1, x2) = SwappingColumnsRowsExample.getSolution(st)
        // Считаем целевую функцию (макс. доход)
        val F = computeF(x1, x2, state.incomeToy1, state.incomeToy2)

        _uiState.update {
            it.copy(
                x1 = x1,
                x2 = x2,
                maxIncome = F,
                solutionFound = true
            )
        }
    }

    /**
     * Формируем симплекс-таблицу из пользовательских данных.
     * Аналогично buildInitialTable(), но коэффициенты берём из state.
     */
    private fun buildTableFromState(s: MainViewState): SwappingColumnsRowsExample.SimplexTable {
        /**
         * Пример задачи в общем виде:
         *   s.fabricToy1 * x1 + s.fabricToy2 * x2 <= s.fabricSupply
         *   s.syntheponToy1 * x1 + s.syntheponToy2 * x2 <= s.syntheponSupply
         *   s.furToy1 * x1 + s.furToy2 * x2 <= s.furSupply
         *   max F = s.incomeToy1 * x1 + s.incomeToy2 * x2
         *
         * В симплекс-таблице храним в формате:
         *   colVars: ["RHS", "x1", "x2"]
         *   rowVars: ["x3", "x4", "x5", "F"]
         *   matrix:
         *     [s.fabricSupply,   s.fabricToy1,   s.fabricToy2 ]
         *     [s.syntheponSupply,s.syntheponToy1,s.syntheponToy2]
         *     [s.furSupply,      s.furToy1,      s.furToy2     ]
         *     [0,               -s.incomeToy1,  -s.incomeToy2 ]
         */
        val matrix = arrayOf(
            doubleArrayOf(s.fabricSupply, s.fabricToy1, s.fabricToy2),
            doubleArrayOf(s.syntheponSupply, s.syntheponToy1, s.syntheponToy2),
            doubleArrayOf(s.furSupply, s.furToy1, s.furToy2),
            doubleArrayOf(0.0, -s.incomeToy1, -s.incomeToy2)
        )
        val rowVars = mutableListOf("x3","x4","x5","F")
        val colVars = mutableListOf("RHS","x1","x2")
        return SwappingColumnsRowsExample.SimplexTable(matrix, rowVars, colVars)
    }

    /**
     * Считаем целевую функцию: incomeToy1*x1 + incomeToy2*x2
     */
    private fun computeF(x1:Double, x2:Double, inc1:Double, inc2:Double): Double {
        return inc1*x1 + inc2*x2
    }
}
