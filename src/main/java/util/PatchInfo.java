package util;

import PatchProcess.Format;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.Constant;
import entity.Pair;
import entity.PatchFile;
import entity.Subject;
import instrument.visitor.MethodInstrumentVisitor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import run.Runner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class PatchInfo {


    public static void obainAllMethod(List<PatchFile> patchFileList) {
        log.info("Obtain All Method for Patch List ");
        for (PatchFile patchFile : patchFileList) {
            getModifyMethod(patchFile);
        }
    }

    // Set patchFile 修改的函数名称
    public static void getModifyMethod(PatchFile patchFile) {

        //log.info("Init Method Range Info for: " + patchFile.getPatchName());

        int modifyLine = patchFile.getModifyLine();
        String fixedFile = patchFile.getFixedFile();

        String MethodRangeFile = fixedFile.split("/")[fixedFile.split("/").length - 1];
        // 存储 fixed file下所有method的range的文件
        String tmpMethodRecord = Constant.CACHE + patchFile.getSubject().getName()
                + "/" + patchFile.getSubject().getId() + "/" + MethodRangeFile;
        // 存储每个补丁，修改函数的文件
        String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName()
                + "/" + patchFile.getSubject().getId() + "/" + patchFile.getPatchName();

        Map<String, Pair<Integer, Integer>> methodRangeList = new HashMap<String, Pair<Integer, Integer>>();

        if (new File(patchMethodFile).exists()) {
            String methodName = FileIO.readFileToString(patchMethodFile);
            patchFile.setModifiedMethod(methodName);
            // return methodName;

        } else if (new File(tmpMethodRecord).exists()) {
            methodRangeList = str2Map(FileIO.readFileToString(tmpMethodRecord));

        } else {
            FileIO.backUpFile(fixedFile, fixedFile + ".bak");

            Subject subject = patchFile.getSubject();
            subject.deleteTarget();
            if(! Runner.compileSubject(subject)){
                log.error("Compile Fail");
            }
            Format format = new Format(subject);
            format.formatSingleFile(fixedFile);

            //Format format = new Format();
            MethodInstrumentVisitor methodVisitor = new MethodInstrumentVisitor();
            CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(fixedFile),
                    ASTParser.K_COMPILATION_UNIT);
            compilationUnit.accept(methodVisitor);
            methodRangeList = methodVisitor.getMethodRange();
            String methodRange = JSONObject.toJSONString(methodRangeList);
            FileIO.writeStringToFile(tmpMethodRecord, methodRange);

            FileIO.restoreFile(fixedFile, fixedFile + ".bak");

        }
        for (Map.Entry<String, Pair<Integer, Integer>> entry : methodRangeList.entrySet()) {
            int starLine = entry.getValue().getKey();
            int endLine = entry.getValue().getValue();
            if (modifyLine >= starLine && modifyLine <= endLine) {
                FileIO.writeStringToFile(patchMethodFile, entry.getKey());
                patchFile.setModifiedMethod(entry.getKey());
                break;
            }

        }
        if (patchFile.getModifiedMethod().equals("")) {
            log.error("Patch " + patchFile.getPatchName() + " Cannot get modified Method!");
        }

        // return null;
    }


    //private void 

    public static Map<String, Pair<Integer, Integer>> str2Map(String str) {
        //Map<String, Pair<Integer, Integer>> map =
        //        (Map<String, Pair<Integer, Integer>>) JSON.parseObject(str, new TypeReference<Map<String, Pair<Integer, Integer>>>() {});
        return new Gson().fromJson(str, new TypeToken<HashMap<String, Pair<Integer, Integer>>>() {
        }.getType());
    }

}
