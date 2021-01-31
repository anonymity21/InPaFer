package script;

import entity.Pair;
import util.FileIO;

import java.util.*;
import java.util.stream.Collectors;

public class ObtainData {

    public static void getData(){
        String readingFile = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/interactive-patch-filter/DataAnaylysis/fse_new/totaldata.csv";
        String content = FileIO.readFileToString(readingFile);
        String writeFile = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/interactive-patch-filter/DataAnaylysis/fse_new/total-all.csv";
        List<DataInfo> numberRemainPatch = new ArrayList<>();
        for(String line : content.split("\n")){
            if(line.startsWith("project")){
                continue;
            }
            String[] data = line.split(",");
            if (data.length < 6) {
                continue;
            }
            DataInfo dataInfo = new DataInfo();
            dataInfo.setProjectName(data[0]);
            dataInfo.setInit(Integer.parseInt(data[1]));
            dataInfo.setFinish(Integer.parseInt(data[2]));
            dataInfo.setQueryNumber(Double.parseDouble(data[3]));
            dataInfo.setRadio(Double.parseDouble(data[4].replaceAll("\\%", "")));
            dataInfo.setContain(data[5]);
            numberRemainPatch.add(dataInfo);
        }
        Map<String, Long> countMap = numberRemainPatch.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(dataInfo -> dataInfo.getQueryNumber() + "_" + dataInfo.getRadio(), Collectors.counting()));

        for (int i = 0; i < numberRemainPatch.size(); ++i) {
            DataInfo data1 = numberRemainPatch.get(i);
            for (int j= 0; j < numberRemainPatch.size(); ++j) {
                DataInfo data2 = numberRemainPatch.get(j);
                if (!countMap.containsKey(data1.getQueryNumber() + "_" + data2.getRadio())) {
                    countMap.put(data1.getQueryNumber() + "_" + data2.getRadio(), 0L);
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, Long> entry: countMap.entrySet()){
            String queryNumber = entry.getKey().split("_")[0];
            String remainPatch = entry.getKey().split("_")[1] + "%";
            stringBuilder.append(queryNumber).append(",").append(remainPatch).append(entry.getValue()).append("\n");
        }
        FileIO.writeStringToFile(writeFile, stringBuilder.toString());

    }


    public static void main(String[] args){
        getData();
    }
}
