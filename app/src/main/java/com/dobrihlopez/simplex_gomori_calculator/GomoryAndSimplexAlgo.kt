package com.dobrihlopez.simplex_gomori_calculator

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive

/**
 * Упрощённый пример симплекс-метода (с отсечениями Гомори),
 * как в тексте задачи. Код адаптирован под нужды проекта.
 *
 * В реальном проекте лучше использовать уже готовые библиотеки LP/MILP,
 * но здесь приведён учебный пример.
 */
object SwappingColumnsRowsExample {

    // -------------------- Структуры данных --------------------

    data class SimplexTable(
        var matrix: Array<DoubleArray>, // matrix[i][j]
        val rowVars: MutableList<String>,
        val colVars: MutableList<String>
    )

    // Чтобы сохранить "снимок" таблицы, удобно сделать метод-расширение:
    private fun SimplexTable.toSnapshot(title: String): TableSnapshot {
        // Преобразуем matrix в List<List<Double>>
        val matrixAsList = this.matrix.map { row -> row.toList() }
        return TableSnapshot(
            stepTitle = title,
            matrix = matrixAsList,
            rowVars = this.rowVars.toList(),
            colVars = this.colVars.toList()
        )
    }

    // -------------------- Печать таблицы --------------------

    fun printTable(st: SimplexTable) {
        val (matrix, rows, colVars) = st
        println("      | " + colVars.joinToString("  ") { "%4s".format(it) })
        println("------+-------------------------------------")
        for (i in matrix.indices) {
            val rowName = rows[i]
            val rowData = matrix[i].joinToString("  ") { "%6.2f".format(it) }
            println("%4s | %s".format(rowName, rowData))
        }
        println()
    }

    // -------------------- Правило прямоугольника (pivot) --------------------

    fun doPivot(
        st: SimplexTable,
        r: Int,
        s: Int,
        snapshots: MutableList<TableSnapshot>? = null, // добавим параметр для логирования
        stepName: String = "Pivot"
    ) {
        val (matrix, rowVars, colVars) = st
        val old = matrix.map { it.copyOf() }
        val pivot = old[r][s]

        val m = matrix.size    // кол-во строк
        val n = matrix[0].size // кол-во столбцов

        for (i in 0 until m) {
            for (j in 0 until n) {
                matrix[i][j] = when {
                    i == r && j == s -> 1.0 / old[i][j]
                    i == r -> old[i][j] / pivot
                    j == s -> - old[i][j] / pivot
                    else   -> (old[i][j]*pivot - old[i][s]*old[r][j]) / pivot
                }
            }
        }
        // Меняем местами rowVars[r] и colVars[s]
        val temp = rowVars[r]
        rowVars[r] = colVars[s]
        colVars[s] = temp

        // Сохраняем снимок таблицы
        snapshots?.add(st.toSnapshot("$stepName (строка=$r, столбец=$s)"))
    }
    // -------------------- Поиск pivot-элементов (упрощённый) --------------------

    fun findPivotColForOpt(st: SimplexTable): Int? {
        val rowF = st.matrix.last()
        var pivotCol: Int? = null
        var minVal = 0.0
        for (j in 1 until rowF.size) {
            if (rowF[j] < minVal) {
                minVal = rowF[j]
                pivotCol = j
            }
        }
        return pivotCol
    }

    fun findPivotRowForOpt(st: SimplexTable, pivotCol: Int): Int? {
        var pivotRow: Int? = null
        var minRatio = Double.POSITIVE_INFINITY
        for (i in 0 until st.matrix.size - 1) {
            val rhs = st.matrix[i][0]
            val coef = st.matrix[i][pivotCol]
            if (coef > 1e-9) {
                val ratio = rhs / coef
                if (ratio < minRatio) {
                    minRatio = ratio
                    pivotRow = i
                }
            }
        }
        return pivotRow
    }

    fun solveSimplexPhase1(st: SimplexTable, snapshots: MutableList<TableSnapshot>? = null): Boolean {
        var fixedSomething = false
        while (true) {
            // 1) ищем строку с отрицательным ЗБП
            var pivotRow: Int? = null
            for (i in 0 until st.matrix.size - 1) {
                if (st.matrix[i][0] < -1e-6) {
                    pivotRow = i
                    break
                }
            }
            // если не нашли, все ЗБП >= 0 => план опорный
            if (pivotRow == null) break

            // 2) ищем столбец с отрицательным коэффициентом
            val row = st.matrix[pivotRow]
            var pivotCol: Int? = null
            for (j in 1 until row.size) {
                if (row[j] < -1e-6) {
                    pivotCol = j
                    break
                }
            }
            if (pivotCol == null) break

            // 3) pivot
            doPivot(st, pivotRow, pivotCol, snapshots, "Пересчёт симплекс-таблицы")
            fixedSomething = true
        }
        return fixedSomething
    }

    fun solveSimplex(st: SimplexTable, snapshots: MutableList<TableSnapshot>? = null): Boolean {
        while (true) {
            solveSimplexPhase1(st, snapshots)

            val pivotCol = findPivotColForOpt(st) ?: return true // нет отриц => оптимум
            val pivotRow = findPivotRowForOpt(st, pivotCol) ?: return false // неограниченно
            doPivot(st, pivotRow, pivotCol, snapshots, "Пересчёт симплекс-таблицы")
        }
    }

    // -------------------- Извлечение решения x1, x2 --------------------

    fun getSolution(st: SimplexTable): Pair<Double, Double> {
        var x1 = 0.0
        var x2 = 0.0
        for (i in st.matrix.indices) {
            val rv = st.rowVars[i]
            if (rv == "x1") x1 = st.matrix[i][0]
            if (rv == "x2") x2 = st.matrix[i][0]
        }
        return x1 to x2
    }

    // -------------------- ЦЕЛОЧИСЛЕННОСТЬ: метод Гомори --------------------

    suspend fun solveIntegerSimplex(st: SimplexTable): Pair<Boolean, String> {
        return coroutineScope {
            // Список «снимков» для всех шагов
            val snapshots = mutableListOf<TableSnapshot>()
            // Добавим в начало «исходную» таблицу
            snapshots.add(st.toSnapshot("Начальная таблица"))

            var iteration = 0
            while (true) {
                val isOptimal = solveSimplex(st, snapshots)
                if (!isOptimal) {
                    // Возвращаем "ложь" и сами snapshots не возвращаем из метода напрямую,
                    // но мы можем сохранить их во временное поле или вернуть в другом формате.
                    // Для примера будем хранить их в Companion-объекте, чтобы потом ViewModel мог забрать.
                    latestSnapshots = snapshots
                    return@coroutineScope false to "Функция неограничена"
                }

                // Проверка целочисленности
                val (varName, _) = findNonIntegerVariable(st) ?: run {
                    // если не нашли нецелых переменных -> всё ок
                    latestSnapshots = snapshots
                    return@coroutineScope true to "Оптимальное целочисленное решение найдено"
                }

                // Добавляем отсечение
                if (!addGomoryCut(st, varName, snapshots)) {
                    latestSnapshots = snapshots
                    return@coroutineScope false to "Нет целочисленного решения (отсечение невозможно)"
                }

                iteration++
                if (iteration > 20) {
                    latestSnapshots = snapshots
                    return@coroutineScope false to "Слишком много итераций, решение не найдено"
                }
                ensureActive()
            }
            error("illegal state")
        }
    }

    fun findNonIntegerVariable(st: SimplexTable): Pair<String, Double>? {
        var maxFraction = 0.0
        var selectedVar = ""
        for (i in 0 until st.matrix.size - 1) {
            val value = st.matrix[i][0]
            val frac = value - kotlin.math.floor(value)
            if (frac > 1e-6 && frac < 1 - 1e-6 && frac > maxFraction) {
                maxFraction = frac
                selectedVar = st.rowVars[i]
            }
        }
        return if (selectedVar.isNotEmpty()) selectedVar to maxFraction else null
    }

    fun addGomoryCut(
        st: SimplexTable,
        varName: String,
        snapshots: MutableList<TableSnapshot>? = null
    ): Boolean {
        val row = st.rowVars.indexOf(varName)
        if (row == -1) return false

        val originalRow = st.matrix[row]
        val newCutRow = DoubleArray(originalRow.size) { j ->
            -(originalRow[j] - kotlin.math.floor(originalRow[j]))
        }
        // Если все коэффициенты почти целые, отсечение бессмысленно
        if (newCutRow.all { kotlin.math.abs(it) < 1e-6 }) return false

        val fIndex = st.rowVars.indexOf("F")
        val newMatrixList = st.matrix.toMutableList()
        newMatrixList.add(fIndex, newCutRow)
        st.matrix = newMatrixList.toTypedArray()

        val cutVarName = "x${st.rowVars.size + st.colVars.size - 1}"
        st.rowVars.add(fIndex, cutVarName)

        snapshots?.add(st.toSnapshot("Вводим новое ограничение $varName -> $cutVarName"))
        return true
    }

    // Храним последние полученные шаги (снимки) во временном поле,
    // чтобы потом забрать их во ViewModel (или вернуть из метода, как вам удобнее).
    var latestSnapshots: List<TableSnapshot> = emptyList()
}