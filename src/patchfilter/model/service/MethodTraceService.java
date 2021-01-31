package patchfilter.model.service;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.instrument.visitor.StateCollectionInstrumentVisitor;
import patchfilter.model.instrument.visitor.StatementInstrumentVisitor;
import patchfilter.model.instrument.visitor.TraversalVisitor;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@Builder
@NoArgsConstructor
public class MethodTraceService {

/*
    private String prefix2path(String prefix) {
        StringBuilder path = new StringBuilder();
        path.append(subject.getHome()).append(subject.getSsrc())
                .append(prefix.replaceAll("\\.", "\\")).append(".java");
        return path.toString();
    }*/

    // 对list<method>的函数 line number 插桩
    public void instrumentLineNumber(Map<String, List<PatchFile>> fixedFileMap, String writeFile) {

        for (Map.Entry<String, List<PatchFile>> entry : fixedFileMap.entrySet()) {
            String fixedFile = entry.getKey();
            Set<String> methodSet = entry.getValue().stream().filter(Objects::nonNull)
                    .map(PatchFile::getModifiedMethod).collect(Collectors.toSet());
            StatementInstrumentVisitor statementInstrumentVisitor = new StatementInstrumentVisitor();
            statementInstrumentVisitor.setInstrumentSet(methodSet);
            statementInstrumentVisitor.setWriteFile(writeFile);
            writeInstrumentFile(fixedFile, statementInstrumentVisitor);
        }
    }


    // 对内的 Set<method>进行 state 插桩
    public void instrumentState(Map<String, List<PatchFile>> fixedFileMap, String writeFile) {
        // log.info("Instrument State for " + methodName);
        for (Map.Entry<String, List<PatchFile>> entry : fixedFileMap.entrySet()) {
            String fixedFile = entry.getKey();
            Set<String> methodSet = entry.getValue().stream().filter(Objects::nonNull)
                    .map(PatchFile::getModifiedMethod).collect(Collectors.toSet());
            StateCollectionInstrumentVisitor stateCollectionInstrumentVisitor = new StateCollectionInstrumentVisitor();
            stateCollectionInstrumentVisitor.setIntrumentMethodSet(methodSet);
            stateCollectionInstrumentVisitor.setWriteFile(writeFile);
            writeInstrumentFile(fixedFile, stateCollectionInstrumentVisitor);
        }

    }

    private void writeInstrumentFile(String fixedFile, TraversalVisitor traversalVisitor) {
        CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(fixedFile), ASTParser.K_COMPILATION_UNIT);
        compilationUnit.accept(traversalVisitor);
        FileIO.writeStringToFile(fixedFile, compilationUnit.toString());
    }

    // 执行失败的测试
    public void runFailTest(Subject subject) {

       String resultString =  Runner.JUnitTestSubject(subject, subject.getFailedTestList().get(0));
       log.info(resultString);

/*        // 暂时只考虑一个failing test.
        for (String failingTest : subject.getFailedTestList()) {
            Runner.JUnitTestSubject(subject, failingTest);
        }*/
    }

    // 编译项目
    public boolean Compile(Subject subject) {
        //log.info("Run Fail Test " );

        String srcPath = subject.getHome() + "/" + subject.getSsrc();
        try {
            FileUtils.copyFile(new File(Constant.HOME + "/src/auxiliary/Dumper.java"), new File(srcPath + "/auxiliary/Dumper.java"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        subject.deleteTarget();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!Runner.compileSubject(subject)) {
            log.error(subject.toString() + " Compile Failed !");
            return false;
        }
        log.info(subject.toString() + " Compile Success !");
        return true;
    }


    public static void main(String[] args) {


        Subject subject = new Subject("Chart", 3);
        String PatchName = "SimFix-0";
        String path = Constant.AllPatchPath + "Chart/3/" + PatchName;
        PatchFile patchFile = new PatchFile(subject, path);
        patchFile.parsePatch();

        MethodTraceService methodTraceService = new MethodTraceService();
         PatchInfo.getModifyMethod(patchFile);
        Set<String> methodList = new LinkedHashSet<>();
        methodList.add(patchFile.getModifiedMethod());
        String fixedFile = patchFile.getFixedFile();

        String writeFile = "./test/init";

        FileIO.backUpFile(fixedFile, fixedFile + ".bak");
        methodTraceService.instrumentLineNumber(Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)), writeFile);
        if (methodTraceService.Compile(subject)) {
            methodTraceService.runFailTest(subject);
        }
        FileIO.restoreFile(fixedFile, fixedFile + ".bak");

        FileIO.backUpFile(fixedFile, fixedFile + ".bak");

        patchFile.patchToFile();
        writeFile = "./test/" + PatchName;
        methodTraceService.instrumentLineNumber(Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)), writeFile);
        if (methodTraceService.Compile(subject)) {
            methodTraceService.runFailTest(subject);
        }
        FileIO.restoreFile(fixedFile, fixedFile + ".bak");

        TraceMapService traceMapService = new TraceMapService(patchFile);
        traceMapService.traceMap(new LinkedHashSet<>());


        FileIO.backUpFile(fixedFile, fixedFile + ".bak");
        methodTraceService.instrumentState(Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)), writeFile);
        if (methodTraceService.Compile(subject)) {
            methodTraceService.runFailTest(subject);
        }


        FileIO.restoreFile(fixedFile, fixedFile + ".bak");

        FileIO.backUpFile(fixedFile, fixedFile + ".bak");
        patchFile.patchToFile();
        methodTraceService.instrumentState(Collections.singletonMap(fixedFile, Collections.singletonList(patchFile)), writeFile);
        if (methodTraceService.Compile(subject)) {
            methodTraceService.runFailTest(subject);
        }
        FileIO.restoreFile(fixedFile, fixedFile + ".bak");

    }

}
