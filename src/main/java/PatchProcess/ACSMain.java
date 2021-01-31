package PatchProcess;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.Constant;
import entity.Subject;
import run.Runner;
import util.FileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liangjingjing on 2019/3/12.
 * This File is formating the patches from ACS HDrepair and Nopol
 * input : exist patch
 * output: a diff patch from a formated original file and a formated patched program
 */
public class ACSMain {
    static String tech = "Nopol2017";

    //static String project = "";
    //static String astorPatch = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/" + tech + "/" + project;
    static String Patch = "/Users/liangjingjing/WorkSpace/Project/Patch-Correctness/tool/patches/";
    static String logFile = Constant.LOG_FILE + "/formatPatch/" + tech;
    static String result = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/";
    static String[] projectlist = {"Chart", "Closure", "Math", "Time", "Lang"};

    //static String bugid = "";


    // specific for ACS HDrepair Nopol
    private static void getFormatDiffPatch(File patchFile, String bug) {


        String fileName = patchFile.getName();
        String project = bug.split("-")[0];
        if (!Arrays.asList(projectlist).contains(project)) {
            System.out.println("Illegal Project Name " + project);
            return;
        }
        String bugid = bug.split("-")[1];

        String newresult = result + project + "/" + bugid + "/";

        Subject subject = new Subject(project, Integer.parseInt(bugid));
        Format format = new Format(subject);
        String projectHome = subject.getHome();

        String patchContent = FileIO.readFileToString(patchFile);
        if (patchContent.equals("")) {
            System.out.println("Patch File Content is NUll: " + patchFile.getName());
            return;
        }
        String fixedFile = "";
        for (String line : patchContent.split("\n")) {
            //--- Math3b/src/main/java/org/apache/commons/math3/util/MathArrays.java	2017-05-09 06:17:37.000000000 +0800
            if (line.startsWith("---")) {
                fixedFile = projectHome + "/" + line.split("/", 2)[1].split("\t")[0];
                if (!fixedFile.endsWith(".java")) {
                    fixedFile = fixedFile.split("\\.java")[0] + ".java";
                }
                break;
            }
        }

        Runner.compileSubject(subject);

        if (!new File(fixedFile).exists()) {
            FileIO.writeStringToFile(logFile, fixedFile + "does not exist in " + patchFile.getName(), true);
            System.out.println(fixedFile + "does not exist in " + patchFile.getName());
            return;
        }

        FileIO.backUpFile(fixedFile, fixedFile + ".bak");
        FileIO.backUpFile(fixedFile, fixedFile + ".patched");
        format.formatSingleFile(fixedFile);


        if (Runner.patchFile(fixedFile + ".patched", patchFile.getAbsolutePath())) {
            String diffFile = newresult;
            if (!new File(diffFile).exists()) {
                new File(diffFile).mkdirs();
            }
            System.out.println("Patch " + fileName + " Success!");
            format.formatSingleFile(fixedFile + ".patched");
            diffFile = newresult + tech + "-" + fileName;
            Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);
        } else {
            System.out.println("Patch " + fileName + " Failed!");
            FileIO.writeStringToFile(logFile, "Patching " + fileName + " Failed!", true);
        }


        FileIO.restoreFile(fixedFile, fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".patched");
    }

    private static LinkedHashMap<String, List<String>> parsePatchInfo(String fileName) {

        LinkedHashMap<String, List<String>> bugPatchMap = new LinkedHashMap<>();
        JsonParser parser = new JsonParser();

        try {
            JsonArray array = (JsonArray) parser.parse(new FileReader(fileName));
            for (int i = 0; i < array.size(); i++) {
                JsonObject subObject = array.get(i).getAsJsonObject();
                String bugid = subObject.get("bug_id").getAsString();
                String project = subObject.get("project").getAsString();
                String patchID = subObject.get("ID").getAsString();
                String key = project + "-" + bugid;
                List<String> patchList;
                if (bugPatchMap.containsKey(key)) {
                    patchList = bugPatchMap.get(key);
                } else {
                    patchList = new LinkedList<>();
                }
                patchList.add(patchID);
                bugPatchMap.put(key, patchList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bugPatchMap;
    }

    public static void main(String[] args) {

        System.out.println(System.getProperty("java.version"));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        System.out.println(dateFormat.format(date));

        FileIO.writeStringToFile(logFile, dateFormat.format(date) + "\n", true);

        String patchinfoFile = Patch + "/" + tech;
        LinkedHashMap<String, List<String>> bugPatchMap = parsePatchInfo(patchinfoFile);

        System.out.println("Totol: " + bugPatchMap.size());

        int i = 1;
/*        for(Map.Entry<String, List<String>> entry: bugPatchMap.entrySet()){
            String bugid = entry.getKey();
            for(String patchid: entry.getValue()){
                System.out.println("Process: " + i + "/" + bugPatchMap.size()  + " " + patchid);
                i++;
                File patchfile = new File(Patch + patchid);
                getFormatDiffPatch(patchfile, bugid);
            }
        }*/

        File patchfile = new File(Patch + "Patch175");
        getFormatDiffPatch(patchfile, "Math-87");

//Math_87_Nopol2017-Patch175
    }
}
