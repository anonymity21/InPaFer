package experiment;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import run.Runner;
import service.AllQueryService;
import util.BuildFilePath;
import util.FileIO;

import java.util.List;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OracleCalculation {

    private Subject subject;

    public boolean containsCorretPatch() {
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

    public void mainProcess(){

        int number = subject.getFailedTestList().size() < Constant.FailingTestNumber ?
                subject.getFailedTestList().size():Constant.FailingTestNumber;

        int queryNumber = 0;
        boolean hasLineDifference = false;
        boolean hasVarDifference = false;

        String resultFile = "./oracleProject.csv";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(subject.getName()).append(subject.getId());
        for(int i = 0; i < number; i++) {
           String failingTest = subject.getFailedTestList().get(i);
           String mapFilePath = BuildFilePath.tmpMapLine(subject, failingTest);
           String mapContent =  FileIO.readFileToString(mapFilePath);
           if(!mapContent.trim().equalsIgnoreCase("")){
               hasLineDifference =true;
               break;
           }
           VariableCollection variableCollection = new VariableCollection(subject);
           variableCollection.mainProcess(failingTest, subject.getPatchList().size());
           List<VariableLine> variableLines = variableCollection.getVariableLines();

           if(variableLines.size() != 0){
               hasVarDifference = true;
           }

        }

        if(hasLineDifference){
            stringBuilder.append(",yes,");
        }else {
            stringBuilder.append(",no,");
        }

        if(hasVarDifference){
            stringBuilder.append(",yes\n");
        }else {
            stringBuilder.append(",no\n");
        }
        log.info(subject.getName() + subject.getId()   + stringBuilder.toString());
        FileIO.writeStringToFile(resultFile, stringBuilder.toString(), true);
    }

    public static void main(String[] args){
        String project = "Lang";
        int start = 39;
        int end = 39;
        for (String arg : args) {
            if (arg.startsWith("-p=")) {
                // Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
                project = arg.substring("-p=".length());
            } else if (arg.startsWith("-s=")) {
                start = Integer.parseInt(arg.substring("-s=".length()));
            } else if (arg.startsWith("-e=")) {
                end = Integer.parseInt(arg.substring("-e=".length()));
            }
        }
        log.info("Start Running <<" + project +">> PatchVariationMain from " + start + " to " + end );
        for (int i = start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                //FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
                List<PatchFile> patchList = subject.getPatchList();
                if (!subject.exist()) {
                    if (!Runner.downloadSubject(subject)) {
                        continue;
                    }
                }
                OracleCalculation oracleCalculation = new OracleCalculation(subject);
                if(! oracleCalculation.containsCorretPatch()){
                        oracleCalculation.mainProcess();
                }

            }
        }

    }
}
