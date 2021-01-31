package service;


import config.Constant;
import entity.PatchFile;
import entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.BuildFilePath;
import util.FileIO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraceMapService {

    private PatchFile patchFile;

    //public static String resultFile = Constant.LOG_FILE + "ObtainTrace.log";


    // 将插入补丁之后的trace映射在原代码上
    public boolean traceMap(Set<String> allDetelteLine, String failingTest) {
        log.info("Trace Correct Line");
        String traceFileName = BuildFilePath.tmpLine(patchFile.getPatchName(), patchFile.getSubject(), failingTest);

        String diffFileName = patchFile.getPatchPath();
        String fullClassName = patchFile.getFixedFile();
        try {
            String content = FileIO.readFileToString(traceFileName);
            if (content.equals("")) {
                log.error("Content of " + patchFile.getPatchName() + " Trace is Empty.");
                //FileIO.writeStringToLog(resultFile, "Content of " + patchFile.getPatchName() + " Trace is Empty.");
                return false;
            }

            String traceFinalFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject(), failingTest);

            BufferedReader diffReader = new BufferedReader(new FileReader(diffFileName));
            String formatClassName = fullClassName.replaceAll("\\/\\/", "").replaceAll("/", "\\.");
            if (formatClassName.startsWith(".")) {
                formatClassName = formatClassName.split("\\.", 2)[1];
            }
            String diffLine = diffReader.readLine();

            String[] contentArray = content.split("\n");
            int maxLine = 0;
            for (String line : contentArray) {
                String className = line.split("#")[0];
                if (className.contains("$")) {
                    className = className.split("\\$")[0];
                }
                className = className + ".java";
                // String className = line.split("#")[0] + ".java";
                if (formatClassName.contains(className)) {
                    int lineNum = Integer.parseInt(line.split("#")[line.split("#").length - 1]);
                    maxLine = Math.max(maxLine, lineNum);
                }
            }
            int[] newIndex = new int[maxLine + 10];
            LinkedHashSet<Integer> removedLineSet = new LinkedHashSet<Integer>();
            for (int i = 0; i <= maxLine; ++i) {
                newIndex[i] = 0;
            }
            int patchedLine = -1;
            int previousLine = -1;
            while (diffLine != null) {
                //log.info("diff " + diffLine + "\n");
                if (diffLine.startsWith("---") || diffLine.startsWith("+++")) {
                    diffLine = diffReader.readLine();
                    continue;
                } else if (diffLine.startsWith("@@")) {
                    // @@ -166,10 +166,10 @@
                    previousLine = Integer.valueOf(diffLine.split(",")[0].split("-")[1]);
                    patchedLine = Integer.valueOf(diffLine.split(",")[1].split("\\+")[1]);
                } else if (diffLine.startsWith("-") && (!diffLine.startsWith("---"))) {
                    previousLine += 1;
                    //log.info("addtag " + patchedLine + " " + maxLine);
                    if (patchedLine <= maxLine) {
                        newIndex[patchedLine] += 1;
                    }
                } else if (diffLine.startsWith("+") && (!diffLine.startsWith("+++"))) {
                    removedLineSet.add(patchedLine);
                    patchedLine += 1;
                    //log.info("decreasetag " + patchedLine + " " + maxLine);
                    if (patchedLine <= maxLine) {
                        newIndex[patchedLine] -= 1;
                    }
                } else {
                    patchedLine += 1;
                    previousLine += 1;
                }
                diffLine = diffReader.readLine();
            }
            for (int i = 1; i <= maxLine; i += 1) {
                newIndex[i] += newIndex[i - 1];
            }

            StringBuilder tmpLines = new StringBuilder();
            for (String line : contentArray) {
                String className = line.split("#")[0];
                if (className.contains("$")) {
                    className = className.split("\\$")[0];
                }
                className = className + ".java";
                //String className = line.split("#")[0] + ".java";
                int lineNum = Integer.parseInt(line.split("#")[line.split("#").length - 1]);

                if (formatClassName.contains(className)) {
                    //log.info("replace " + lineNum + " " + newIndex[lineNum]);
                    if (!removedLineSet.contains(lineNum)) {

                        int newLineNumber = newIndex[lineNum] + lineNum;
                        String tmpLine = className + "#" + newLineNumber;
                        // 所有其它补丁内对修改前代码 进行修改的行也不做考虑
                        boolean shouldDelete = false;
                        for (String deleteLine : allDetelteLine) {
                            if (deleteLine.contains(tmpLine)) {
                                shouldDelete = true;
                                break;
                            }
                        }
                        if (!shouldDelete) {
                            tmpLines.append(line.split(lineNum + "")[0]).append(newIndex[lineNum] + lineNum).append("\n");
                        }
                    }
                } else {
                    String tmpLine = className + "#" + lineNum;
                    // 所有其它补丁内对修改前代码 进行修改的行也不做考虑
                    boolean shouldDelete = false;
                    for (String deleteLine : allDetelteLine) {
                        if (deleteLine.contains(tmpLine)) {
                            shouldDelete = true;
                            break;
                        }
                    }
                    if (!shouldDelete) {
                        tmpLines.append(line).append("\n");
                    }
                }
            }
            FileIO.writeStringToFile(traceFinalFile, tmpLines.toString(), false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
    public boolean traceMapforFixedFile(Set<String> allDetelteLine, String traceFileName, String diffFileName, String fullClassName ,
                                        String traceFinalFile) {

       // String traceFileName = BuildFilePath.tmpLine(patchFile.getPatchName(), patchFile.getSubject(), failingTest);

       // String diffFileName = patchFile.getPatchPath();
      //  String fullClassName = patchFile.getFixedFile();
        try {
            String content = FileIO.readFileToString(traceFileName);
            if (content.equals("")) {
                log.error("Content of " + patchFile.getPatchName() + " Trace is Empty.");
                //FileIO.writeStringToLog(resultFile, "Content of " + patchFile.getPatchName() + " Trace is Empty.");
                return false;
            }

           // String traceFinalFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject(), failingTest);

            BufferedReader diffReader = new BufferedReader(new FileReader(diffFileName));
            String formatClassName = fullClassName.replaceAll("\\/\\/", "").replaceAll("/", "\\.");
            if (formatClassName.startsWith(".")) {
                formatClassName = formatClassName.split("\\.", 2)[1];
            }
            String diffLine = diffReader.readLine();

            String[] contentArray = content.split("\n");
            int maxLine = 0;
            for (String line : contentArray) {
                String className = line.split("#")[0];
                if (className.contains("$")) {
                    className = className.split("\\$")[0];
                }
                className = className + ".java";
                // String className = line.split("#")[0] + ".java";
                if (formatClassName.contains(className)) {
                    int lineNum = Integer.parseInt(line.split("#")[line.split("#").length - 1]);
                    maxLine = Math.max(maxLine, lineNum);
                }
            }
            int[] newIndex = new int[maxLine + 10];
            LinkedHashSet<Integer> removedLineSet = new LinkedHashSet<Integer>();
            for (int i = 0; i <= maxLine; ++i) {
                newIndex[i] = 0;
            }
            int patchedLine = -1;
            int previousLine = -1;
            while (diffLine != null) {
                //log.info("diff " + diffLine + "\n");
                if (diffLine.startsWith("---") || diffLine.startsWith("+++")
                || diffLine.startsWith("diff") || diffLine.startsWith("index")) {
                    diffLine = diffReader.readLine();
                    continue;
                } else if (diffLine.startsWith("@@")) {
                    // @@ -166,10 +166,10 @@
                    previousLine = Integer.valueOf(diffLine.split(",")[0].split("-")[1]);
                    patchedLine = Integer.valueOf(diffLine.split(",")[1].split("\\+")[1]);
                } else if (diffLine.startsWith("-") && (!diffLine.startsWith("---"))) {
                    previousLine += 1;
                    //log.info("addtag " + patchedLine + " " + maxLine);
                    if (patchedLine <= maxLine) {
                        newIndex[patchedLine] += 1;
                    }
                } else if (diffLine.startsWith("+") && (!diffLine.startsWith("+++"))) {
                    removedLineSet.add(patchedLine);
                    patchedLine += 1;
                    //log.info("decreasetag " + patchedLine + " " + maxLine);
                    if (patchedLine <= maxLine) {
                        newIndex[patchedLine] -= 1;
                    }
                } else {
                    patchedLine += 1;
                    previousLine += 1;
                }
                diffLine = diffReader.readLine();
            }
            for (int i = 1; i <= maxLine; i += 1) {
                newIndex[i] += newIndex[i - 1];
            }

            StringBuilder tmpLines = new StringBuilder();
            for (String line : contentArray) {
                String className = line.split("#")[0];
                if (className.contains("$")) {
                    className = className.split("\\$")[0];
                }
                className = className + ".java";
                //String className = line.split("#")[0] + ".java";
                int lineNum = Integer.parseInt(line.split("#")[line.split("#").length - 1]);

                if (formatClassName.contains(className)) {
                    //log.info("replace " + lineNum + " " + newIndex[lineNum]);
                    if (!removedLineSet.contains(lineNum)) {

                        int newLineNumber = newIndex[lineNum] + lineNum;
                        String tmpLine = className + "#" + newLineNumber;
                        // 所有其它补丁内对修改前代码 进行修改的行也不做考虑
                        boolean shouldDelete = false;
                        for (String deleteLine : allDetelteLine) {
                            if (deleteLine.contains(tmpLine)) {
                                shouldDelete = true;
                                break;
                            }
                        }
                        if (!shouldDelete) {
                            tmpLines.append(line.split(lineNum + "")[0]).append(newIndex[lineNum] + lineNum).append("\n");
                        }
                    }
                } else {
                    String tmpLine = className + "#" + lineNum;
                    // 所有其它补丁内对修改前代码 进行修改的行也不做考虑
                    boolean shouldDelete = false;
                    for (String deleteLine : allDetelteLine) {
                        if (deleteLine.contains(tmpLine)) {
                            shouldDelete = true;
                            break;
                        }
                    }
                    if (!shouldDelete) {
                        tmpLines.append(line).append("\n");
                    }
                }
            }
            FileIO.writeStringToFile(traceFinalFile, tmpLines.toString(), false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
      return true;
    }

    public static void main(String[] arg) {

        Subject subject = new Subject("Chart", 1);
        String PatchName = "jKali-Patch2";
        String path = Constant.AllPatchPath + "Chart/1/" + PatchName;
        PatchFile patchFile = new PatchFile(subject, path);
        patchFile.parsePatch();

        TraceMapService traceMapService = new TraceMapService(patchFile);
       // traceMapService.traceMap(new LinkedHashSet<>());

    }



}
