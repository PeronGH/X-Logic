/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package cf.idontplay.xlogic

import java.util.*

fun parse(input: String): ASTNode {
    val expression = preprocess(input)
    val rpn = convertToRPN(expression)
    return parseRPN(rpn)
}

private fun preprocess(input: String) = input
    .replace("\\s+".toRegex(), "")
    .replace("equiv|=|↔|<->".toRegex(), " ≡ ")
    .replace("implies|->".toRegex(), " → ")
    .replace("or|\\|".toRegex(), " ∨ ")
    .replace("and|&".toRegex(), " ∧ ")
    .replace("not|!|~|-".toRegex(), "¬")
    .trim()

// Token Helpers

private typealias Token = Char

private fun Token.isOperator() =
    this.isBinaryOperator() || this.isUnaryOperator()

private fun Token.isUnaryOperator() = this == '¬'

private fun Token.isBinaryOperator() =
    this == '∧' || this == '∨' || this == '≡' || this == '→'

private fun Token.getPrecedence(): Int = when (this) {
    '¬' -> 4
    '∧', '∨' -> 3
    '→' -> 2
    '≡' -> 1
    else -> throw IllegalArgumentException("Invalid operator: $this")
}

private fun Token.toBinaryOperator(lhs: ASTNode, rhs: ASTNode): BinaryOperator = when (this) {
    '∧' -> And(lhs, rhs)
    '∨' -> Or(lhs, rhs)
    '≡' -> Equiv(lhs, rhs)
    '→' -> Implies(lhs, rhs)
    else -> throw IllegalArgumentException("Invalid binary operator: $this")
}

private fun Token.toUnaryOperator(operand: ASTNode): UnaryOperator = when (this) {
    '¬' -> Not(operand)
    else -> throw IllegalArgumentException("Invalid unary operator: $this")
}

private fun Token.isOperand() = this.isLetter() || this == '⊤' || this == '⊥'

private fun Token.toOperand(): Operand? = when (this) {
    'T', '⊤' -> Literal(true)
    'F', '⊥' -> Literal(false)
    else -> if (this.isOperand()) Variable(this) else null
}

private fun Token.isLeftParen() = this == '('

private fun Token.isRightParen() = this == ')'

// Convert to RPN, assume the expression is preprocessed
private fun convertToRPN(expression: String): List<Token> {
    val output = mutableListOf<Token>()
    val stack = Stack<Token>()

    for (token in expression) {
        when {
            token.isOperand() -> output.add(token)
            token.isOperator() -> {
                while (stack.isNotEmpty() && stack.peek()
                        .isOperator() && token.getPrecedence() <= stack.peek().getPrecedence()
                ) {
                    output.add(stack.pop())
                }
                stack.push(token)
            }
            token.isLeftParen() -> stack.push(token)
            token.isRightParen() -> {
                while (stack.isNotEmpty() && !stack.peek().isLeftParen()) {
                    output.add(stack.pop())
                }
                stack.pop()
            }
        }
    }

    while (stack.isNotEmpty()) {
        output.add(stack.pop())
    }

    return output
}

// parse RPN to AST
private fun parseRPN(tokens: List<Token>): ASTNode {
    val stack = Stack<ASTNode>()

    for (token in tokens) {
        when {
            token.isOperand() -> stack.push(token.toOperand()!!)
            token.isBinaryOperator() -> {
                val rhs = stack.pop()
                val lhs = stack.pop()
                stack.push(token.toBinaryOperator(lhs, rhs))
            }
            token.isUnaryOperator() -> {
                val operand = stack.pop()
                stack.push(token.toUnaryOperator(operand))
            }
        }
    }

    if (stack.size != 1) {
        throw Exception("Invalid expression")
    }

    return stack.pop()
}

// tests
fun main() {
    // preprocess
    val expr = preprocess("not a or b")
    println(expr)

    val expr2 = preprocess("a & b | c")
    println(expr2)

    val expr3 = preprocess("!p | q = p -> q")
    println(expr3)

    // rpn
    val rpn = convertToRPN(expr)
    println(rpn)

    val rpn2 = convertToRPN(expr2)
    println(rpn2)

    val rpn3 = convertToRPN(expr3)
    println(rpn3)

    // parse
    val ast = parseRPN(rpn)
    println(ast.toFinalString())

    val ast2 = parseRPN(rpn2)
    println(ast2.toFinalString())

    val ast3 = parseRPN(rpn3)
    println(ast3.toFinalString())

    // final
    val final = parse("not a or b")
    println(final.toFinalString())
}
