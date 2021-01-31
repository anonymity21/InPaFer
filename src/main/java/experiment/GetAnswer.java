package experiment;

import Main.MethodInitialization;
import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import run.Runner;
import service.MapService;
import util.BuildFilePath;
import util.FileIO;

import java.util.*;

@Slf4j
@AllArgsConstructor

public class GetAnswer {

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
            }else{
            }
        }
        return contains;
    }
    public void mainProcessForLine(Subject subject) {

        log.info("RUNNING Program for Line ");
        // only running three failing test
        int number = subject.getFailedTestList().size() < Constant.FailingTestNumber ?
                subject.getFailedTestList().size() : Constant.FailingTestNumber;

        for (int i = 0; i < number; i++) {
            MapService.getDifferentIndex(subject.getFailedTestList().get(i), subject);
        }
    }

    public void mainProcessForState(Subject subject) {

        log.info("RUNNING Program for State ");
        // only running three failing test
        int number = subject.getFailedTestList().size() < Constant.FailingTestNumber ?
                subject.getFailedTestList().size() : Constant.FailingTestNumber;

        for (int i = 0; i < number; i++) {
            VariableCollection variableCollection = new VariableCollection(subject);
            variableCollection.mainProcess(subject.getFailedTestList().get(i), subject.getPatchList().size());
        }
    }


    public static void main(String[] args){
        String project = "Math";
        int start = 73;
        int end = 73;
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
        log.info("Start Running <<" + project +">> GetAnswer from " + start + " to " + end );
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
                GetAnswer getAnswer = new GetAnswer(subject);
                if(! getAnswer.containsCorretPatch()){
                    getAnswer.mainProcessForLine(subject);
                    getAnswer.mainProcessForState(subject);
                }
            }
        }
    }
}
