package cf.idontplay.xlogic

import kotlin.math.pow

class TruthTable(private val expression: String) {
    private val ast = parse(expression)

    private val variables = ast.getVariables().sortedBy { it.name }

    private val header =
        sequenceOf(*variables.toTypedArray(), ast).map { it.toFinalString() }.toList()

    private val rows = sequence {
        for (i in 0 until 2.0.pow(variables.size).toInt()) {
            val assignments = variables.mapIndexed { index, variable ->
                variable.name to (i shr index and 1 == 1)
            }.toMap()

            yield(
                sequenceOf(*assignments.values.toTypedArray(), ast.evaluate(assignments))
            )
        }
    }.flatten().chunked(variables.size + 1).toList()

    // generate the table with alignment and border
    override fun toString():String {
        val columnWidths = sequence {
            for (i in header.indices) {
                yield(rows.maxOfOrNull { it[i].toString().length } ?: 0)
            }
        }.toList()

        val headerString = sequence {
            for (i in header.indices) {
                yield(header[i].padEnd(columnWidths[i]))
            }
        }.joinToString(" | ")

        val rowsString = sequence {
            for (row in rows) {
                yield(sequence {
                    for (i in row.indices) {
                        yield(row[i].toString().padEnd(columnWidths[i]))
                    }
                }.joinToString(" | "))
            }
        }.joinToString("\n")

        val border = sequence {
            for (i in header.indices) {
                yield("-".repeat(columnWidths[i]))
            }
        }.joinToString("-+-")

        return "$headerString\n$border\n$rowsString"
    }
}

fun main() {
    println(TruthTable("!a | b = a -> b"))
}