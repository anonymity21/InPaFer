package instrument.visitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import config.Constant;
import instrument.gen.GenStatement;
import instrument.gen.InsertVariableGenerator;
import util.FileIO;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StateCollectionInstrumentVisitor extends TraversalVisitor {

    private final static String __name__ = "@StateCollectInstrumentVisitor ";

    private String _clazzName = "";
    private String _clazzFileName = "";
    private CompilationUnit _cu;
    private Set<String> intrumentMethodSet;
    private String writeFile = "";
    //private boolean usedReturn = false;
    //private boolean usedException = false;


    @Override
    public boolean visit(CompilationUnit node) {
        _cu = node;
        _clazzName = node.getPackage().getName().getFullyQualifiedName();
        if (_clazzName.equals("auxiliary")) {
            return false;
        }
        for (Object object : node.types()) {
            if (object instanceof TypeDeclaration) {
                TypeDeclaration type = (TypeDeclaration) object;
                if (Modifier.isPublic(type.getModifiers())) {
                    _clazzName += Constant.INSTRUMENT_DOT_SEPARATOR + type.getName().getFullyQualifiedName();
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
            if(_clazzFileName == "") {
                _clazzFileName = _clazzName;
                _clazzName = _clazzFileName + "." + node.getName().getFullyQualifiedName();
            }else {
                _clazzName = _clazzFileName + "$" + node.getName().getFullyQualifiedName();
            }
        }
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        // filter those methods that defined in anonymous classes
        // refresh usedReturn and usedException for every method
        //usedReturn = false;
        //usedException = false;
        ASTNode parent = node.getParent();
        while (parent != null && !(parent instanceof TypeDeclaration)) {
            if (parent instanceof ClassInstanceCreation) {
                return true;
            }
            parent = parent.getParent();
        }

        if (node.getBody() != null) {

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
            if (intrumentMethodSet.contains(message)) {
                Block methodBody = node.getBody();
                if (methodBody == null) {
                    return true;
                }
                List<ASTNode> blockStatement = new ArrayList<>();
                AST ast = AST.newAST(AST.JLS8);
                int i = 0;
                if (methodBody.statements().size() > 0) {
                    ASTNode astNode = (ASTNode) methodBody.statements().get(0);
                    if (astNode instanceof ConstructorInvocation || astNode instanceof SuperConstructorInvocation) {
                        i = 1;
                        blockStatement.add(astNode);
                    }
                }

                Statement startGuard = GenStatement.genDumpLine(writeFile, message + Constant.INSTRUMENT_START, 0);
                blockStatement.add(startGuard);

                InsertVariableGenerator genVariablePrinter = new InsertVariableGenerator(_cu, node, writeFile);
                blockStatement.addAll(genVariablePrinter.generate());

                Statement endGuard = GenStatement.genDumpLine(writeFile, message + Constant.INSTRUMENT_END, 0);

                List<ASTNode> tmpNodeList = new ArrayList<>();

                tmpNodeList.addAll(genVariablePrinter.generate());
                tmpNodeList.add(endGuard);

                for (; i < methodBody.statements().size(); i++) {
                    ASTNode astNode = (ASTNode) methodBody.statements().get(i);
                    if (astNode instanceof Statement) {
                        blockStatement.addAll(processMethodBody((Statement) astNode, tmpNodeList, node.getReturnType2()));
                    } else {
                        blockStatement.add(ASTNode.copySubtree(ast, astNode));
                    }
                }
                ASTNode lastStatement = blockStatement.get(blockStatement.size() - 1);
                // hard code for math_84, because of a extra instrument statement in last statement
                if(! message.equals("org.apache.commons.math.optimization.direct.MultiDirectional#void#iterateSimplex#?,Comparator<RealPointValuePair>")) {
                    if ((node.getReturnType2() == null) || (node.getReturnType2().toString().equals("void") && !(lastStatement instanceof ReturnStatement || lastStatement instanceof ThrowStatement))) {
                        for (ASTNode insert : tmpNodeList) {
                            blockStatement.add(ASTNode.copySubtree(ast, insert));
                        }
                    }
                }

//		}
                methodBody.statements().clear();
                for (ASTNode statement : blockStatement) {
                    methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), statement));
                }

            }

        }

        return true;
    }

    public List<Statement> processMethodBody(Statement statement, List<ASTNode> insertedNodes, Type reType) {
        List<Statement> result = new ArrayList<>();
        if (statement instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statement;
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

                Block newThenBlock = processBlock(thenBlock, insertedNodes, reType);
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
                Block newElseBlock = processBlock(elseBlock, insertedNodes, reType);
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

                Block newWhileBlock = processBlock(whileBlock, insertedNodes, reType);
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
                Block newForBlock = processBlock(forBlock, insertedNodes, reType);
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

                Block newDoBlock = processBlock(doBlock, insertedNodes, reType);
                doStatement.setBody((Statement) ASTNode.copySubtree(doStatement.getAST(), newDoBlock));
            }

            result.add(doStatement);
        } else if (statement instanceof Block) {
            Block block = (Block) statement;
            Block newBlock = processBlock(block, insertedNodes, reType);
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
                Block newEnhancedBlock = processBlock(enhancedBlock, insertedNodes, reType);
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
                    for (Statement statement2 : processMethodBody(s, insertedNodes, reType)) {
                        switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), statement2));
                    }
                } else {
                    switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), astNode));
                }
            }
            result.add(switchStatement);
        } else if (statement instanceof TryStatement) {

            TryStatement tryStatement = (TryStatement) statement;

            Block tryBlock = tryStatement.getBody();
            if (tryBlock != null) {
                Block newTryBlock = processBlock(tryBlock, insertedNodes, reType);
                tryStatement.setBody((Block) ASTNode.copySubtree(tryStatement.getAST(), newTryBlock));
            }

            List catchList = tryStatement.catchClauses();
            if (catchList != null) {
                for (Object object : catchList) {
                    if (object instanceof CatchClause) {
                        CatchClause catchClause = (CatchClause) object;
                        Block catchBlock = catchClause.getBody();
                        Block newCatchBlock = processBlock(catchBlock, insertedNodes, reType);
                        catchClause.setBody((Block) ASTNode.copySubtree(catchClause.getAST(), newCatchBlock));
                    }
                }
            }

            Block finallyBlock = tryStatement.getFinally();
            if (finallyBlock != null) {
                Block newFinallyBlock = processBlock(finallyBlock, insertedNodes, reType);
                tryStatement.setFinally((Block) ASTNode.copySubtree(tryStatement.getAST(), newFinallyBlock));
            }

            result.add(tryStatement);
        } else {
            AST ast = AST.newAST(AST.JLS8);
            if (statement instanceof ReturnStatement) {
                ReturnStatement returnStatement = (ReturnStatement) statement;
                Expression expression = returnStatement.getExpression();
                if (expression != null) {
                    if( expression instanceof NullLiteral) {
                        int index = 0;
                        for (; index < insertedNodes.size() - 1; index++) {
                            result.add((Statement) ASTNode.copySubtree(ast, insertedNodes.get(index)));
                        }
                        ReturnStatement reStatement = (ReturnStatement) GenStatement.genRenWriteStatement(writeFile, expression, reType);
                        result.add((Statement) ASTNode.copySubtree(ast, insertedNodes.get(index)));
                        //result.add((Statement) ASTNode.copySubtree(ast, statement));
                        result.add(reStatement);

                    }else {
                        int index = 0;
                        for(; index < insertedNodes.size() - 1; index++){
                            result.add((Statement) ASTNode.copySubtree(ast, insertedNodes.get(index)));
                        }
                        //insert a statement for printing the return value
                        //result.add(GenStatement.genVariableDumpMethodInvation(message, "wo_shi_lin_shi_de"));
                        if(reType == null) {
                            log.error("@StateCollectionVisitor: return type is null for statement: " + statement);
                            System.exit(0);
                        }
                        ReturnStatement reStatement = (ReturnStatement) GenStatement.genRenWriteStatement(writeFile, expression, reType);
                        result.add((Statement) ASTNode.copySubtree(ast, insertedNodes.get(index)));
                        result.add(reStatement);
                    }
                } else {
                    for (ASTNode insert : insertedNodes) {
                        result.add((Statement) ASTNode.copySubtree(ast, insert));
                    }
                    result.add((Statement) ASTNode.copySubtree(ast, statement));
                }
            } else if (statement instanceof ThrowStatement) {
                ThrowStatement throwStatement = (ThrowStatement) statement;
                Expression expression = throwStatement.getExpression();
                if (expression != null) {
                    Type throwType = null;
                    if (expression instanceof ClassInstanceCreation) {
                        ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
                        throwType = (Type) ASTNode.copySubtree(ast, classInstanceCreation.getType());

                    }
                    else {
                        throwType = ast.newSimpleType(ast.newName("RuntimeException"));
                    }

                    // TODO : throwstatement;

                    RuntimeException aException = new RuntimeException("");

                    int index = 0;
                    for (; index < insertedNodes.size() - 1; index++) {
                        result.add((Statement) ASTNode.copySubtree(ast, insertedNodes.get(index)));
                    }
                    //insert a statement for printing the return value
                    // result.add(GenStatement.genVariableDumpMethodInvation(writeFile, varName));

                    result.add((Statement) ASTNode.copySubtree(ast, insertedNodes.get(index)));
                    result.add(GenStatement.genThrowWriteStatement(writeFile, expression, throwType));
                } else {
                    for (ASTNode insert : insertedNodes) {
                        result.add((Statement) ASTNode.copySubtree(ast, insert));
                    }
                    result.add((Statement) ASTNode.copySubtree(ast, statement));
                }

            } else {
                result.add((Statement) ASTNode.copySubtree(ast, statement));
            }
        }

        return result;
    }

    private Block processBlock(Block block, List<ASTNode> insertedNodes, Type reType) {
        Block newBlock = AST.newAST(AST.JLS8).newBlock();
        if (block == null) {
            return newBlock;
        }

        for (Object object : block.statements()) {
            if (object instanceof Statement) {
                Statement statement = (Statement) object;
                List<Statement> newStatements = processMethodBody(statement, insertedNodes, reType);
                for (Statement newStatement : newStatements) {
                    newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), newStatement));
                }
            } else {

            }
        }
        return newBlock;
    }

    private Statement genVariableDeclaration(AST ast, String varName, Expression expression, Type type ) {
        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        SimpleName simpleName = ast.newSimpleName(varName);
        fragment.setName(simpleName);
        Expression initializer = (Expression) ASTNode.copySubtree(ast, expression);
        fragment.setInitializer(initializer);
        VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(fragment);

        Type retType = (Type) ASTNode.copySubtree(ast, type);
        vds.setType(retType);
        return vds;

    }

    private Statement genVariableAssignment(AST ast, String varName, Expression expression) {

        SimpleName simpleName = ast.newSimpleName(varName);
        Expression assignmentExp = (Expression) ASTNode.copySubtree(ast, expression);
        Assignment newAssignment = ast.newAssignment();
        newAssignment.setLeftHandSide(simpleName);
        newAssignment.setOperator(Assignment.Operator.ASSIGN);
        newAssignment.setRightHandSide(assignmentExp);
        ExpressionStatement expressionStatement = ast.newExpressionStatement(newAssignment);
        return expressionStatement;
    }

    public static void main(String[] args) {

        String project = "Chart";
        String id = 1 + "";
        String filePath = "/Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Chart/Chart_1_buggy/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java";
        String writeFile = Constant.Record + "Chart/1/test_var";
        String intrumentMethod = "org.jfree.chart.renderer.category.AbstractCategoryItemRenderer#LegendItemCollection#getLegendItems#?";
        StateCollectionInstrumentVisitor stateCollectInstrumentVisitor = new StateCollectionInstrumentVisitor();
        //stateCollectInstrumentVisitor.setIntrumentMethod(intrumentMethod);
        stateCollectInstrumentVisitor.setWriteFile(writeFile);
        CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(filePath),
                ASTParser.K_COMPILATION_UNIT);
        compilationUnit.accept(stateCollectInstrumentVisitor);
        System.out.println(compilationUnit.toString());

    }

}

