package PatchProcess;

/**
 * Created by liangjingjing on 2019/3/13.
 * This File is formating the patches from  CapGen and DeepRepair
 * input : exist patch
 * output: a diff patch from a formated original file and a formated patched program
 */

import config.Constant;
import entity.Subject;
import run.Runner;
import util.FileIO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CapGenMain {
    static String tech = "CapGen";
    static String project = "Lang";
    static String astorPatch = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/" + tech + "/" + project;
    static String logFile = Constant.LOG_FILE + "/formatPatch/" + tech + ".log";
    static String result = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/" + project + "/";

    //static String bugid = "";


    // specific for Cardumen Deeprepair
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
        String projectHome = subject.getHome() + "/" + subject.getSsrc();

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
        //format.formatSingleFile(fixedFile+".patched");


        if (Runner.patchFile(fixedFile + ".patched", patchFile.getAbsolutePath())) {
            String diffFile = result + bugid;
            if (!new File(diffFile).exists()) {
                new File(diffFile).mkdirs();
            }
            System.out.println("Patch " + fileName + " Success!");
            format.formatSingleFile(fixedFile + ".patched");
            diffFile = diffFile + "/" + tech + "-" + patchid;
            Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);
        } else {
            System.out.println("Patch " + fileName + " Failed!");
            FileIO.writeStringToFile(logFile, "Patching " + fileName + " Failed!", true);
        }


        FileIO.restoreFile(fixedFile, fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".patched");
    }

    private static void formatAndDiff(File patchFile, String bugDir) {
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

        String diffFile = result + bugid;
        if (!new File(diffFile).exists()) {
            new File(diffFile).mkdirs();
        }
        diffFile = diffFile + "/" + tech + "-" + patchid;
        Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);
        FileIO.restoreFile(fixedFile, fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".patched");

    }


    public static void main(String args[]) {

        System.out.println(System.getProperty("java.version"));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        System.out.println(dateFormat.format(date));

        FileIO.writeStringToFile(logFile, dateFormat.format(date) + "\n", true);

        for (File bugDir : new File(astorPatch).listFiles()) {
            if (!bugDir.isDirectory()) {
                continue;
            }
            if (!bugDir.getName().contains("Lang57")) {
                continue;
            }
            System.out.println("Process: " + bugDir.getName());
            for (int i = 1; i <= bugDir.listFiles().length; i++) {
                File patchFile = bugDir.listFiles()[i - 1];
                System.out.println("Process: " + i + "/" + bugDir.listFiles().length + " " + patchFile.getName());
                getFormatDiffPatch(patchFile, bugDir.getName());
            }
        }

        //getFormatDiffPatch(new File("/Users/liangjingjing/WorkSpace/Project/astor-experiments/CapGen/Chart/Chart1/patch_99.txt"), "Chart1");
        //formatAndDiff(new File("/Users/liangjingjing/WorkSpace/Project/astor-experiments/CapGen/Chart/Chart24/patch_14.txt"), "Chart24");
    }


}
