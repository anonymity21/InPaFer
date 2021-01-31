package entity;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.Constant;
import instrument.visitor.MethodInstrumentVisitor;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import util.FileIO;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class LineNumber {
    private String filePath;
    private Subject subject;
    private Map<String, Integer> methodRangeList;

    public LineNumber(Subject subject, String file) {
        this.subject = subject;
        this.filePath = file;
    }

    public void parseFile() {
        // String fixedFile = patchFile.getFixedFile();

        String MethodRangeFile = filePath.split("/")[filePath.split("/").length - 1];
        // 存储 fixed file下所有method的range的文件
        String tmpMethodRecord = Constant.CACHE + subject.getName()
                + "/" + subject.getId() + "/" + MethodRangeFile;
        methodRangeList = new HashMap<String, Integer>();

        if (new File(tmpMethodRecord).exists()) {
            methodRangeList = str2Map(FileIO.readFileToString(tmpMethodRecord));
        } else {
            MethodInstrumentVisitor methodVisitor = new MethodInstrumentVisitor();
            CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(filePath),
                    ASTParser.K_COMPILATION_UNIT);
            compilationUnit.accept(methodVisitor);
            methodRangeList = methodVisitor.getMethodStart();
            String methodRange = JSONObject.toJSONString(methodRangeList);
            FileIO.writeStringToFile(tmpMethodRecord, methodRange);
        }
    }

    private static Map<String, Integer> str2Map(String str) {
        //Map<String, Pair<Integer, Integer>> map =
        //        (Map<String, Pair<Integer, Integer>>) JSON.parseObject(str, new TypeReference<Map<String, Pair<Integer, Integer>>>() {});
        return new Gson().fromJson(str, new TypeToken<HashMap<String, Integer>>() {
        }.getType());
    }

    public int getLine(String lineContent) {
        for (Entry<String, Integer> entry : this.methodRangeList.entrySet()) {
            if (entry.getKey().contains(lineContent)) {
                return entry.getValue();
            }
        }
        return 1;
    }

    public static void main(String[] args) {
        String filePath = "/Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Chart/Chart_13_buggy/tests/org/jfree/data/time/junit/TimeSeriesTests.java";
        Subject subject = new Subject("Chart", 3);
        LineNumber linenumber = new LineNumber(subject, filePath);
        linenumber.parseFile();
        System.out.println(linenumber.getLine("testCreateCopy3"));

    }
}
