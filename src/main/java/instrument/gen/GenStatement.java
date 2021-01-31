package instrument.gen;

import org.eclipse.jdt.core.dom.*;

public class GenStatement {

    private static AST ast = AST.newAST(AST.JLS8);

    private static Statement genPrinter(Expression writeFile, Expression expression) {
        // auxiliary.Dumper.write(writeFile,expression);
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
        methodInvocation.setName(ast.newSimpleName("write"));
        methodInvocation.arguments().add(writeFile);
        methodInvocation.arguments().add(expression);
        ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
        return expressionStatement;
    }

    /**
     * generate "System.out.println()" {@code Statement} with the arguments as given
     * {@code locMessage} and {@code line}
     *
     * @param writeFile
     * @param locMessage
     * @param line
     * @return
     */
    public static Statement genDumpLine(String writeFile, String locMessage, int line) {

        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue(locMessage + "#" + line);

        StringLiteral fileLiteral = ast.newStringLiteral();
        fileLiteral.setLiteralValue(writeFile);

        return genPrinter(fileLiteral, stringLiteral);
    }

    // public static Statement

    public static Statement genVariableDumpMethodInvation(String writeFile, String variableName) {

        // infixExpression is "this.columnCount:"+
        // auxiliary.Dumper.dump(this.columnCount)
        InfixExpression infixExpression = ast.newInfixExpression();
        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue(variableName + ": ");

        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        SimpleName simpleName = ast.newSimpleName(variableName);
        methodInvocation.arguments().add(simpleName);

        infixExpression.setLeftOperand(stringLiteral);
        infixExpression.setOperator(InfixExpression.Operator.PLUS);
        infixExpression.setRightOperand(methodInvocation);

        StringLiteral fileLiteral = ast.newStringLiteral();
        fileLiteral.setLiteralValue(writeFile);

        return genPrinter(fileLiteral, infixExpression);

    }

    public static Statement genThisFieldDumpMethodInvocation(String writeFile) {
        ThisExpression thisExpression = ast.newThisExpression();

        InfixExpression infixExpression = ast.newInfixExpression();
        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue("this: ");

        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
        methodInvocation.setName(ast.newSimpleName("dump"));
        methodInvocation.arguments().add(thisExpression);

        infixExpression.setLeftOperand(stringLiteral);
        infixExpression.setOperator(InfixExpression.Operator.PLUS);
        infixExpression.setRightOperand(methodInvocation);

        StringLiteral fileLiteral = ast.newStringLiteral();
        fileLiteral.setLiteralValue(writeFile);

        return genPrinter(fileLiteral, infixExpression);

    }

    public static Statement genReturnStatement(String varName) {
        ReturnStatement returnStatement = ast.newReturnStatement();
        SimpleName simpleName = ast.newSimpleName(varName);
        returnStatement.setExpression(simpleName);
        return returnStatement;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    public static Expression genDumperWrite(String writeFile, Expression expression) {
        StringLiteral fileLiteral = ast.newStringLiteral();
        fileLiteral.setLiteralValue(writeFile);

        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue(expression + ": ");
		
		/*infixExpression.setLeftOperand(stringLiteral);
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		infixExpression.setRightOperand((Expression) ASTNode.copySubtree(ast,expression));*/


        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
        methodInvocation.setName(ast.newSimpleName("write"));
        methodInvocation.arguments().add(fileLiteral);
        methodInvocation.arguments().add(stringLiteral);
        methodInvocation.arguments().add((Expression) ASTNode.copySubtree(ast, expression));
        return methodInvocation;
    }

    // (Number) auxiliary.Dumper.write("", createLong(numeric));
    public static Statement genRenWriteStatement(String writeFile, Expression expression, Type reType) {
       /* if (reType instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) reType;
            if (primitiveType.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) {
                reType = ast.newSimpleType(ast.newSimpleName("Double"));
            } else if (primitiveType.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
                reType = ast.newSimpleType(ast.newSimpleName("Boolean"));
            } else if (primitiveType.getPrimitiveTypeCode() == primitiveType.LONG) {
                reType = ast.newSimpleType(ast.newSimpleName("Long"));
            } else if (primitiveType.getPrimitiveTypeCode() == primitiveType.INT) {
                reType = ast.newSimpleType(ast.newSimpleName("Integer"));
            } else {
                System.out.println("@GenStatement.genRenWriteStatement");
                System.out.println("Need to process: " + primitiveType.getPrimitiveTypeCode().toString());
            }
        }*/

        CastExpression castExpr = ast.newCastExpression();
        castExpr.setType((Type) ASTNode.copySubtree(ast, reType));
        castExpr.setExpression(genDumperWrite(writeFile, expression));

        ReturnStatement returnStatement = ast.newReturnStatement();
        returnStatement.setExpression(castExpr);
        return returnStatement;
    }

    // (Number) auxiliary.Dumper.write("", createLong(numeric));
    public static Statement genThrowWriteStatement(String writeFile, Expression expression, Type reType) {
        CastExpression castExpr = ast.newCastExpression();
        castExpr.setType((Type) ASTNode.copySubtree(ast, reType));
        castExpr.setExpression(genDumperWrite(writeFile, expression));

        ThrowStatement throwStatement = ast.newThrowStatement();
        throwStatement.setExpression(castExpr);
        return throwStatement;
    }

    public static Statement genDumperWriteStatement(String writeFile, Expression expression) {
        // StringLiteral stringLiteral = ast.newStringLiteral();
        //stringLiteral.setLiteralValue(expression);
        ExpressionStatement expressionStatement = ast.newExpressionStatement(genDumperWrite(writeFile, expression));
        return expressionStatement;
    }

    public static Statement genThisFieldWriteStatement(String writeFile) {
        ThisExpression thisExpression = ast.newThisExpression();
        ExpressionStatement expressionStatement = ast.newExpressionStatement(genDumperWrite(writeFile, thisExpression));
        return expressionStatement;

    }

}
