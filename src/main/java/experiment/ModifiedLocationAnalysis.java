package experiment;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.CorrectPatch;
import entity.Method;
import entity.PatchFile;
import entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedLocationAnalysis {

    private Subject subject;

    // 将 补丁按照修改位置分组
    public void MainProcess() {

        setModification();
        Set<String> locations = subject.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getModifiedMethod).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, List<PatchFile>> methodPatchMap = subject.getPatchList().stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(PatchFile::getModifiedMethod, Collectors.toCollection(LinkedList::new)));
        String resultFile = "./ModifiedLocationAnalysis.csv";
        StringBuilder result = new StringBuilder();
        result.append(subject.getName()).append(subject.getId()).append(",")
                .append(locations.size()).append(",").append("\n");
        System.out.println(result.toString());
        FileIO.writeStringToFile(resultFile, result.toString(), true);

    }

    // 获取一个Oracle 即每个bug 正确的修改位置
    public void obainCorrectModification() {

        setModification();

        String oracleFile = Constant.CorrectPatchInfo + "correct_modification";
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(subject.getName()).append(subject.getId()).append(",");
        if (containsCorretPatch()) {
            String locations = subject.getPatchList().stream().filter(Objects::nonNull)
                    .filter(PatchFile::isCorrectness)
                    .map(PatchFile::getModifiedMethod)
                    .distinct()
                    //.map(Method::getMethodNameString)
                    .collect(Collectors.joining("\t"));
            resultBuilder.append(locations).append("\n");
        } else {
            resultBuilder.append("\n");
        }
        FileIO.writeStringToFile(oracleFile, resultBuilder.toString(), true);
    }

    // set modification for every patch
    private void setModification() {
        for (PatchFile patchFile : subject.getPatchList()) {
            String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName()
                    + "/" + patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
            Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
            patchFile.setModifiedMethod(method.getMethodNameString());
        }
    }

    // set correctness for every patch
    private boolean containsCorretPatch() {
        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        boolean contains = false;
        List<PatchFile> patchFileList = subject.getPatchList();
        for (PatchFile patchFile : patchFileList) {
            CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(), String.valueOf(subject.getId()));
            if (correctPatchList.contains(correctPatch)) {
                patchFile.setCorrectness(true);
                contains = true;
            }
        }
        return contains;
    }

    // 获取当前subject正确的修改函数
    private String getCorrectModification() {
        String correctOracle = Constant.CorrectPatchInfo + "correct_modification";
        String oracle = FileIO.readFileToString(correctOracle);
        Map<String, String> orcaleMap = Arrays.stream(oracle.split("\n"))
                .map(s -> s.split(",", 2))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        return orcaleMap.get(subject.getName() + subject.getId());
    }

    // calculate query process
    private void QueryProcess() {

        String resultpath = "./test.csv";
        StringBuilder resultBuilder = new StringBuilder();

        setModification();

        Map<String, List<PatchFile>> methodPatchMap = subject.getPatchList().stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(PatchFile::getModifiedMethod,
                        Collectors.toCollection(LinkedList::new)));

        resultBuilder.append(subject.getName()).append(subject.getId()).append(",").append(subject.getPatchList().size()).append(",");
        List<PatchFile> remainList = subject.getPatchList();
        for (Entry<String, List<PatchFile>> entry : methodPatchMap.entrySet()) {
            String keyString = entry.getKey();
            if (getCorrectModification().equals(keyString)) {
                remainList.clear();
                remainList.addAll(entry.getValue());
                resultBuilder.append(remainList.size());
                break;
            } else {
                remainList.removeAll(entry.getValue());
                resultBuilder.append(remainList.size()).append(",");
            }
        }
        resultBuilder.append("\n");
        FileIO.writeStringToFile(resultpath, resultBuilder.toString(), true);
    }


    public static void main(String[] args) {
        String project = "Time";
        int Start = 1;
        int end = 27;
        for (int i = Start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                ModifiedLocationAnalysis modifiedLocationAnalysis = new ModifiedLocationAnalysis(subject);
                modifiedLocationAnalysis.MainProcess();
                //modifiedLocationAnalysis.obainCorrectModification();
                //modifiedLocationAnalysis.QueryProcess();
            }
        }

    }


}
