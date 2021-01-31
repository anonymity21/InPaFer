package instrument.visitor;

import config.Constant;
import instrument.gen.GenStatement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.jdt.core.dom.*;
import util.FileIO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used for instrumentLineNumber for each {@code Statement}
 *
 * @author Jiajun
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StatementInstrumentVisitor extends TraversalVisitor {

    private final static String __name__ = "@StatementInstrumentVisitor ";

    private String _clazzName = "";
    private String _clazzFileName = "";
    private CompilationUnit _cu;
    private String intrumentMethod = "";
    private Set<String> instrumentSet = new LinkedHashSet<>();
    private String writeFile = "";
    //private String _methodFlag = Constant.INSTRUMENT_SOURCE;
    //private Method _method = null;


    @Override
    public boolean visit(CompilationUnit node) {
        _cu = node;
        if (node.getPackage().getName() != null && node.getPackage().getName().getFullyQualifiedName().equals("auxiliary")) {
            return false;
        }
        _clazzName = node.getPackage().getName().getFullyQualifiedName();
        for (Object object : node.types()) {
            if (object instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) object;
                if (Modifier.isPublic(type.getModifiers())) {
                    _clazzName += "." + type.getName().getFullyQualifiedName();
                    _clazzFileName = _clazzName;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {

        if (!Modifier.isPublic(node.getModifiers())) {
            if (_clazzFileName == "") {
                _clazzFileName = _clazzName;
                _clazzName = _clazzFileName + "." + node.getName().getFullyQualifiedName();
            } else {
                _clazzName = _clazzFileName + "$" + node.getName().getFullyQualifiedName();
            }

        }
        return true;

    }

    @Override
    public boolean visit(MethodDeclaration node) {

        // filter those methods that defined in anonymous classes
        ASTNode parent = node.getParent();
        while (parent != null && !(parent instanceof TypeDeclaration)) {
            if (parent instanceof ClassInstanceCreation) {
                return true;
            }
            parent = parent.getParent();
        }

        if (node.getBody() != null) {
            Block body = node.getBody();
            StringBuffer buffer = new StringBuffer(_clazzName + "#");

            String retType = "?";
            if (node.getReturnType2() != null) {
                retType = node.getReturnType2().toString();
            }
            StringBuffer param = new StringBuffer("?");
            for (Object object : node.parameters()) {
                if (!(object instanceof SingleVariableDeclaration)) {
                    param.append(",?");
                } else {
                    SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;
                    param.append("," + singleVariableDeclaration.getType().toString());
                }
            }
            // add method return type
            buffer.append(retType + "#");
            // add method name
            buffer.append(node.getName().getFullyQualifiedName() + "#");
            // add method params, NOTE: the first parameter starts at index 1.
            buffer.append(param);
            String message = buffer.toString();
            if (instrumentSet.contains(message)) {
                Block methodBody = node.getBody();

                if (methodBody == null) {
                    return true;
                }
                AST ast = AST.newAST(AST.JLS8);
                List<ASTNode> blockStatement = new ArrayList<>();

                int i = 0;
                if (methodBody.statements().size() > 0) {
                    ASTNode astNode = (ASTNode) methodBody.statements().get(0);
                    if (astNode instanceof ConstructorInvocation || astNode instanceof SuperConstructorInvocation) {
                        i = 1;
                        blockStatement.add(astNode);
                    }
                }
                Statement startGuard = GenStatement.genDumpLine(writeFile, message + Constant.INSTRUMENT_START, 0);
                if(! (methodBody.statements().get(0) instanceof SuperConstructorInvocation)) {
                    blockStatement.add(startGuard);
                }

                // Statement endGuard = GenStatement.genDumpLine(writeFile, message + Constant.INSTRUMENT_END, 0 );

                for (; i < methodBody.statements().size(); i++) {
                    ASTNode astNode = (ASTNode) methodBody.statements().get(i);
                    if (astNode instanceof Statement) {
                        Statement statement = (Statement) astNode;
                        blockStatement.addAll(process(statement, message));
                    } else {
                        blockStatement.add(ASTNode.copySubtree(ast, (ASTNode) astNode));
                    }
                }
                methodBody.statements().clear();
                for (ASTNode statement : blockStatement) {
                    methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), statement));
                }

            }
        }

        return true;
    }

    private List<Statement> process(Statement statement, String message) {

        List<Statement> result = new ArrayList<>();

        if (statement instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statement;

            int lineNumber = _cu.getLineNumber(ifStatement.getExpression().getStartPosition());
            Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);
            result.add(insert);

            Statement thenBody = ifStatement.getThenStatement();
            if (thenBody != null) {
                Block thenBlock = null;
                if (thenBody instanceof Block) {
                    thenBlock = (Block) thenBody;
                } else {
                    AST ast = AST.newAST(AST.JLS8);
                    thenBlock = ast.newBlock();
                    thenBlock.statements().add(ASTNode.copySubtree(thenBlock.getAST(), thenBody));
                }

                Block newThenBlock = processBlock(thenBlock, null, message);
                ifStatement.setThenStatement((Statement) ASTNode.copySubtree(ifStatement.getAST(), newThenBlock));
            }

            Statement elseBody = ifStatement.getElseStatement();
            if (elseBody != null) {
                Block elseBlock = null;
                if (elseBody instanceof Block) {
                    elseBlock = (Block) elseBody;
                } else {
                    AST ast = AST.newAST(AST.JLS8);
                    elseBlock = ast.newBlock();
                    elseBlock.statements().add(ASTNode.copySubtree(elseBlock.getAST(), elseBody));
                }
                Block newElseBlock = processBlock(elseBlock, null, message);
                ifStatement.setElseStatement((Statement) ASTNode.copySubtree(ifStatement.getAST(), newElseBlock));
            }
            result.add(ifStatement);
        } else if (statement instanceof WhileStatement) {

            WhileStatement whileStatement = (WhileStatement) statement;
            Statement whilebody = whileStatement.getBody();
            if (whilebody != null) {
                Block whileBlock = null;
                if (whilebody instanceof Block) {
                    whileBlock = (Block) whilebody;
                } else {
                    AST ast = AST.newAST(AST.JLS8);
                    whileBlock = ast.newBlock();
                    whileBlock.statements().add(ASTNode.copySubtree(whileBlock.getAST(), whilebody));
                }

                int lineNumber = _cu.getLineNumber(whileStatement.getExpression().getStartPosition());
                Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);
                Block newWhileBlock = processBlock(whileBlock, insert, message);
                whileStatement.setBody((Statement) ASTNode.copySubtree(whileStatement.getAST(), newWhileBlock));
            }

            result.add(whileStatement);
        } else if (statement instanceof ForStatement) {

            ForStatement forStatement = (ForStatement) statement;
            Statement forBody = forStatement.getBody();
            if (forBody != null) {
                Block forBlock = null;
                if (forBody instanceof Block) {
                    forBlock = (Block) forBody;
                } else {
                    AST ast = AST.newAST(AST.JLS8);
                    forBlock = ast.newBlock();
                    forBlock.statements().add(ASTNode.copySubtree(forBlock.getAST(), forBody));
                }

                int position = forStatement.getStartPosition();
                if (forStatement.getExpression() != null) {
                    position = forStatement.getExpression().getStartPosition();
                } else if (forStatement.initializers() != null && forStatement.initializers().size() > 0) {
                    position = ((ASTNode) forStatement.initializers().get(0)).getStartPosition();
                } else if (forStatement.updaters() != null && forStatement.updaters().size() > 0) {
                    position = ((ASTNode) forStatement.updaters().get(0)).getStartPosition();
                }
                int lineNumber = _cu.getLineNumber(position);
                Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);
                Block newForBlock = processBlock(forBlock, insert, message);
                forStatement.setBody((Statement) ASTNode.copySubtree(forStatement.getAST(), newForBlock));
            }

            result.add(forStatement);
        } else if (statement instanceof DoStatement) {

            DoStatement doStatement = (DoStatement) statement;
            Statement doBody = doStatement.getBody();
            if (doBody != null) {
                Block doBlock = null;
                if (doBody instanceof Block) {
                    doBlock = (Block) doBody;
                } else {
                    AST ast = AST.newAST(AST.JLS8);
                    doBlock = ast.newBlock();
                    doBlock.statements().add(ASTNode.copySubtree(doBlock.getAST(), doBody));
                }

                Block newDoBlock = processBlock(doBlock, null, message);
                ASTNode lastStatement = (ASTNode) newDoBlock.statements().get(newDoBlock.statements().size() - 1);
                if (!(lastStatement instanceof BreakStatement || lastStatement instanceof ReturnStatement)) {
                    int lineNumber = _cu.getLineNumber(doStatement.getExpression().getStartPosition());
                    Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);
                    newDoBlock.statements().add(ASTNode.copySubtree(newDoBlock.getAST(), insert));
                }

                doStatement.setBody((Statement) ASTNode.copySubtree(doStatement.getAST(), newDoBlock));
            }

            result.add(doStatement);
        } else if (statement instanceof Block) {
            Block block = (Block) statement;
            Block newBlock = processBlock(block, null, message);
            result.add(newBlock);
        } else if (statement instanceof EnhancedForStatement) {

            EnhancedForStatement enhancedForStatement = (EnhancedForStatement) statement;
            Statement enhancedBody = enhancedForStatement.getBody();
            if (enhancedBody != null) {
                Block enhancedBlock = null;
                if (enhancedBody instanceof Block) {
                    enhancedBlock = (Block) enhancedBody;
                } else {
                    AST ast = AST.newAST(AST.JLS8);
                    enhancedBlock = ast.newBlock();
                    enhancedBlock.statements().add(ASTNode.copySubtree(enhancedBlock.getAST(), enhancedBody));
                }

                int lineNumber = _cu.getLineNumber(enhancedForStatement.getExpression().getStartPosition());
                Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);
                Block newEnhancedBlock = processBlock(enhancedBlock, insert, message);
                enhancedForStatement
                        .setBody((Statement) ASTNode.copySubtree(enhancedForStatement.getAST(), newEnhancedBlock));
            }

            result.add(enhancedForStatement);
        } else if (statement instanceof SwitchStatement) {

            SwitchStatement switchStatement = (SwitchStatement) statement;
            List<ASTNode> statements = new ArrayList<>();
            AST ast = AST.newAST(AST.JLS8);
            for (Object object : switchStatement.statements()) {
                ASTNode astNode = (ASTNode) object;
                statements.add(ASTNode.copySubtree(ast, astNode));
            }

            switchStatement.statements().clear();

            for (ASTNode astNode : statements) {
                if (astNode instanceof Statement) {
                    Statement s = (Statement) astNode;
                    for (Statement statement2 : process(s, message)) {
                        switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), statement2));
                    }
                } else {
                    switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), astNode));
                }
            }

            int lineNumber = _cu.getLineNumber(switchStatement.getExpression().getStartPosition());
            Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);

            result.add(insert);
            result.add(switchStatement);
        } else if (statement instanceof TryStatement) {

            TryStatement tryStatement = (TryStatement) statement;

            Block tryBlock = tryStatement.getBody();
            if (tryBlock != null) {
                Block newTryBlock = processBlock(tryBlock, null, message);
                tryStatement.setBody((Block) ASTNode.copySubtree(tryStatement.getAST(), newTryBlock));
            }

            List catchList = tryStatement.catchClauses();
            if (catchList != null) {
                for (Object object : catchList) {
                    if (object instanceof CatchClause) {
                        CatchClause catchClause = (CatchClause) object;
                        Block catchBlock = catchClause.getBody();
                        Block newCatchBlock = processBlock(catchBlock, null, message);
                        catchClause.setBody((Block) ASTNode.copySubtree(catchClause.getAST(), newCatchBlock));
                    }
                }
            }

            Block finallyBlock = tryStatement.getFinally();
            if (finallyBlock != null) {
                Block newFinallyBlock = processBlock(finallyBlock, null, message);
                tryStatement.setFinally((Block) ASTNode.copySubtree(tryStatement.getAST(), newFinallyBlock));
            }

            result.add(tryStatement);
        } else {
            int lineNumber = _cu.getLineNumber(statement.getStartPosition());
            Statement copy = (Statement) ASTNode.copySubtree(AST.newAST(AST.JLS8), statement);
            Statement insert = GenStatement.genDumpLine(writeFile, message, lineNumber);

            if (statement instanceof ConstructorInvocation) {
                result.add(copy);
                result.add(insert);
            } else if (statement instanceof ContinueStatement || statement instanceof BreakStatement
                    || statement instanceof ReturnStatement || statement instanceof ThrowStatement
                    || statement instanceof AssertStatement || statement instanceof ExpressionStatement
                    || statement instanceof VariableDeclarationStatement) {
                result.add(insert);
                result.add(copy);

            } else if (statement instanceof LabeledStatement) {
                result.add(copy);
            } else if (statement instanceof SynchronizedStatement) {
                result.add(copy);
            } else {
                result.add(copy);
            }
        }

        return result;
    }

    private Block processBlock(Block block, Statement insert, String message) {
        Block newBlock = AST.newAST(AST.JLS8).newBlock();
        if (block == null) {
            return newBlock;
        }
        if (insert != null) {
            newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), insert));
        }
        for (Object object : block.statements()) {
            if (object instanceof Statement) {
                Statement statement = (Statement) object;
                List<Statement> newStatements = process(statement, message);
                for (Statement newStatement : newStatements) {
                    newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), newStatement));
                }
            } else {

            }
        }
        return newBlock;
    }

    public static void main(String[] args) {
        String filePath = "/Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Lang/Lang_27_buggy/src/main/java/org/apache/commons/lang3/math/NumberUtils.java";
        StatementInstrumentVisitor statementInstrumentVisitor = new StatementInstrumentVisitor();
        statementInstrumentVisitor.setIntrumentMethod("org.apache.commons.lang3.math.NumberUtils#java.lang.Number#createNumber#?,java.lang.String");
        //statementInstrumentVisitor.setWriteFile();
        CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(filePath),
                ASTParser.K_COMPILATION_UNIT);
        compilationUnit.accept(statementInstrumentVisitor);
        System.out.println(compilationUnit.toString());
        FileIO.writeStringToFile(filePath, compilationUnit.toString());


    }

}
