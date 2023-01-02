package cf.idontplay.xlogic

// Rules to apply to simplify propositional logic expressions

// Associative Laws
// (A ∧ B) ∧ C = A ∧ (B ∧ C)
// (A ∨ B) ∨ C = A ∨ (B ∨ C)
private fun simplifyAssociativeLaws(node: ASTNode): ASTNode = when (node) {
    is And -> when (node.lhs) {
        // (A ∧ B) ∧ C
        is And -> And(node.lhs.lhs, And(node.lhs.rhs, node.rhs))
        else -> when (node.rhs) {
            // A ∧ (B ∧ C)
            is And -> And(And(node.lhs, node.rhs.lhs), node.rhs.rhs)
            else -> node
        }
    }
    is Or -> when (node.lhs) {
        // (A ∨ B) ∨ C
        is Or -> Or(node.lhs.lhs, Or(node.lhs.rhs, node.rhs))
        else -> when (node.rhs) {
            // A ∨ (B ∨ C)
            is Or -> Or(Or(node.lhs, node.rhs.lhs), node.rhs.rhs)
            else -> node
        }
    }
    else -> node
}

// Commutative Laws
// A ∧ B = B ∧ A
// A ∨ B = B ∨ A
private fun simplifyCommutativeLaws(node: ASTNode): ASTNode = when (node) {
    is And -> And(node.rhs, node.lhs)
    is Or -> Or(node.rhs, node.lhs)
    else -> node
}

// Identity Laws
private fun simplifyIdentityLaws(node: ASTNode): ASTNode = when (node) {
    is And -> when {
        node.rhs is Literal && node.rhs.value -> node.lhs // A ∧ T = A
        node.rhs is Literal && !node.rhs.value -> Literal(false) // A ∧ F = F
        else -> node
    }
    is Or -> when {
        node.rhs is Literal && node.rhs.value -> Literal(true) // A ∨ T = T
        node.rhs is Literal && !node.rhs.value -> node.lhs // A ∨ F = A
        else -> node
    }
    else -> node
}

// Distributive Laws
// A ∧ (B ∨ C) = (A ∧ B) ∨ (A ∧ C)
// A ∨ (B ∧ C) = (A ∨ B) ∧ (A ∨ C)
private fun simplifyDistributiveLaws(node: ASTNode): ASTNode = when (node) {
    is And -> when {
        // (A ∨ B) ∧ (A ∨ C) = A ∨ (B ∧ C)
        node.lhs is Or && node.rhs is Or && node.lhs.lhs == node.rhs.lhs ->
            Or(
                node.lhs.lhs,
                And(node.lhs.rhs, node.rhs.rhs)
            )
        // A ∧ (B ∨ C)= (A ∧ B) ∨ (A ∧ C)
        node.rhs is Or ->
            Or(
                And(node.lhs, node.rhs.lhs),
                And(node.lhs, node.rhs.rhs)
            )
        else -> node
    }
    is Or -> when {
        // (A ∧ B) ∨ (A ∧ C) = A ∧ (B ∨ C)
        node.lhs is And && node.rhs is And && node.lhs.lhs == node.rhs.lhs ->
            And(
                node.lhs.lhs,
                Or(node.lhs.rhs, node.rhs.rhs)
            )
        // A ∨ (B ∧ C) = (A ∨ B) ∧ (A ∨ C)
        node.rhs is And ->
            And(
                Or(node.lhs, node.rhs.lhs),
                Or(node.lhs, node.rhs.rhs)
            )
        else -> node
    }
    else -> node
}

// tests
fun main() {
    val a = Variable('A')
    val b = Variable('B')
    val c = Variable('C')
    // Associative Laws
    // (A ∧ B) ∧ C = A ∧ (B ∧ C)
    println(simplifyAssociativeLaws(And(And(a, b), c)).toFinalString())
    // A ∨ (B ∨ C) = (A ∨ B) ∨ C
    println(simplifyAssociativeLaws(Or(a, Or(b, c))).toFinalString())
    // a → (b → c) != (a → b) → c
    println(simplifyAssociativeLaws(Implies(a, Implies(b, c))).toFinalString())

    // Commutative Laws
    // A ∧ B = B ∧ A
    println(simplifyCommutativeLaws(And(a, b)).toFinalString())
    // A ∨ B = B ∨ A
    println(simplifyCommutativeLaws(Or(a, b)).toFinalString())

    // Identity Laws
    // A ∧ T = A
    println(simplifyIdentityLaws(And(a, Literal(true))).toFinalString())
    // A ∨ F = A
    println(simplifyIdentityLaws(Or(a, Literal(false))).toFinalString())
    // A ∧ F = F
    println(simplifyIdentityLaws(And(a, Literal(false))).toFinalString())
    // A ∨ T = T
    println(simplifyIdentityLaws(Or(a, Literal(true))).toFinalString())

    // Distributive Laws
    // A ∧ (B ∨ C) = (A ∧ B) ∨ (A ∧ C)
    println(simplifyDistributiveLaws(And(a, Or(b, c))).toFinalString())
    // A ∨ (B ∧ C) = (A ∨ B) ∧ (A ∨ C)
    println(simplifyDistributiveLaws(Or(a, And(b, c))).toFinalString())
    // (A ∧ B) ∨ (A ∧ C) = A ∧ (B ∨ C)
    println(simplifyDistributiveLaws(Or(And(a, b), And(a, c))).toFinalString())
    // (A ∨ B) ∧ (A ∨ C) = A ∨ (B ∧ C)
    println(simplifyDistributiveLaws(And(Or(a, b), Or(a, c))).toFinalString())
}