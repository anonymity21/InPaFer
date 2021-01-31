package entity;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import util.BuildFilePath;
import util.FastJsonParseUtil;
import util.FileIO;
import util.StateType;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableCollection {

    private Subject subject;
    //private Map<String, List<PatchFile>> varPatchMap = new HashMap<String, List<PatchFile>>();
    private Set<String> filterout = new HashSet<>(Arrays.asList("clock", "timestamp", "pData","random"));
    private List<VariableLine> variableLines = new LinkedList<VariableLine>();
    private HashMap<String, String> correctKeyValue = new LinkedHashMap<String, String>();


    public VariableCollection(Subject subject) {
        this.subject = subject;
    }

    public void mainProcess(String failingtest, int patchSize) {
        List<PatchFile> patchFiles = subject.getPatchList();
        Map<String, List<JSONObject>> variableMap = new LinkedHashMap<>();
        for (PatchFile patchFile : patchFiles) {
            String varPath = BuildFilePath.tmpState(patchFile.getPatchName(), subject, failingtest);
            String variableFile = FileIO.readFileToString(varPath);
            //System.out.println(variableFile);
            List<JSONObject> variableJsonList = Arrays.stream(variableFile.split("\n"))
                    .filter(StringUtils::isNotBlank)
                    .filter(line -> !(line.endsWith("START#0") || line.endsWith("END#0")))
                    .map(variableLine -> {
                        //System.out.println(variableLine.toString());
                        String[] array = variableLine.split(": \\{", 2);
                        if (array.length >= 2) {
                            array[1] = "{" + array[1];
                            String value = array[1].replaceAll("\\{null\\}", "\\{\\}")
                                    .replaceAll("\\\"<null>\\\"", "null");
                            JSONObject resultJsonObject = FastJsonParseUtil.jsonFormatter(value, array[0]);
                            resultJsonObject.put("patchName", patchFile);
                            return resultJsonObject;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull).collect(Collectors.toList());
            variableMap.put(patchFile.getPatchName(), variableJsonList);
        }
        int jsonSize = variableMap.values().stream()
                .filter(Objects::nonNull).mapToInt(List::size).min().orElse(0);
        if (jsonSize <= 0) {
            return;
        }
        int flag = -1;
        for (int i = 0; i < jsonSize; i++) {
            int index = i;
            flag = index;
            List<JSONObject> currentLineVariableList = patchFiles.stream().filter(Objects::nonNull)
                    .map(patchFile -> variableMap.get(patchFile.getPatchName()).get(index))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(currentLineVariableList)) {
                continue;
            }
            Set<String> keySet = currentLineVariableList.stream()
                    .flatMap(variableJson -> variableJson.keySet().stream())
                    .filter(StringUtils::isNotEmpty)
                    .filter(key -> {
                        for (String str : filterout) {
                            if (key.contains(str)) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toSet());

            Set<String> differentKey = new LinkedHashSet();
            for (String key : keySet) {
                if (key.equals("patchName")) {
                    continue;
                }
                Map<String, List<PatchFile>> valuePatchMap = currentLineVariableList.stream()
                        .filter(json -> Objects.nonNull(json.get(key)))
                        .collect(Collectors.groupingBy(variableJson -> variableJson.getString(key),
                                Collectors.mapping(variableJson -> variableJson.getObject("patchName", PatchFile.class), Collectors.toList())));
                if (valuePatchMap.size() > 1 && valuePatchMap.size() < patchSize) {
                    for (Entry<String, List<PatchFile>> entry : valuePatchMap.entrySet()) {

                        VariableLine variableLine = new VariableLine(key, entry.getKey(), StateType.UNCLEAR, entry.getValue());
                        variableLines.add(variableLine);
                        differentKey.add(key);
                    }
                }
            }
            if (variableLines.size() > 0) {
                log.info("Different Variable in : " + index);
                String fixedContent = FileIO.readFileToString(BuildFilePath.tmpState("fixed", subject, subject.getFailedTestList().get(0)));
                if (!fixedContent.equals("")) {
                    //collectCorrect(fixedContent, index, differentKey);
                }
                break;
            }
        }
        if (variableLines.size() == 0) {
            flag = -1;
            log.info("All variables are same ");
        }
        FileIO.writeStringToFile(BuildFilePath.tmpStateIndex(subject, failingtest), String.valueOf(flag), false);

    }

    public void collectCorrect(String fixedContent, int index, Set<String> differentKeyList) {
        //String fixedContent = FileIO.readFileToString(BuildFilePath.tmpState("fixed", subject));
        List<JSONObject> variableJsonList = Arrays.stream(fixedContent.split("\n"))
                .filter(StringUtils::isNotBlank)
                .filter(line -> !(line.endsWith("START#0") || line.endsWith("END#0")))
                .map(variableLine -> {
                    //System.out.println(variableLine.toString());
                    String[] array = variableLine.split(": \\{", 2);
                    if (array.length >= 2) {
                        array[1] = "{" + array[1];
                        String value = array[1].replaceAll("\\{null\\}", "\\{\\}")
                                .replaceAll("\\\"<null>\\\"", "null");
                        JSONObject resultJsonObject = FastJsonParseUtil.jsonFormatter(value, array[0]);
                        resultJsonObject.put("patchName", "initial");
                        return resultJsonObject;
                    }
                    return null;
                })
                .filter(Objects::nonNull).collect(Collectors.toList());


        //List<JSONObject> currentLineVariableList = variableJsonList.get(index)).collect(Collectors.toList());
        JSONObject object = variableJsonList.get(index);
        for (String key : differentKeyList) {
            correctKeyValue.put(key, object.getString(key));
        }

    }

    public static void main(String[] args) {
        Subject subject = new Subject("Chart", 25);
        subject.initPatchListByPath(Constant.AllPatchPath);
        VariableCollection variableCollection = new VariableCollection(subject);
        variableCollection.mainProcess(subject.getFailedTestList().get(0),subject.getPatchList().size());
        List<VariableLine> variableLines = variableCollection.getVariableLines();
        HashMap<String, String> correctValue = variableCollection.getCorrectKeyValue();
        System.out.println(variableLines.size());
    }
}
