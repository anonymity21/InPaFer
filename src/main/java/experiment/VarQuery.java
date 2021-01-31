package experiment;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.*;
import lombok.extern.slf4j.Slf4j;
import service.QueryVarService;
import util.FileIO;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class VarQuery {

    private Subject subject;

    public VarQuery(Subject subject) {
        this.subject = subject;
    }

    private boolean containsCorretPatch() {
        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        boolean contains = false;
        List<PatchFile> patchFileList = subject.getPatchList();
        for (PatchFile patchFile : patchFileList) {
            CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(),
                    String.valueOf(subject.getId()));
            if (correctPatchList.contains(correctPatch)) {
                patchFile.setCorrectness(true);
                contains = true;
            }
        }
        return contains;
    }

    public void queryProcess() {
        StringBuilder resultBuilder = new StringBuilder();
        String resultPath = "./varquery.csv";
        resultBuilder.append(subject.getName()).append(subject.getId()).append(",")
                .append(subject.getPatchList().size()).append(",");

        VariableCollection variableCollection = new VariableCollection(subject);
        variableCollection.mainProcess(subject.getFailedTestList().get(0), subject.getPatchList().size());
        List<VariableLine> variableLines = variableCollection.getVariableLines();
        HashMap<String, String> correctHashMap = variableCollection.getCorrectKeyValue();

        QueryVarService queryService = new QueryVarService(subject, variableLines);
        int queryNumber = 0;

        while (!queryService.isTerminate()) {

            queryNumber++;
            VariableLine currentLine = queryService.pickOne();
            log.info("----------- QueryNumber: " + queryNumber);
            log.info("current patch files {}, var name: {}, value: {}", currentLine.getPatchList().toString(),
                    currentLine.getVarName(), currentLine.getValue());
            boolean isTrue = getAnswer(currentLine, correctHashMap);

            log.info("Answer: " + isTrue);
            if (!isTrue) {
                queryService.processAfterWrongTrace(currentLine);
            } else {
                queryService.processAfterRightTrace(currentLine);
            }
            log.info("ProcessList Size: " + queryService.getProcessList().size());
            log.info("PatchList Size: " + queryService.getCandidatePatchFiles().size());
            log.info("QueryNumber: " + queryNumber);
            resultBuilder.append(queryService.getCandidatePatchFiles().size()).append(",");

        }
        resultBuilder.append("\n");
        FileIO.writeStringToFile(resultPath, resultBuilder.toString(), true);
    }

    private boolean getAnswer(VariableLine currentLine, HashMap<String, String> correctHashMap) {
        boolean isTrue = false;
        if (containsCorretPatch()) {
            for (PatchFile patchFile : currentLine.getPatchList()) {
                if (patchFile.isCorrectness()) {
                    isTrue = true;
                    break;
                }
            }
        } else {
            String key = currentLine.getVarName();
            isTrue = currentLine.getValue().equals(correctHashMap.get(key));
        }
        return isTrue;
    }

    public static void main(String[] args) {
        String project = "Lang";
        int Start = 1;
        int end = 65;
        for (int i = Start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                VarQuery varQuery = new VarQuery(subject);
                varQuery.queryProcess();
            }
        }
    }

}
