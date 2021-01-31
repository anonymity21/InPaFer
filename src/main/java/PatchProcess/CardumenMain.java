package PatchProcess;

import config.Constant;
import entity.PatchFile;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import run.Runner;
import util.FileIO;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;

/**
 * Created by liangjingjing on 2019/3/14.
 * This File is formating the patches from Cardumen
 * input : exist patch
 * output: a diff patch from a formated original file and a formated patched program
 */
@Slf4j
public class CardumenMain {
    static String tech = "Cardumen";
    static String project = "Lang";
    static String astorPatch = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/" + tech + "/" + project;
    static String logFile = Constant.LOG_FILE + "/formatPatch/" + tech + "." + project + ".log";
    static String result = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/" + project + "/";

    //static String bugid = "";

    private static Vector<String> getBuggyCode(String path) {
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(file));
        String line = null;
        Vector<String> codes = new Vector<String>();
        try {
            while ((line = br.readLine()) != null) {
               /* if (line.trim().startsWith("//")) {
                    continue;
                }*/
                codes.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return codes;
    }

    private static boolean qualify(String s, String t) {
        int n = s.length(), m = t.length();
        return s.length() > 0 && s.charAt(0) == t.charAt(0) && s.charAt(n - 1) == t.charAt(m - 1);
    }

    private static Vector<Integer> getCost(String s, String t, Vector<Integer> loc) {
        int n = s.length(), m = t.length();
        int[][][] f = new int[n + 1][m + 1][2];
        int ex = 4, inf = 100000000;
        f[0][0][0] = inf;
        f[0][0][1] = inf;
        for (int i = 0; i <= n; ++i) {
            for (int j = 0; j <= m; ++j) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 0 || j == 0) {
                    f[i][j][0] = inf;
                    f[i][j][1] = inf;
                    continue;
                } else if (i == 1 && j == 1) {
                    f[i][j][0] = inf;
                    f[i][j][1] = ex;
                    continue;
                }
                f[i][j][0] = f[i][j][1] = inf;
                if (s.charAt(i - 1) == t.charAt(j - 1)) {
                    f[i][j][1] = Math.min(f[i - 1][j - 1][0] + ex, f[i - 1][j - 1][1]) - 2;
                }
                f[i][j][0] = Math.min(Math.min(f[i - 1][j][0], f[i][j - 1][0]), Math.min(f[i - 1][j][1], f[i][j - 1][1]) + ex * 2);
            }
        }
        int cost = inf;
        int best = -1;
        for (int i = 0; i < loc.size(); ++i) {
            int len = loc.elementAt(i);
            int current_cost = f[len][m][1] * 10 + len;
            if (current_cost < cost) {
                cost = current_cost;
                best = i;
            }
        }
        Vector<Integer> res = new Vector<Integer>();
        res.add(cost);
        res.add(best);
        return res;
    }

    public static boolean applyPatch(PatchFile patchFile, String patchedFile) {

        boolean result = true;
        Vector<String> codes = getBuggyCode(patchedFile);
        String buggy_content = patchFile.getBuggyContent();
        String fixed_content = patchFile.getFixedContent();
        //int patchId = patchFile.getPatchId();

        int m = buggy_content.length();
        int n = codes.size();
        int best_cost = 10000000;
        int best_l = -1, best_r = -1;
        while (true) {
            for (int i = 0; i < n; ++i) {
                String now = "", longest_code = "";
                Vector<Integer> fit_r = new Vector<Integer>();
                Vector<Integer> fit_location = new Vector<Integer>();
                for (int j = i; j < n && now.length() <= m; ++j) {

                    if (!codes.elementAt(j).startsWith("//")) {
                        now = now + codes.elementAt(j).trim();
                        if (qualify(now, buggy_content)) {
                            fit_r.add(j);
                            fit_location.add(now.length());
                            longest_code = now;
                        }
                    }
                }
                if (longest_code.length() == 0) {
                    continue;
                }
                Vector<Integer> res = getCost(longest_code, buggy_content, fit_location);
                int current_cost = res.elementAt(0);
                int current_r = res.elementAt(1) >= 0 ? fit_r.elementAt(res.elementAt(1)) : -1;
                // int current_r = fit_r.elementAt(res.elementAt(1));
                if (current_cost < best_cost) {
                    best_cost = current_cost;
                    best_l = i;
                    best_r = current_r + 1;
                }
            }
            int where = buggy_content.indexOf(" ");
            if (where != -1) {
                int first_dot = buggy_content.indexOf(".");
                if (first_dot != -1 && first_dot < where) {
                    buggy_content = buggy_content.substring(first_dot + 1);
                    continue;
                }
            }
            if (best_l == -1) {
                System.out.println("parse failed!");
                result = false;
                return result;
            } else {
                break;
            }
        }

        StringBuilder ans_string = new StringBuilder();

        for (int i = best_l; i < best_r; ++i) {
            ans_string.append(codes.elementAt(i)).append("\n");
        }
        ans_string.append("\n\n");
        ans_string.append(fixed_content);

        System.out.println(ans_string);
        FileIO.writeStringToLog(logFile, ans_string.toString());

        StringBuilder fixed_code = new StringBuilder();

        for (int i = 0; i < best_l; ++i) {
            fixed_code.append(codes.elementAt(i)).append("\n");
        }

        for (String line : fixed_content.split("\n")) {
            if (line.trim().equals("")) {
                continue;
            }
            fixed_code.append(line).append("\n");
        }

        for (int i = best_r; i < n; ++i) {
            fixed_code.append(codes.elementAt(i)).append("\n");
        }

        FileIO.writeStringToFile(patchedFile, fixed_code.toString());
        return result;
    }

    // specific for Cardumen
    private static void getFormatDiffPatch(File patchFile, String bugDir) {

        if (!patchFile.getName().endsWith(".txt")) {
            return;
        }
        // filename patch1.txt
        String fileName = patchFile.getName();
        String patchid = fileName.split(".txt")[0];
        String bugid = bugDir.split(project)[1];

        Subject subject = new Subject(project, Integer.parseInt(bugid));
        Format format = new Format(subject);
        String projectHome = subject.getHome() + subject.getSsrc();

        String patchContent = FileIO.readFileToString(patchFile);
        if (patchContent.equals("")) {
            return;
        }
        String fixedFile = "";
        for (String line : patchContent.split("\n")) {
            if (line.startsWith("---")) {
                // fixedFile: --- /original/org/jfree/data/time/TimeSeries.java
                fixedFile = projectHome + "/" + line.split("original")[1].trim();
                break;
            }
        }

        Runner.compileSubject(subject);

        FileIO.backUpFile(fixedFile, fixedFile + ".bak");
        FileIO.backUpFile(fixedFile, fixedFile + ".patched");
        format.formatSingleFile(fixedFile);
        format.formatSingleFile(fixedFile + ".patched");

        // Apply Patch
        PatchFile pFile = new PatchFile(subject, patchFile.getAbsolutePath());

        System.out.println("Apply " + fileName + " !");
        FileIO.writeStringToLog(logFile, "Apply " + fileName + " !");

        if (applyPatch(pFile, fixedFile + ".patched")) {
            subject.deleteTarget();
            if (!Runner.compileSubject(subject)) {
                System.err.println("After Patch " + fileName + " Compile Failed!");
                FileIO.writeStringToLog(logFile, "After Patch " + fileName + " Compile Failed!");
            }
            System.out.println("Patch " + fileName + " Success!");
            FileIO.writeStringToLog(logFile, "Patch " + fileName + " Success!");

            String diffFile = result + bugid;
            if (!new File(diffFile).exists()) {
                new File(diffFile).mkdirs();
            }
            diffFile = diffFile + "/" + tech + "-" + patchid;
            Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);
        } else {
            System.err.println("Patch " + fileName + " Failed!");
            FileIO.writeStringToLog(logFile, "Patching " + fileName + " Failed!");
        }
        FileIO.restoreFile(fixedFile, fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".patched");
    }

/*    private static void formatAndDiff(File patchFile, String bugDir){
        // filename patch1.txt
        String fileName = patchFile.getName();
        String patchid = fileName.split(".txt")[0];
        String bugid = bugDir.split(project)[1];

        Subject subject = new Subject(project, Integer.parseInt(bugid));
        Format format = new Format(subject);
        String projectHome = subject.getHome()+subject.getSsrc();

        String patchContent = FileIO.readFileToString(patchFile);
        if(patchContent.equals("")){
            return;
        }
        String fixedFile = "";
        for(String line: patchContent.split("\n")){
            if(line.startsWith("---")){
                // fixedFile: --- /original/org/jfree/data/time/TimeSeries.java
                fixedFile = projectHome + "/" + line.split("original")[1].trim();
                break;
            }
        }

        Runner.compileSubject(subject);

        FileIO.backUpFile(fixedFile, fixedFile+".bak");
        FileIO.backUpFile(fixedFile, fixedFile+".patched");


        format.formatSingleFile(fixedFile);
        format.formatSingleFile(fixedFile+".patched");

        String diffFile = result+ bugid ;
        if(!new File(diffFile).exists()){
            new File(diffFile).mkdirs();
        }
        diffFile = diffFile  + "/" + tech+"-"+patchid;
        Runner.diff2File(fixedFile, fixedFile+".patched", diffFile);
        FileIO.restoreFile(fixedFile, fixedFile+ ".bak");
        FileIO.deleteFile(fixedFile+".bak");
        FileIO.deleteFile(fixedFile+".patched");

    }*/


    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        System.out.println(dateFormat.format(date));

        FileIO.writeStringToLog(logFile, dateFormat.format(date));
        //Runner.compileSubject(new Subject("Math", 30));

        int fileNum = 0;

        File[] files = new File(astorPatch).listFiles();
        if (Objects.isNull(files) || files.length <= 0) {
            log.error("astor patch file is empty!");
            return;
        }

        for (File bugDir : files) {
            if (!bugDir.isDirectory()) {
                continue;
            }
            fileNum++;
            System.out.println(fileNum);


            FileIO.writeStringToLog(logFile, "File Number: " + fileNum);

            System.out.println("============================================================================");
            System.out.println("Process: " + bugDir.getName());
            FileIO.writeStringToLog(logFile, "============================================================================");
            FileIO.writeStringToLog(logFile, "Process: " + bugDir.getName());
            File[] bugDirFiles = bugDir.listFiles();
            if (Objects.isNull(bugDirFiles) || bugDirFiles.length <= 0) {
                continue;
            }
            for (int i = 1; i <= bugDirFiles.length; i++) {
                File patchFile = bugDirFiles[i - 1];
                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Process: " + i + "/" + bugDirFiles.length + " " + patchFile.getName());
                FileIO.writeStringToLog(logFile, "--------------------------------------------------------------------------");
                FileIO.writeStringToLog(logFile, "Process: " + i + "/" + bugDirFiles.length + " " + patchFile.getName());
                getFormatDiffPatch(patchFile, bugDir.getName());
                System.out.println("Complete: " + i + "/" + bugDirFiles.length + " " + patchFile.getName());
                FileIO.writeStringToLog(logFile, "Complete: " + i + "/" + bugDirFiles.length + " " + patchFile.getName());
                System.out.println("--------------------------------------------------------------------------");
                FileIO.writeStringToLog(logFile, "--------------------------------------------------------------------------");
            }
            System.out.println("============================================================================");

        }
        // getFormatDiffPatch(new File("/Users/liangjingjing/WorkSpace/Project/astor-experiments/Cardumen/Math/Math20/patch49.txt"), "Math20");
        //formatAndDiff(new File("/Users/liangjingjing/WorkSpace/Project/astor-experiments/CapGen/Chart/Chart24/patch_14.txt"), "Chart24");
    }


}
