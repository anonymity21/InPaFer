package experiment;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.CorrectPatch;
import entity.LineInfo;
import entity.PatchFile;
import entity.Subject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import service.QueryService;
import util.FileIO;

import java.util.List;

@Slf4j
@Builder
@Data
public class TraceQuery {


    private Subject subject;


    public TraceQuery(Subject subject) {
        this.subject = subject;
    }


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


    public void queryProcess() {
        StringBuilder resultBuilder = new StringBuilder();
        String resultPath = "./test.csv";

        resultBuilder.append(subject.getName()).append(subject.getId()).append(",").append(subject.getPatchList().size()).append(",");

        QueryService queryService = new QueryService(subject);
        queryService.initSet();
        int queryNumber = 0;


        while (!queryService.isTerminate()) {

            if (!containsCorretPatch()) {
                log.error("need manually check");
                break;
            }

            queryNumber++;
            LineInfo currentLine = queryService.pickOne();

            log.info("----------- QueryNumber: " + queryNumber);
            log.info("Pick line info {}", currentLine.getLineName(), currentLine.getPatchList().toString());

            boolean isTrue = false;
            for (PatchFile patchFile : currentLine.getPatchList()) {
                if (patchFile.isCorrectness()) {
                    isTrue = true;
                    break;
                }
            }

            log.info("Answer: " + isTrue);
            if (!isTrue) {
                queryService.processAfterWrongTrace(currentLine);
            } else {
                queryService.processAfterRightTrace(currentLine);
            }
            //processMap = queryService.getProcessMap();
            //patchList = queryService.getPatchList();

            log.info("ProcessList Size: " + queryService.getProcessList().size());
            log.info("PatchList Size: " + queryService.getCandidatePatchList().size());
            log.info("QueryNumber: " + queryNumber);
            resultBuilder.append(queryService.getCandidatePatchList().size()).append(",");
            //log.info("PatchList: " + patchList.toString());
            //queryResult = queryResult + patchList.size() + ",";
            //restore(subject, currentLine);

        }
        resultBuilder.append("\n");
        FileIO.writeStringToFile(resultPath, resultBuilder.toString(), true);
    }


    public static void main(String[] args) {
        String project = "Chart";
        int Start = 1;
        int end = 26;
        for (int i = Start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                TraceQuery traceQuery = new TraceQuery(subject);
                traceQuery.queryProcess();

            }
        }
    }

}
