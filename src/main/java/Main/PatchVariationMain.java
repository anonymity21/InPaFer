package Main;


import PatchProcess.Format;
import config.Constant;
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
public class PatchVariationMain {

    // private Set<String> modifiedMethodSet = new LinkedHashSet<>();
    private Map<String, List<PatchFile>> fixedFileMap = new LinkedHashMap<>();
    //public static String resultFile = Constant.LOG_FILE + "ObtainTrace.log";

    /*
    initialize fixedFileMap
     */
    public void initModifiedMethodMap(List<PatchFile> patchFileList) {
       /* for(PatchFile patchFile: patchFileList){
            patchFile.setFixedFile(patchFile.getFixedFile().replaceAll("\\/\\/","\\/"));
        }*/

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

    public void mainProcessForLine(Subject subject) {

        log.info("RUNNING Program for Line ");
        // only running three failing test
        int number = subject.getFailedTestList().size() < Constant.FailingTestNumber ?
                subject.getFailedTestList().size():Constant.FailingTestNumber;

        for(int i = 0; i < number; i++){
            getTrace4SingleTest(subject.getFailedTestList().get(i), subject);
            MapService.compareByParagraphOInitial(subject.getFailedTestList().get(i), subject);
        }
    }

    private void getTrace4SingleTest(String failingTest, Subject subject ) {

        List<PatchFile> patchFileList = subject.getPatchList();
        Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> allDelteLine = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getDeleteLine)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));

        String writeFile = BuildFilePath.tmpLine(Constant.INSTRUMENT_INIT_SEPARATORINIT, subject, failingTest);

        if (new File(writeFile).exists()) {
            new File(writeFile).delete();
        }

        if( !formatFixedFileSet(fixedFileSet, subject)){
            log.error("Initial Format Failed");
            return;
        }

        MethodTraceService methodTraceService = new MethodTraceService();
        methodTraceService.instrumentLineNumber(fixedFileMap, writeFile);
        if (methodTraceService.Compile(subject)) {
            if(! Runner.JUnitTestSubject(subject,failingTest)){
                deleteFixedLine(writeFile, allDelteLine);
            }else {
                log.error("Initial Test Success");
            }
        }else {
            log.error("Initial Compile Failed");
        }

        fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

        for (PatchFile patchFile : patchFileList) {
            /*String[] testPatches ={"Cardumen-patch67","Cardumen-patch20", "Cardumen-patch47", "Cardumen-patch8", "Cardumen-patch37"};
            if(! Arrays.asList(testPatches).contains(patchFile.getPatchName())){
                continue;
            }*/
            log.info("RUNNING Patch " + patchFile.getPatchName());

            //fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
            if( !formatFixedFileSet(fixedFileSet, subject)){
                log.error("After Patched Format Failed");
                return;
            }

            if (patchFile.patchToFile()) {
                log.info("Patch " + patchFile.getPatchName() + " Patches File Success.");
                writeFile = BuildFilePath.tmpLine(patchFile.getPatchName(), subject, failingTest);
                if (new File(writeFile).exists()) {
                    new File(writeFile).delete();
                }
                methodTraceService.instrumentLineNumber(fixedFileMap, writeFile);

                if (methodTraceService.Compile(subject)) {
                    if( Runner.JUnitTestSubject(subject,failingTest)){
                        //deleteFixedLine(writeFile, allDelteLine);
                    }else {
                        log.error("After Patched Test Failed");
                    }
                }else {
                    log.error("After Patched Compile Failed");
                }

            } else {
                log.error("Patch " + patchFile.getPatchName() + " Patches File Fail.");
                //FileIO.writeStringToLog(resultFile, "Patch " + patchFile.getPatchName() + " Patches File Fail.");
            }
            fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

            TraceMapService traceMapService = new TraceMapService(patchFile);
            traceMapService.traceMap(allDelteLine, failingTest);

        }
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

        String writeFile = BuildFilePath.tmpState(Constant.INSTRUMENT_INIT_SEPARATORINIT, subject, failingTest);
        if (new File(writeFile).exists()) {
            new File(writeFile).delete();
        }

        //fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
        if( !formatFixedFileSet(fixedFileSet, subject)){
            log.error("Initial Format Failed");
            return;
        }

        MethodTraceService methodTraceService = new MethodTraceService();
        methodTraceService.instrumentState(fixedFileMap, writeFile);

        if (methodTraceService.Compile(subject)) {
            if(! Runner.JUnitTestSubject(subject,failingTest)){

            }else {
                log.error("Initial Test Success");
            }
        }else {
            log.error("Initial Compile Failed");
            return;
        }
        fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

        for (PatchFile patchFile : patchFileList) {
           /* String[] testPatches ={"Cardumen-patch67","Cardumen-patch20", "Cardumen-patch47", "Cardumen-patch8", "Cardumen-patch37"};
            if(! Arrays.asList(testPatches).contains(patchFile.getPatchName())){
                continue;
            }*/
            /*if(! patchFile.getPatchName().equals("Cardumen-patch160")){
                continue;
            }*/
            log.info("RUNNING Patch << " + patchFile.getPatchName() + " >>");

            if( !formatFixedFileSet(fixedFileSet, subject)){
                log.error("Initial Format Failed");
                return;
            }
            if (patchFile.patchToFile()) {
                log.info("Patch " + patchFile.getPatchName() + " Patches File Success.");
                writeFile = BuildFilePath.tmpState(patchFile.getPatchName(), subject, failingTest);

                if (new File(writeFile).exists()) {
                    new File(writeFile).delete();
                }
                methodTraceService.instrumentState(fixedFileMap, writeFile);

                if (methodTraceService.Compile(subject)) {
                    if( Runner.JUnitTestSubject(subject,failingTest)){
                    }else {
                        log.error("After Patched Test Failed");
                    }
                }else {
                    log.error("After Patched Compile Failed");
                }
            } else {
                log.error("Patch " + patchFile.getPatchName() + " Patches File Fail.");
            }
            fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));
        }
    }


    public static void main(String[] args) {
        String project = "Math";
        int start = 53;
        int end = 53;
        boolean runVar = true;
        boolean runLine = true;

        for(int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-project=")) {
                // Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
                project = args[i].substring("-project=".length());
            } else if (args[i].startsWith("-start=")) {
                start = Integer.parseInt(args[i].substring("-start=".length()));
            } else if (args[i].startsWith("-end=")) {
                end = Integer.parseInt(args[i].substring("-end=".length()));
            } else if (args[i].startsWith("-r=")){
                if(args[i].substring("-r=".length()).equals("var")){
                    runVar = true;
                }
                if(args[i].substring("-r=".length()).equals("line")){
                    runLine = true;
                }
            }
        }
        log.info("Start Running <<" + project +">> PatchVariationMain from " + start + " to " + end );

        for (int i = start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                //FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
                List<PatchFile> patchList = subject.getPatchList();
                if(!subject.exist()){
                    if(! Runner.downloadSubject(subject)){
                        continue;
                    }
                }


                MethodInitialization methodInitialization = new MethodInitialization(subject);
                methodInitialization.MainProcess();
                //methodInitialization.MainProcess4TestFile();

                PatchVariationMain patchVariationMain = new PatchVariationMain();
                patchVariationMain.initModifiedMethodMap(patchList);
                if(runLine){
                    patchVariationMain.mainProcessForLine(subject);
                }
                if(runVar){
                    patchVariationMain.mainProcessForState(subject);
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
