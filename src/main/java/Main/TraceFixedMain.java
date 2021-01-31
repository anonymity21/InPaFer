package Main;

import config.Constant;
import entity.PatchFile;
import entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import service.MethodTraceService;
import util.FileIO;
import util.PatchInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/*
 * Get Correct Trace and Variable value for the subjects which do not contain correct patches.
 * notice that change the Constant.Project_home to fixed_projects when running this file.
 */
@Slf4j
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceFixedMain {

    private Subject subject;

    private Map<String, List<PatchFile>> fixedFileMap = new LinkedHashMap<>();

    public TraceFixedMain(Subject subject) {
        this.subject = subject;
    }

    public void initModifiedMethodMap(List<PatchFile> patchFileList) {
        fixedFileMap = patchFileList.stream().filter(Objects::nonNull).collect(Collectors
                .groupingBy(PatchFile::getFixedFile, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
    }

    public void mainProcessForLine() {
        List<PatchFile> patchFileList = subject.getPatchList();
        Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> allDelteLine = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getDeleteLine)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));

        log.info("RUNNING inital Program for Line ");

        MethodTraceService methodTraceService = new MethodTraceService();
        fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
        String writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
                + Constant.INSTRUMENT_LINE_DIR + Constant.INSTRUMENT_FIXED_SEPARATORINIT
                + Constant.INSTRUMENT_LINE_SEPARATORINIT;
        if (new File(writeFile).exists()) {
            new File(writeFile).delete();
        }
        methodTraceService.instrumentLineNumber(fixedFileMap, writeFile);
        if (methodTraceService.Compile(subject)) {
            methodTraceService.runFailTest(subject);
        }
        // deleteFixedLine(writeFile, allDelteLine);

    }

    private void deleteFixedLine(String writeFile, Set<String> allDelteLine) {

        String content = FileIO.readFileToString(writeFile);
        StringBuilder result = new StringBuilder();
        for (String line : content.split("\n")) {
            String className = line.split("#")[0] + ".java";
            int lineNum = Integer.parseInt(line.split("#")[line.split("#").length - 1]);
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

    public void mainProcessForState() {
        List<PatchFile> patchFileList = subject.getPatchList();
        Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        log.info("RUNNING fixed Program for State ");

        MethodTraceService methodTraceService = new MethodTraceService();

        fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
        String writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
                + Constant.INSTRUMENT_STATE_DIR + Constant.INSTRUMENT_FIXED_SEPARATORINIT
                + Constant.INSTRUMENT_STATE_SEPARATORINIT;
        if (new File(writeFile).exists()) {
            new File(writeFile).delete();
        }
        methodTraceService.instrumentState(fixedFileMap, writeFile);
        if (methodTraceService.Compile(subject)) {
            methodTraceService.runFailTest(subject);
        } else {
            FileIO.writeStringToFile(Constant.HOME + "/fixedstate.log", subject.toString() + " need to check!", true);
        }
        fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));
    }

    public static void main(String[] args) {
        String project = "Chart";
        int Start = 13;
        int end = 13;
        List<Integer> idlist = Arrays.asList(13, 15, 21, 25, 26);
        // chart (13, 15, 21, 25, 26)
        // lang 53
        // math (2,8,28,49,81,84,87,88,95,97,105)
        for (int i = Start; i <= end; i++) {
            if (!idlist.contains(i)) {
                continue;
            }
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                List<PatchFile> patchList = subject.getPatchList();
                TraceFixedMain tarceFixedMain = new TraceFixedMain(subject);
                PatchInfo.obainAllMethod(patchList);
                tarceFixedMain.initModifiedMethodMap(patchList);
                // tarceFixedMain.mainProcessForLine();
                tarceFixedMain.mainProcessForState();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
