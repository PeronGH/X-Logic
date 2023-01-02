package cf.idontplay.xlogic

sealed class ASTNode {
    abstract fun evaluate(assignments: Map<Char, Boolean>): Boolean

    abstract override fun toString(): String

    fun toFinalString() = this.toString().removeSurrounding("(", ")")

    override fun equals(other: Any?) = this.toString() == other.toString()

    override fun hashCode() = this.toString().hashCode()
}

internal sealed class Operator : ASTNode()

internal sealed class BinaryOperator(protected val lhs: ASTNode, protected val rhs: ASTNode) :
    Operator()

internal class And(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        lhs.evaluate(assignments) && rhs.evaluate(assignments)

    override fun toString() = "($lhs ∧ $rhs)"
}

internal class Or(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        lhs.evaluate(assignments) || rhs.evaluate(assignments)

    override fun toString() = "($lhs ∨ $rhs)"
}

internal class Equiv(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        lhs.evaluate(assignments) == rhs.evaluate(assignments)

    override fun toString() = "($lhs ≡ $rhs)"
}

internal class Implies(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        !lhs.evaluate(assignments) || rhs.evaluate(assignments)

    override fun toString() = "($lhs → $rhs)"
}

internal sealed class UnaryOperator(protected val operand: ASTNode) : Operator()

internal class Not(operand: ASTNode) : UnaryOperator(operand) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        !operand.evaluate(assignments)

    override fun toString() = "¬$operand"
}

internal sealed class Operand : ASTNode()

internal class Variable(private val name: Char) : Operand() {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        assignments[name] ?: throw IllegalArgumentException("Variable $name is not assigned")

    override fun toString() = name.toString()
}

internal class Literal(private val value: Boolean) : Operand() {
    override fun evaluate(assignments: Map<Char, Boolean>) = value

    override fun toString() = if (value) "⊤" else "⊥"
}

// tests
fun main() {
    // test case 1
    // ¬p ∨ q
    val p = Variable('p')
    val q = Variable('q')
    val ast = Or(Not(p), q)

    println(ast.toFinalString())
    println(ast.evaluate(mapOf('p' to true, 'q' to false)))

    // test case 2
    // (p ∧ q) ∨ (r ∧ s)
    val r = Variable('r')
    val s = Variable('s')
    val ast2 = Or(And(p, q), And(r, s))

    println(ast2.toFinalString())
    println(ast2.evaluate(mapOf('p' to true, 'q' to false, 'r' to true, 's' to false)))
}