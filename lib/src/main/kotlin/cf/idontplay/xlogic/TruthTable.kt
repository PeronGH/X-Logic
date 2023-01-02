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
    override fun toString(): String {
        val result = StringBuilder()

        // calculate the width of each column
        val columnWidths = sequence {
            for (i in header.indices) {
                yield(
                    sequenceOf(
                        header[i].length,
                        *rows.map { it[i].toString().length }.toTypedArray()
                    )
                        .maxOrNull()!!
                )
            }
        }.toList()

        // generate the header
        result.appendLine(
            sequence {
                for (i in header.indices) {
                    yield(header[i].padStart(columnWidths[i]))
                }
            }.joinToString(" | ")
        )

        // generate the border
        result.appendLine(
            sequence {
                for (i in header.indices) {
                    yield("-".repeat(columnWidths[i]))
                }
            }.joinToString("-+-")
        )

        // generate the rows
        for (row in rows) {
            result.appendLine(
                sequence {
                    for (i in row.indices) {
                        yield(row[i].toString().padStart(columnWidths[i]))
                    }
                }.joinToString(" | ")
            )
        }

        return result.toString()
    }
}

fun main() {
    println(TruthTable("!a | b = a -> b"))
}