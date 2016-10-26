package com.netflix.java.refactor.ast

import java.lang.IllegalStateException

class PrintVisitor : AstVisitor<String>("") {
    override fun reduce(r1: String, r2: String): String = r1 + r2

    fun visit(nodes: Collection<Tree>, suffixBetween: String, suffixEnd: String = ""): String {
        return nodes.foldIndexed("") { i, s, node ->
            reduce(s, visit(node) + (if(i == nodes.size - 1) suffixEnd else suffixBetween))
        }
    }

    fun visitStatements(statements: Collection<Tree>): String {
        return statements.fold("") { s, stat ->
            s + visit(stat) +
                when(stat) {
                    is Tr.Assign, is Tr.AssignOp, is Tr.Break, is Tr.Continue, is Tr.MethodInvocation -> ";"
                    is Tr.NewClass, is Tr.Return, is Tr.Throw, is Tr.Unary, is Tr.VariableDecl -> ";"
                    is Tr.Label -> ":"
                    else -> ""
                }

        }
    }

    fun visit(t: Tree?, suffix: String): String {
        return if(t is Tree)
            visit(t) + suffix
        else ""
    }

    override fun visitAnnotation(annotation: Tr.Annotation): String {
        val args = annotation.args?.let {
            it.fmt("(${visit(it.args, ",")})")
        } ?: ""
        return annotation.fmt("@${visit(annotation.annotationType)}$args")
    }

    override fun visitArrayAccess(arrayAccess: Tr.ArrayAccess): String {
        val dimension = arrayAccess.dimension.fmt("[${visit(arrayAccess.dimension.index)}]")
        return arrayAccess.fmt("${visit(arrayAccess.indexed)}$dimension")
    }

    override fun visitAssign(assign: Tr.Assign): String {
        return assign.fmt("${visit(assign.variable)}=${visit(assign.assignment)}")
    }

    override fun visitAssignOp(assign: Tr.AssignOp): String {
        val keyword = when(assign.operator) {
            is Tr.AssignOp.Operator.Addition -> "+="
            is Tr.AssignOp.Operator.Subtraction -> "-="
            is Tr.AssignOp.Operator.Multiplication -> "*="
            is Tr.AssignOp.Operator.Division -> "/="
            is Tr.AssignOp.Operator.Modulo -> "%="
            is Tr.AssignOp.Operator.BitAnd -> "&="
            is Tr.AssignOp.Operator.BitOr -> "|="
            is Tr.AssignOp.Operator.BitXor -> "^="
            is Tr.AssignOp.Operator.LeftShift -> "<<="
            is Tr.AssignOp.Operator.RightShift -> ">>="
            is Tr.AssignOp.Operator.UnsignedRightShift -> ">>>="
        }

        return assign.fmt("${visit(assign.variable)}${assign.operator.fmt(keyword)}${visit(assign.assignment)}")
    }

    override fun visitBinary(binary: Tr.Binary): String {
        val keyword = when(binary.operator) {
            is Tr.Binary.Operator.Addition -> "+"
            is Tr.Binary.Operator.Subtraction -> "-"
            is Tr.Binary.Operator.Multiplication -> "*"
            is Tr.Binary.Operator.Division -> "/"
            is Tr.Binary.Operator.Modulo -> "%"
            is Tr.Binary.Operator.LessThan -> "<"
            is Tr.Binary.Operator.GreaterThan -> ">"
            is Tr.Binary.Operator.LessThanOrEqual -> "<="
            is Tr.Binary.Operator.GreaterThanOrEqual -> ">="
            is Tr.Binary.Operator.Equal -> "=="
            is Tr.Binary.Operator.NotEqual -> "!="
            is Tr.Binary.Operator.BitAnd -> "&"
            is Tr.Binary.Operator.BitOr -> "|"
            is Tr.Binary.Operator.BitXor -> "^"
            is Tr.Binary.Operator.LeftShift -> "<<"
            is Tr.Binary.Operator.RightShift -> ">>"
            is Tr.Binary.Operator.UnsignedRightShift -> ">>>"
            is Tr.Binary.Operator.Or -> "||"
            is Tr.Binary.Operator.And -> "&&"
        }

        return binary.fmt("${visit(binary.left)}${binary.operator.fmt(keyword)}${visit(binary.right)}")
    }

    override fun visitBlock(block: Tr.Block<Tree>): String {
        return block.fmt("{${visitStatements(block.statements)}${block.endOfBlockSuffix}}")
    }

    override fun visitBreak(breakStatement: Tr.Break): String {
        return breakStatement.fmt("break${visit(breakStatement.label)}")
    }

    override fun visitCase(case: Tr.Case): String {
        return case.fmt("${visit(case.pattern)}:${visitStatements(case.statements)}")
    }

    override fun visitCatch(catch: Tr.Catch): String {
        return catch.fmt("catch${visit(catch.param)}${visit(catch.body)}")
    }

    override fun visitClassDecl(classDecl: Tr.ClassDecl): String {
        val typeParams = classDecl.typeParams?.let { it.fmt("<${visit(it.params)}>") } ?: ""

        val modifiers = classDecl.modifiers.fold("") { s, mod ->
            s + mod.fmt(when(mod) {
                is Tr.TypeModifier.Public -> "public"
                is Tr.TypeModifier.Protected -> "protected"
                is Tr.TypeModifier.Private -> "private"
                is Tr.TypeModifier.Abstract -> "abstract"
                is Tr.TypeModifier.Static -> "static"
                is Tr.TypeModifier.Final -> "final"
            })
        }

        return classDecl.fmt("${visit(classDecl.annotations)}${modifiers}class${visit(classDecl.name)}$typeParams" +
                "${visit(classDecl.extends)}${visit(classDecl.implements)}${visit(classDecl.body)}")
    }

    override fun visitCompilationUnit(cu: Tr.CompilationUnit): String {
        return cu.fmt("${visit(cu.packageDecl, ";")}${visit(cu.imports, ";", ";")}${visit(cu.typeDecls)}")
    }

    override fun visitContinue(continueStatement: Tr.Continue): String {
        return continueStatement.fmt("continue${visit(continueStatement.label)}")
    }

    override fun visitDoWhileLoop(doWhileLoop: Tr.DoWhileLoop): String {
        return doWhileLoop.fmt("do${visit(doWhileLoop.body)}while${visit(doWhileLoop.condition)}")
    }

    override fun visitEmpty(empty: Tr.Empty): String = empty.fmt("")

    override fun visitEnumValue(enum: Tr.EnumValue): String {
        val initializer = if(enum.initializer != null) {
            enum.initializer.fmt("(${visit(enum.initializer.args, ",")})")
        } else ""

        return enum.fmt("${visit(enum.name)}$initializer")
    }

    override fun visitEnumClass(enumClass: Tr.EnumClass): String {
        val modifiers = enumClass.modifiers.fold("") { s, mod -> s + mod.fmt(when(mod) {
            is Tr.TypeModifier.Public -> "public"
            is Tr.TypeModifier.Protected -> "protected"
            is Tr.TypeModifier.Private -> "private"
            is Tr.TypeModifier.Abstract -> "abstract"
            is Tr.TypeModifier.Static -> "static"
            is Tr.TypeModifier.Final -> "final"
        }) }

        val body = enumClass.body.let {
            val values = visit(enumClass.values(), ",", if(enumClass.members().isNotEmpty()) ";" else "")
            it.fmt("{$values${visitStatements(enumClass.members())}${it.endOfBlockSuffix}}")
        }

        return enumClass.fmt("${visit(enumClass.annotations)}${modifiers}enum${visit(enumClass.name)}" +
                "${visit(enumClass.implements)}$body")
    }

    override fun visitFieldAccess(field: Tr.FieldAccess): String {
        return field.fmt("${visit(field.target)}.${visit(field.name)}")
    }

    override fun visitForLoop(forLoop: Tr.ForLoop): String {
        val expr = forLoop.control.let { it.fmt("(${visit(it.init)};${visit(it.condition)};${visit(it.update)})") }
        return forLoop.fmt("for$expr${visit(forLoop.body)}")
    }

    override fun visitForEachLoop(forEachLoop: Tr.ForEachLoop): String {
        val control = forEachLoop.control.let { it.fmt("(${visit(it.variable)}:${visit(it.iterable)})") }
        return forEachLoop.fmt("for$control${visit(forEachLoop.body)}")
    }

    override fun visitIdentifier(ident: Tr.Ident): String {
        return ident.fmt(ident.name)
    }

    override fun visitImport(import: Tr.Import): String {
        return if (import.static)
            import.fmt("import static${visit(import.qualid)}")
        else
            import.fmt("import${visit(import.qualid)}")
    }

    override fun visitInstanceOf(instanceOf: Tr.InstanceOf): String {
        return instanceOf.fmt("${visit(instanceOf.expr)}instanceof${visit(instanceOf.clazz)}")
    }

    override fun visitLabel(label: Tr.Label): String {
        return label.fmt("${visit(label.label)}:${visit(label.statement)}")
    }

    override fun visitLambda(lambda: Tr.Lambda): String {
        return lambda.fmt("(${visit(lambda.params)})${lambda.arrow.fmt("->")}${visit(lambda.body)}")
    }

    override fun visitLiteral(literal: Tr.Literal): String {
        val v = literal.value
        return literal.fmt(when(literal.typeTag) {
            Type.Tag.Boolean -> when(v) {
                0 -> "false"
                1 -> "true"
                else -> throw IllegalStateException("Boolean has unexpected value $v")
            }
            Type.Tag.Byte -> v.toString()
            Type.Tag.Char -> v.toString()
            Type.Tag.Double -> "${v}d"
            Type.Tag.Float -> "${v}f"
            Type.Tag.Int -> v.toString()
            Type.Tag.Long -> "${v}L"
            Type.Tag.Short -> v.toString()
            Type.Tag.Void -> v.toString()
            Type.Tag.String -> "\"$v\""
            Type.Tag.None -> ""
            Type.Tag.Wildcard -> "*"
            Type.Tag.Null -> "null"
        })
    }

    override fun visitMethod(method: Tr.MethodDecl): String {
        val modifiers = method.modifiers.fold("") { s, mod -> s + mod.fmt(when(mod) {
            is Tr.MethodDecl.Modifier.Public -> "public"
            is Tr.MethodDecl.Modifier.Protected -> "protected"
            is Tr.MethodDecl.Modifier.Private -> "private"
            is Tr.MethodDecl.Modifier.Abstract -> "abstract"
            is Tr.MethodDecl.Modifier.Static -> "static"
            is Tr.MethodDecl.Modifier.Final -> "final"
        }) }

        val typeParams = method.typeParameters?.let { it.fmt("<${visit(it.params, ",")}>") } ?: ""
        val params = method.params.fmt("(${visit(method.params.params, ",")}") + ")"

        return method.fmt("${visit(method.annotations)}$modifiers$typeParams${visit(method.returnTypeExpr)}${visit(method.name)}$params${visit(method.throws)}${visit(method.body)}")
    }

    override fun visitMethodInvocation(meth: Tr.MethodInvocation): String {
        val args = meth.args.fmt("(${visit(meth.args.args, ",")})")
        val typeParams = meth.typeParameters?.let { it.fmt("<${visit(it.params, ",")}>") } ?: ""
        val selectSeparator = if(meth.select != null) "." else ""
        return meth.fmt("${visit(meth.select)}$selectSeparator$typeParams${visit(meth.name)}$args")
    }

    override fun visitNewArray(newArray: Tr.NewArray): String {
        val dimensions = newArray.dimensions.fold("") { s, dim -> s + dim.fmt("[${visit(dim.size)}]") }
        val init = if(newArray.initializer != null) {
            newArray.fmt("{${visit(newArray.initializer.elements, ",")}}")
        } else ""
        return newArray.fmt("new${visit(newArray.typeExpr)}$dimensions$init")
    }

    override fun visitNewClass(newClass: Tr.NewClass): String {
        val args = newClass.args.fmt("(${visit(newClass.args.args, ",")})")
        return newClass.fmt("new${visit(newClass.clazz)}$args${visit(newClass.classBody)}")
    }

    override fun visitPackage(pkg: Tr.Package): String {
        return pkg.fmt("package${visit(pkg.expr)}")
    }

    override fun visitParameterizedType(type: Tr.ParameterizedType): String {
        val typeParams = type.typeArguments?.let {
            it.fmt("<${visit(it.args, ",")}>")
        } ?: ""
        return type.fmt("${visit(type.clazz)}$typeParams")
    }

    override fun visitPrimitive(primitive: Tr.Primitive): String {
        return primitive.fmt(when (primitive.typeTag) {
            Type.Tag.Boolean -> "boolean"
            Type.Tag.Byte -> "byte"
            Type.Tag.Char -> "char"
            Type.Tag.Double -> "double"
            Type.Tag.Float -> "float"
            Type.Tag.Int -> "int"
            Type.Tag.Long -> "long"
            Type.Tag.Short -> "short"
            Type.Tag.Void -> "void"
            Type.Tag.String -> "String"
            Type.Tag.Wildcard -> "*"
            Type.Tag.None -> throw IllegalStateException("Unable to print None primitive")
            Type.Tag.Null -> throw IllegalStateException("Unable to print Null primitive")
        })
    }

    override fun visitParentheses(parens: Tr.Parentheses): String {
        return parens.fmt("(${visit(parens.expr)})")
    }

    override fun visitReturn(retrn: Tr.Return): String {
        return retrn.fmt("return${visit(retrn.expr)}")
    }

    override fun visitSwitch(switch: Tr.Switch): String {
        return switch.fmt("switch${visit(switch.selector)}${visit(switch.cases)}")
    }

    override fun visitSynchronized(synch: Tr.Synchronized): String {
        return synch.fmt("synchronized${visit(synch.lock)}${visit(synch.body)}")
    }

    override fun visitThrow(thrown: Tr.Throw): String {
        return "throw${visit(thrown.exception)}"
    }

    override fun visitTry(tryable: Tr.Try): String {
        val resources = tryable.resources?.let {
            it.fmt("(${visit(it.decls, ";")})")
        } ?: ""

        val finally = tryable.finally?.let {
            it.fmt("finally${visit(it.block)}")
        } ?: ""

        return tryable.fmt("try$resources${visit(tryable.body)}${visit(tryable.catches)}$finally")
    }

    override fun visitTypeParameter(typeParameter: Tr.TypeParameter): String {
        val bounds = if(typeParameter.bounds.isNotEmpty()) {
            "extends${visit(typeParameter.bounds, "&")}"
        } else ""

        return typeParameter.fmt("${visit(typeParameter.annotations, "")}${visit(typeParameter.name)}$bounds")
    }

    override fun visitUnary(unary: Tr.Unary): String {
        return unary.fmt(when (unary.operator) {
            is Tr.Unary.Operator.PreIncrement -> "++${visit(unary.expr)}"
            is Tr.Unary.Operator.PreDecrement -> "--${visit(unary.expr)}"
            is Tr.Unary.Operator.PostIncrement -> "${visit(unary.expr)}++"
            is Tr.Unary.Operator.PostDecrement -> "${visit(unary.expr)}--"
            is Tr.Unary.Operator.Positive -> "+${visit(unary.expr)}"
            is Tr.Unary.Operator.Negative -> "-${visit(unary.expr)}"
            is Tr.Unary.Operator.Complement -> "~${visit(unary.expr)}"
            is Tr.Unary.Operator.Not -> "!${visit(unary.expr)}"
        })
    }

    override fun visitVariable(variable: Tr.VariableDecl): String {
        val modifiers = variable.modifiers.fold("") { s, mod -> s + mod.fmt(when(mod) {
            is Tr.VariableDecl.Modifier.Public -> "public"
            is Tr.VariableDecl.Modifier.Protected -> "protected"
            is Tr.VariableDecl.Modifier.Private -> "private"
            is Tr.VariableDecl.Modifier.Abstract -> "abstract"
            is Tr.VariableDecl.Modifier.Static -> "static"
            is Tr.VariableDecl.Modifier.Final -> "final"
            is Tr.VariableDecl.Modifier.Transient -> "transient"
            is Tr.VariableDecl.Modifier.Volatile -> "volatile"
        }) }

        val init = when(variable.initializer) {
            is Expression -> "=${visit(variable.initializer)}"
            else -> ""
        }

        val varargs = when(variable.varArgs) {
            is Tr.VariableDecl.Varargs -> variable.varArgs.fmt("...")
            else -> ""
        }

        fun visitDims(dims: List<Tr.VariableDecl.Dimension>): String =
            dims.fold("") { s, d -> s + d.fmt("[${visit(d.whitespace)}]") }

        return variable.fmt("${visit(variable.annotations)}$modifiers" +
                "${visit(variable.varType)}${visitDims(variable.dimensionsBeforeName)}" +
                "$varargs${visit(variable.name)}${visitDims(variable.dimensionsAfterName)}$init")
    }

    override fun visitWhileLoop(whileLoop: Tr.WhileLoop): String {
        return whileLoop.fmt("while${visit(whileLoop.condition)}${visit(whileLoop.body)}")
    }

    private fun Tree?.fmt(code: String?): String {
        return if (this == null || code == null)
            ""
        else {
            //println("${this.javaClass.simpleName} = [" + formatting.prefix() + "," + code + "," + formatting.suffix() + "]")
            formatting.prefix() + code + formatting.suffix()
        }
    }

    private fun Formatting.prefix(): String = when(this) {
        is Formatting.Reified -> prefix
        is Formatting.Infer -> ""
        is Formatting.None -> ""
    }

    private fun Formatting.suffix(): String = when(this) {
        is Formatting.Reified -> suffix
        is Formatting.Infer -> ""
        is Formatting.None -> ""
    }
}