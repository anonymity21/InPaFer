package experiment;

import config.Constant;
import entity.PatchFile;
import entity.Subject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@Data
public class TraceAnalysis {
    private Subject subject;
    private Map<String, List<PatchFile>> modifiedMethodMap = new HashMap<>();
    private Map<String, List<PatchFile>> equalTraceMap = new HashMap<>();
    //private static String mapPath
    private static String resultFile = Constant.TRACE_RESULT_HOME + "TraceAnalysis.log";

    public TraceAnalysis(Subject subject) {
        this.subject = subject;
    }

    @PostConstruct
    public void buildLogFile() {
        resultFile = Constant.TRACE_RESULT_HOME + subject.getName() + subject.getId();
    }

    private String buildTracePath(String patchName) {
        return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + "/" + Constant.INSTRUMENT_LINE_DIR +
                patchName + Constant.INSTRUMENT_LINE_SEPARATORINIT + Constant.MAPTRACE;
    }

    private String buildInitialTracePath() {
        return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + "/" + Constant.INSTRUMENT_LINE_DIR +
                Constant.INSTRUMENT_INIT_SEPARATORINIT + Constant.INSTRUMENT_LINE_SEPARATORINIT;
    }

    private String buildMapPath() {
        return Constant.Record + "/" + subject.getName() + "/" + subject.getId() + "/" + Constant.INSTRUMENT_LINE_DIR +
                Constant.INSTRUMENT_MAP;
    }


    public int anlysisModification() {
        return modifiedMethodMap.keySet().size();
    }


    // 将所有补丁的trace 划分一个等价类
    public void paritionEquivalence() {
        equalTraceMap.clear();
        //String traceContent = FileIO.readFileToString(buildInitialTracePath());
        //equalTraceMap.put(traceContent, );

        List<PatchFile> patchFiles = subject.getPatchList();
        for (PatchFile patchFile : patchFiles) {
            String traceContent = FileIO.readFileToString(buildTracePath(patchFile.getPatchName()));

            if (traceContent.equals("")) {
                log.error(patchFile.getPatchName() + " trace is empty");
            }
            if (equalTraceMap.containsKey(traceContent)) {
                equalTraceMap.get(traceContent).add(patchFile);
            } else {
                List<PatchFile> tmpList = new LinkedList<>();
                tmpList.add(patchFile);
                equalTraceMap.put(traceContent, tmpList);
            }
        }
    }

    public void analysisEqualClass() {
        FileIO.writeStringToLog(resultFile, "Equalence Class: " + equalTraceMap.size());
        FileIO.writeStringToLog(resultFile, "Total Patches Number: " + subject.getPatchList().size());

        for (List<PatchFile> patchFiles : equalTraceMap.values()) {
            FileIO.writeStringToLog(resultFile, "patchFiles Number: " + patchFiles.size() + "");
        }
        getDifference();
    }

    public void getDifference() {

        List<PatchFile> patchFiles = subject.getPatchList();
        Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();

        String traceContent = FileIO.readFileToString(buildInitialTracePath());
        if (traceContent.equals("")) {
            log.error("Init trace is empty");
        }
        for (String line : traceContent.split("\n")) {
            if (linePatchMap.containsKey(line)) {
                linePatchMap.get(line).add("Init");
            } else {
                Set<String> tmpList = new LinkedHashSet<>();
                tmpList.add("Init");
                linePatchMap.put(line, tmpList);
            }
        }


        for (PatchFile patchFile : patchFiles) {
            traceContent = FileIO.readFileToString(buildTracePath(patchFile.getPatchName()));
            if (traceContent.equals("")) {
                log.error(patchFile.getPatchName() + " trace is empty");
            }
            for (String line : traceContent.split("\n")) {
                if (linePatchMap.containsKey(line)) {
                    linePatchMap.get(line).add(patchFile.getPatchName());
                } else {
                    Set<String> tmpList = new LinkedHashSet<>();
                    tmpList.add(patchFile.getPatchName());
                    linePatchMap.put(line, tmpList);
                }
            }
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry entry : linePatchMap.entrySet()) {
            Set<String> tmpList = (Set<String>) entry.getValue();
            if (tmpList.size() < patchFiles.size() + 1) {
                result.append(entry.getKey()).append(":");
                for (String patchName : tmpList) {
                    result.append(patchName).append("\t");
                }
                result.append("\n");
            }
        }
        log.info(subject.getPatchList().stream().map(PatchFile::getPatchName).collect(Collectors.joining("\t")));
        log.info(result.toString());

        FileIO.writeStringToFile(buildMapPath(), result.toString(), false);
    }

    public static void main(String[] args) {
        String project = "Math";
        int Start = 1;
        int end = 106;
        for (int i = Start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
                TraceAnalysis traceAnalysis = new TraceAnalysis(subject);
                //traceAnalysis.paritionEquivalence();
                //traceAnalysis.analysisEqualClass();
                traceAnalysis.getDifference();
            }
        }
    }
}
