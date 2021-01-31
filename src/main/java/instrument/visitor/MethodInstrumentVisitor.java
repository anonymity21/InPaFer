package instrument.visitor;

import entity.Method;
import entity.Pair;
import org.eclipse.jdt.core.dom.*;
import util.FileIO;

import java.util.LinkedHashMap;
import java.util.Map;

//import org.apache.commons.lang3.tuple.*;

/**
 * 这个类主要用于遍历某个指定的类
 * 最后可以得到以下内容
 * Map<String, Pair<Integer, Integer>> methodRangeList 这个类内每个method的初始和终止行号
 * Map<String, Integer> methodStartList 这个类内每个method的初始行号
 * Method _method 指定名称method的 函数内容，该起始和结束行号
 */
public class MethodInstrumentVisitor extends TraversalVisitor {

    private final static String __name__ = "@MethodInstrumentVisitor ";


    //private String _methodFlag = Constant.INSTRUMENT_SOURCE;
    private String _clazzName = "";
    private String _clazzFileName = "";
    private CompilationUnit _cu;
    private Map<String, Pair<Integer, Integer>> methodRangeList;
    private Map<String, Integer> methodStartList;

    //private String _methodName = null;
    //private String _methodContent = "";
    private Method _method;


    @Override
    public boolean visit(CompilationUnit node) {
        if (node.getPackage().getName() != null
                && node.getPackage().getName().getFullyQualifiedName().equals("auxiliary")) {
            return false;
        }
        _cu = node;
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
            if (_clazzFileName.equals("")) {
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
            StringBuilder buffer = new StringBuilder(_clazzName + "#");

            String retType = "?";
            if (node.getReturnType2() != null) {
                retType = node.getReturnType2().toString();
            }
            StringBuilder param = new StringBuilder("?");
            for (Object object : node.parameters()) {
                if (!(object instanceof SingleVariableDeclaration)) {
                    param.append(",?");
                } else {
                    SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;
                    param.append(",").append(singleVariableDeclaration.getType().toString());
                }
            }
            // add method return type
            buffer.append(retType).append("#");
            // add method name
            buffer.append(node.getName().getFullyQualifiedName()).append("#");
            // add method params, NOTE: the first parameter starts at index 1.
            buffer.append(param);
            String message = buffer.toString();

            int lineStartNumber = _cu.getLineNumber(node.getBody().getStartPosition());
            int lineEndNumber = _cu.getLineNumber(node.getBody().getStartPosition() + node.getBody().getLength());
            if (methodRangeList == null) {
                methodRangeList = new LinkedHashMap<>();
            }
            if (methodStartList == null) {
                methodStartList = new LinkedHashMap<String, Integer>();
            }
            methodRangeList.put(message, new Pair<>(lineStartNumber, lineEndNumber));
            methodStartList.put(message, lineStartNumber);
            if (_method != null) {
                if (_method.getMethodNameString() != null && _method.getMethodNameString().equals(message)) {
                    _method.setContentString(node.toString());
                    _method.setStarLineInteger(lineStartNumber);
                    _method.setEndLineInteger(lineEndNumber);
                }
            }

            // System.out.println(node.toString());

        }

        return true;
    }

    public Map getMethodRange() {
        return methodRangeList;
    }

    public Map getMethodStart() {
        return methodStartList;
    }


    public void setMethod(Method method) {
        this._method = method;
    }

    public Method getMethod() {
        return _method;
    }

    public static void main(String[] args) {
        String filePath = "/Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Chart/Chart_3_buggy/tests/org/jfree/data/time/junit/TimeSeriesTests.java";

        MethodInstrumentVisitor methodVisitor = new MethodInstrumentVisitor();
        CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(filePath),
                ASTParser.K_COMPILATION_UNIT);
        compilationUnit.accept(methodVisitor);
        Map<String, Pair<Integer, Integer>> methodRangeList = methodVisitor.getMethodRange();
        System.out.println("complete");

    }

}
