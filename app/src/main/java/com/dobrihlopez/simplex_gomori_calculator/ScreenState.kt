package com.dobrihlopez.simplex_gomori_calculator

/**
 * Снимок таблицы
 */
data class TableSnapshot(
    val stepTitle: String,
    val matrix: List<List<Double>>,
    val rowVars: List<String>,
    val colVars: List<String>
)

/**
 * Состояние экрана:
 *  - входные данные (запасы, расход, доход)
 *  - результаты (x1, x2, maxIncome)
 *  - флаги и списки для ошибок валидации
 */
data class MainViewState(
    val isLoaderShown: Boolean = false,

    // Запасы материалов
    val fabricSupply: Double = 150.0,
    val syntheponSupply: Double = 130.0,
    val furSupply: Double = 120.0,

    // Расход на игрушку 1
    val fabricToy1: Double = 5.0,
    val syntheponToy1: Double = 2.0,
    val furToy1: Double = 1.0,

    // Расход на игрушку 2
    val fabricToy2: Double = 2.0,
    val syntheponToy2: Double = 3.0,
    val furToy2: Double = 7.0,

    // Доход с игрушек
    val incomeToy1: Double = 10.0,
    val incomeToy2: Double = 20.0,

    // Выходные данные (результат)
    val x1: Double = 0.0,
    val x2: Double = 0.0,
    val maxIncome: Double = 0.0,
    val solutionFound: Boolean = false,

    // Ошибки валидации
    val validationErrors: List<String> = emptyList(),

    // Все "снимки" таблиц на каждом шаге
    val snapshots: List<TableSnapshot> = emptyList()
)

/**
 * Перечисление полей ввода (для onFieldChange).
 */
enum class InputField {
    FabricSupply,
    SyntheponSupply,
    FurSupply,

    FabricToy1,
    SyntheponToy1,
    FurToy1,

    FabricToy2,
    SyntheponToy2,
    FurToy2,

    IncomeToy1,
    IncomeToy2
}