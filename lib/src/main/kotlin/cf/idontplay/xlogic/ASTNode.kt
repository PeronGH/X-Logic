package cf.idontplay.xlogic

internal sealed class ASTNode {
    abstract fun evaluate(assignments: Map<Char, Boolean>): Boolean

    abstract fun clone(): ASTNode

    abstract override fun toString(): String

    override fun equals(other: Any?) = this.toString() == other.toString()

    override fun hashCode() = this.toString().hashCode()

    fun toFinalString() = this.toString().removeSurrounding("(", ")")

    fun getVariables(): Set<Variable> {
        val variables = mutableSetOf<Variable>()

        when (this) {
            is Literal -> return variables
            is Variable -> variables.add(this)
            is UnaryOperator -> variables.addAll(this.operand.getVariables())
            is BinaryOperator -> {
                variables.addAll(this.lhs.getVariables())
                variables.addAll(this.rhs.getVariables())
            }
        }

        return variables
    }
}

internal sealed class Operator : ASTNode()

internal sealed class BinaryOperator(val lhs: ASTNode, val rhs: ASTNode) :
    Operator()

internal class And(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        lhs.evaluate(assignments) && rhs.evaluate(assignments)

    override fun clone() = And(lhs.clone(), rhs.clone())

    override fun toString() = "($lhs ∧ $rhs)"
}

internal class Or(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        lhs.evaluate(assignments) || rhs.evaluate(assignments)

    override fun clone() = Or(lhs.clone(), rhs.clone())

    override fun toString() = "($lhs ∨ $rhs)"
}

internal class Equiv(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        lhs.evaluate(assignments) == rhs.evaluate(assignments)

    override fun clone() = Equiv(lhs.clone(), rhs.clone())

    override fun toString() = "($lhs ≡ $rhs)"
}

internal class Implies(lhs: ASTNode, rhs: ASTNode) : BinaryOperator(lhs, rhs) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        !lhs.evaluate(assignments) || rhs.evaluate(assignments)

    override fun clone() = Implies(lhs.clone(), rhs.clone())

    override fun toString() = "($lhs → $rhs)"
}

internal sealed class UnaryOperator(val operand: ASTNode) : Operator()

internal class Not(operand: ASTNode) : UnaryOperator(operand) {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        !operand.evaluate(assignments)

    override fun clone() = Not(operand.clone())

    override fun toString() = "¬$operand"
}

internal sealed class Operand : ASTNode()

internal class Variable(val name: Char) : Operand() {
    override fun evaluate(assignments: Map<Char, Boolean>) =
        assignments[name] ?: throw IllegalArgumentException("Variable $name is not assigned")

    override fun clone() = this

    override fun toString() = name.toString()
}

internal class Literal(val value: Boolean) : Operand() {
    override fun evaluate(assignments: Map<Char, Boolean>) = value

    override fun clone() = this

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
    val ast2 = Or(And(p, Or(p, q)), And(r, s))

    println(ast2.toFinalString())
    println(ast2.evaluate(mapOf('p' to true, 'q' to false, 'r' to true, 's' to false)))
    println(ast2.getVariables())
}