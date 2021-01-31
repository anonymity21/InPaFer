package Main;


import PatchProcess.Format;
import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.CorrectPatch;
import entity.PatchFile;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import run.Runner;
import service.MapService;
import service.MethodTraceService;
import service.TraceMapService;
import util.BuildFilePath;
import util.FileIO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/*
This file obtains trace information and variable information
 */

@Slf4j
public class FixedProjectMain {

    // private Set<String> modifiedMethodSet = new LinkedHashSet<>();
    private Map<String, List<PatchFile>> fixedFileMap = new LinkedHashMap<>();
    //public static String resultFile = Constant.LOG_FILE + "ObtainTrace.log";

    /*
    initialize fixedFileMap
     */
    public void initModifiedMethodMap(List<PatchFile> patchFileList) {
        fixedFileMap = patchFileList.stream().filter(Objects::nonNull).collect(Collectors
                .groupingBy(PatchFile::getFixedFile, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
    }

    /*
     backup all fixedFiles and format them
      if compile failed return false
    */
    public boolean formatFixedFileSet(Set<String> fixedFileSet, Subject subject){

        log.info("Format All FixedFileSet");
        for(String fixedFile: fixedFileSet){
            FileIO.backUpFile(fixedFile, fixedFile+".bak");
        }

        subject.deleteTarget();
        if(!Runner.compileSubject(subject)) {
            return false;
        }

        Format format = new Format(subject);
        format.formatAllFile(fixedFileMap.keySet());
        return true;
    }
    public boolean formatOriginalFixedFile(String fixedFile, Subject subject){
        subject.setHome(Constant.PROJECT_HOME.split("_fixed")[0]);
        FileIO.backUpFile(fixedFile, fixedFile+".bak");
        subject.deleteTarget();
        if(!Runner.compileSubject(subject)) {
            return false;
        }

        Format format = new Format(subject);
        format.formatSingleFile(fixedFile);
        return true;
    }

    public void mainProcessForLine(Subject subject) {

        log.info("RUNNING Program for Line ");
        // only running three failing test
        int number = subject.getFailedTestList().size() < Constant.FailingTestNumber ?
                subject.getFailedTestList().size():Constant.FailingTestNumber;

        for(int i = 0; i < number; i++){
            getTrace4SingleTest(subject.getFailedTestList().get(i), subject);
           // MapService.compareByParagraphOInitial(subject.getFailedTestList().get(i), subject);
            //todo reimplement
        }
    }

    private void getTrace4SingleTest(String failingTest, Subject subject ) {

        List<PatchFile> patchFileList = subject.getPatchList();
        Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> allDelteLine = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getDeleteLine)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));

        String writeFile = BuildFilePath.tmpLine(Constant.INSTRUMENT_FIXED_SEPARATORINIT, subject, failingTest);
        String writeFinalFile = BuildFilePath.tmpMapTraceLine(Constant.INSTRUMENT_FIXED_SEPARATORINIT, subject, failingTest);
        if (new File(writeFile).exists()) {
            new File(writeFile).delete();
        }

        if( !formatFixedFileSet(fixedFileSet, subject)){
            log.error("Fixed Project Format Failed");
            return;
        }

        MethodTraceService methodTraceService = new MethodTraceService();
        methodTraceService.instrumentLineNumber(fixedFileMap, writeFile);
        if (methodTraceService.Compile(subject)) {
            if(Runner.JUnitTestSubject(subject,failingTest)){
               // deleteFixedLine(writeFile, allDelteLine);
            }else {
                log.error("Fixed Project Test Failed");
            }
        }else {
            log.error("Fixed Project Compile Failed");
        }

        fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

       // TraceMapService traceMapService = new TraceMapService(patchFile);
       // traceMapService.traceMap(allDelteLine, failingTest);
        //todo add trace map from fixed trace to original trace
        String diffFileName = Constant.CorrectPatch + subject.getName() + "/" + subject.getId() + ".src.patch";
        String tmpFirstLine = FileIO.readFileToString(diffFileName).split("\n")[0];

        String fullClassName = subject.getHome().replaceAll("_fixed","") +  tmpFirstLine.split("diff --git a")[1].split(".java b")[0] + ".java";

        formatOriginalFixedFile(fullClassName, new Subject(subject.getName(), subject.getId()));

        TraceMapService traceMapService = new TraceMapService();
        traceMapService.traceMapforFixedFile(allDelteLine, writeFile, diffFileName,fullClassName,writeFinalFile );
        }

    private void deleteFixedLine(String writeFile, Set<String> allDelteLine) {

        String content = FileIO.readFileToString(writeFile);
        StringBuilder result = new StringBuilder();
        for (String line : content.split("\n")) {
            String className = line.split("#")[0];
            if (className.contains("$")) {
                className = className.split("\\$")[0];
            }
            className = className + ".java";
            Integer lineNum = Integer.valueOf(line.split("#")[line.split("#").length - 1]);
            String tmpLine = className + "#" + lineNum;
            boolean delete = false;
            for (String delteLine : allDelteLine) {
                if (delteLine.contains(tmpLine)) {
                    delete = true;
                    break;
                }
            }
            if (delete) {
                continue;
            }
            result.append(line).append("\n");

        }
        FileIO.writeStringToFile(writeFile, result.toString(), false);

    }

    public void mainProcessForState(Subject subject) {

        log.info("RUNNING Program for State ");
        // fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
        // only running three failing test
        int number = subject.getFailedTestList().size() < Constant.FailingTestNumber ?
                subject.getFailedTestList().size():Constant.FailingTestNumber;

        for(int i = 0; i < number; i++){
            getVar4SingleTest(subject.getFailedTestList().get(i), subject);
        }

    }

    private void getVar4SingleTest(String failingTest, Subject subject) {

        List<PatchFile> patchFileList = subject.getPatchList();
        Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String writeFile = BuildFilePath.tmpState(Constant.INSTRUMENT_FIXED_SEPARATORINIT, subject, failingTest);
        if (new File(writeFile).exists()) {
            new File(writeFile).delete();
        }

        //fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
        if( !formatFixedFileSet(fixedFileSet, subject)){
            log.error("Fixed Project Format Failed");
            return;
        }

        MethodTraceService methodTraceService = new MethodTraceService();
        methodTraceService.instrumentState(fixedFileMap, writeFile);

        if (methodTraceService.Compile(subject)) {
            if(Runner.JUnitTestSubject(subject,failingTest)){

            }else {
                log.error("Fixed Project Test Failed");
            }
        }else {
            log.error("Fixed Project Compile Failed");
            return;
        }
        fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

    }
    public boolean containsCorretPatch(Subject subject) {
        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        boolean contains = false;
        List<PatchFile> patchFileList = subject.getPatchList();
        for (PatchFile patchFile : patchFileList) {
            CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(),
                    String.valueOf(subject.getId()));
            if (correctPatchList.contains(correctPatch)) {
                patchFile.setCorrectness(true);
                //patchCorrectnessMap.put(patchFile.getPatchName(), true);
                contains = true;
            }else{
               // patchCorrectnessMap.put(patchFile.getPatchName(), false);
            }
        }
        return contains;
    }



    public static void main(String[] args) {
        String project = "Chart";
        int start = 6;
        int end = 6;

        for(int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-p=")) {
                // Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
                project = args[i].substring("-p=".length());
            } else if (args[i].startsWith("-s=")) {
                start = Integer.parseInt(args[i].substring("-s=".length()));
            } else if (args[i].startsWith("-e=")) {
                end = Integer.parseInt(args[i].substring("-e=".length()));
            }
        }
        log.info("Start Running <<" + project +">> PatchVariationMain from " + start + " to " + end );

        for (int i = start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                //FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
                List<PatchFile> patchList = subject.getPatchList();

                FixedProjectMain fixedProjectMain = new FixedProjectMain();
                if(!fixedProjectMain.containsCorretPatch(subject)){

                    if(!subject.exist()){
                        if(! Runner.downloadFixedSubject(subject)){
                            continue;
                        }
                    }

                    MethodInitialization methodInitialization = new MethodInitialization(subject);
                    methodInitialization.MainProcess();
                    fixedProjectMain.initModifiedMethodMap(patchList);
                    fixedProjectMain.mainProcessForLine(subject);
                    fixedProjectMain.mainProcessForState(subject);
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
