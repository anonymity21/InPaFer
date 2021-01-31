package instrument.gen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertVariableGenerator {

    private final static String __name__ = "@InserVariablePrinter ";

    private CompilationUnit _cu = null;
    private MethodDeclaration _methodDeclaration = null;
    private String writeFile = "";
    // private String _locMessage = "";


    public List<ASTNode> generate() {
        List<ASTNode> statements = new ArrayList<>();

        int modifiers = _methodDeclaration.getModifiers();
        if (!Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers)) {
            statements.add(GenStatement.genThisFieldWriteStatement(writeFile));
        }

        // print parameter information
        List<ASTNode> params = _methodDeclaration.parameters();
        for (ASTNode param : params) {
            if (param instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) param;
                SimpleName praramName = singleVariableDeclaration.getName();
                statements.add(GenStatement.genDumperWriteStatement(writeFile, praramName));
            }
        }
        return statements;
    }


   /* private List<ASTNode> insertFieldsPrinter(TypeDeclaration typeDeclaration) {
        List<ASTNode> statements = new ArrayList<>();
        FieldDeclaration[] fieldDeclarations = typeDeclaration.getFields();
        for (FieldDeclaration field : fieldDeclarations) {
            if (!Modifier.isFinal(field.getModifiers())) {
                String prefix = "this";
                // static function can not read non-static field
                if (!Modifier.isStatic(field.getModifiers()) && Modifier.isStatic(_methodDeclaration.getModifiers())) {
                    continue;
                }
                // non-static function should add class name to reach the static
                // field
                if (Modifier.isStatic(field.getModifiers())) {
                    prefix = typeDeclaration.getName().getFullyQualifiedName();
                }

                Type type = field.getType();
                List<ASTNode> variables = field.fragments();
                for (ASTNode astNode : variables) {
                    if (astNode instanceof VariableDeclarationFragment) {
                        VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) astNode;
                        SimpleName name = variableDeclarationFragment.getName();
                        List<ASTNode> nodes = genStatement(prefix, type, name);
                        if (nodes != null) {
                            statements.addAll(nodes);
                        }
                    } else {

                    }
                }
            }
        }
        return statements;
    }


    private List<ASTNode> genStatement(String prefix, Type type, SimpleName name, String message) {
        List<ASTNode> statements = new ArrayList<>();
        if (type.isPrimitiveType() || type.toString().equals("String") || type.toString().equals("CharSequence")
                || type.toString().equals("StringBuilder") || type.toString().equals("StringBuffer")) {
            statements.add(GenStatement.genPrimitiveStatement(prefix, message, name.toString()));
            return statements;
        } else if (type.isSimpleType()) {
            String sourcePath = InfoBuilder.buildSourceSRCPath(_dynamicRuntimeInfo, true);
            boolean imported = false;
            for (Object object : _cu.imports()) {
                if (object instanceof ImportDeclaration) {
                    ImportDeclaration importDeclaration = (ImportDeclaration) object;
                    String importName = importDeclaration.getName().getFullyQualifiedName();
                    int index = importName.lastIndexOf(".");
                    if (index < 0) {
                        if (Debugger.debugOn()) {
                            Debugger.debug(__name__ + "#genStatement Parse the format of Import failed : "
                                    + importDeclaration);
                        }
                        continue;
                    }
                    String importClazzName = importName.substring(index + 1);
                    if (importClazzName.equals(type.toString())) {
                        imported = true;
                        String packageName = importName.substring(0, index);
                        String absoluteJavaFilePath = sourcePath + Constant.PATH_SEPARATOR + packageName;
                        if (TypeSearchEngine.searchType(absoluteJavaFilePath, importClazzName)) {
                            // find clazz
                            String file = absoluteJavaFilePath.replaceAll("\\.", Constant.PATH_SEPARATOR)
                                    + Constant.PATH_SEPARATOR + importClazzName + ".java";
                            List<ASTNode> nodes = genStatement(prefix, file, name, message);
                            if (nodes != null) {
                                statements.addAll(nodes);
                            }

                        } else {
                            if (Debugger.debugOn()) {
                                Debugger.debug(__name__ + "getStatement Not a type imported : " + importClazzName);
                            }
                        }
                        break;
                    }
                } else {
                    if (Debugger.debugOn()) {
                        Debugger.debug(__name__ + "#genStatement Import is not an ImportDeclaration : " + object);
                    }
                }
            }
            if (!imported) {
                String currPackage = _cu.getPackage().getName().getFullyQualifiedName();
                String absoluteJavaFilePath = sourcePath + Constant.PATH_SEPARATOR + currPackage;
                if (TypeSearchEngine.searchType(absoluteJavaFilePath, type.toString())) {
                    // find the type
                    String file = absoluteJavaFilePath.replaceAll("\\.", Constant.PATH_SEPARATOR)
                            + Constant.PATH_SEPARATOR + type.toString() + ".java";
                    List<ASTNode> nodes = genStatement(prefix, file, name, message);
                    if (nodes != null) {
                        statements.addAll(nodes);
                    }
                } else {
                    Statement newStatement = GenStatement.genNullCheckerStatement(prefix, name.getFullyQualifiedName(),
                            message);
                    if (newStatement != null) {
                        statements.add(newStatement);
                    }
                }
            }

        } else if (type.isQualifiedType() || type.isArrayType() || type.isParameterizedType() || type.isUnionType()) {
            Statement newStatement = GenStatement.genNullCheckerStatement(prefix, name.getFullyQualifiedName(),
                    message);
            if (newStatement != null) {
                statements.add(newStatement);
            }

        } else {
            if (Debugger.debugOn()) {
                Debugger.debug(__name__ + "#genStatement  UNKNOWN (Not print) type : " + type);
            }
        }

        return statements;
    }

    private List<ASTNode> genStatement(String prefix, String javaFilePath, SimpleName name, String message) {
        List<ASTNode> statements = new ArrayList<>();
        List<String> methods = TypeSearchEngine.searchSimpleMethod(javaFilePath);
        if (methods != null) {
            for (String method : methods) {
                Statement newStatement = GenStatement.genMethodInvocationStatement(prefix, name.getFullyQualifiedName(),
                        method, message);
                statements.add(newStatement);
            }
        }
        return statements;
    }*/

}

