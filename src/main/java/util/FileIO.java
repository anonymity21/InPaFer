package util;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileIO {

    public static String readFileToString(String filePath) {
        if (filePath == null) {

            return "";
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            // LevelLogger.error(__name__ + "#readFileToString Illegal input file path : " +
            // filePath);
            return "";
        }
        return readFileToString(file);
    }

    public static String readFileToString(File file) {
        if (file == null) {
            // LevelLogger.error(__name__ + "#readFileToString Illegal input file : null.");
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        try {
            in = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            char[] ch = new char[1024];
            int readCount = 0;
            while ((readCount = inputStreamReader.read(ch)) != -1) {
                stringBuilder.append(ch, 0, readCount);
            }
            inputStreamReader.close();
            in.close();

        } catch (Exception e) {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e1) {
                    return "";
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    return "";
                }
            }
        }
        return stringBuilder.toString();
    }

    public static boolean writeStringToLog(String logFile, String info) {
        return writeStringToFile(logFile, info + "\n", true);
    }

    public static boolean writeStringToLog(File logFile, String info) {
        return writeStringToFile(logFile, info + "\n", true);
    }

    public static boolean writeStringToFile(File file, String string) {
        return writeStringToFile(file, string, false);
    }

    public static boolean writeStringToFile(String filePath, String string) {
        return writeStringToFile(filePath, string, false);
    }

    public static boolean writeStringToFile(String filePath, String string, boolean append) {
        if (filePath == null) {
            // LevelLogger.error(__name__ + "#writeStringToFile Illegal file path : null.");
            return false;
        }
        File file = new File(filePath);
        return writeStringToFile(file, string, append);
    }

    public static boolean writeStringToFile(File file, String string, boolean append) {
        if (file == null || string == null) {
            // LevelLogger.error(__name__ + "#writeStringToFile Illegal arguments : null.");
            return false;
        }
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                // LevelLogger.error(__name__ + "#writeStringToFile Create new file failed : " +
                // file.getAbsolutePath());
                return false;
            }
        }
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8));
            bufferedWriter.write(string);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static void deleteComments(String filePath) {
        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            StringBuilder result = new StringBuilder();
            boolean breakContinue = false;
            while (br.ready()) {
                line = br.readLine();
                if (line.trim().equals("")) {
                    continue;
                }
                if (line.trim().startsWith("//")) {
                    continue;
                }
                if (line.trim().startsWith("/*")) {
                    breakContinue = true;
                }
                if (line.trim().endsWith("*/")) {
                    breakContinue = false;
                    continue;
                }
                if (!breakContinue) {
                    result.append(line).append("\n");
                }
            }
            br.close();
            String content = result.toString()
                    .replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
            FileIO.writeStringToFile(filePath, content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * public static String cleanCommons(String content) { content =
     * content.replaceAll(
     * "\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
     * //content = content.replaceAll("\n", ""); return content; }
     */

    public static void normalizeFile(String filePath) {
        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            StringBuilder result = new StringBuilder();

            while (br.ready()) {
                line = deleteZhushi(br.readLine());
                if (!line.trim().startsWith("{") && !line.trim().startsWith("@") && !line.endsWith("{")
                        && !line.endsWith(";") && !line.endsWith("}")) {
                    result.append(line.replaceAll("   ", ""));
                    continue;
                }
                result.append(line).append("\n");
            }
            br.close();
            FileIO.writeStringToFile(filePath, result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void backUpFile(String source, String sourceCopy) {
        File sourceFile = new File(source);
        File sourceCopyFile = new File(sourceCopy);
        try {
            if (sourceCopyFile.exists()) {
                FileUtils.copyFile(sourceCopyFile, sourceFile);
            } else {
                FileUtils.copyFile(sourceFile, sourceCopyFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(String source, String sourceCopy) {
        // TODO Auto-generated method stub
        File sourceFile = new File(source);
        File sourceCopyFile = new File(sourceCopy);
        try {
            FileUtils.copyFile(sourceFile, sourceCopyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void restoreFile(String source, String sourceCopy) {
        File sourceFile = new File(source);
        File sourceCopyFile = new File(sourceCopy);
        if (sourceCopyFile.exists()) {
            try {
                FileUtils.copyFile(sourceCopyFile, sourceFile);
                deleteFile(sourceCopy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFile(String file) {
        File deleteFile = new File(file);
        if (deleteFile.exists()) {
            try {
                FileUtils.forceDelete(deleteFile);
                // FileUtils.deleteQuietly(deleteFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] getClassPath() {
        String property = System.getProperty("java.class.path", ".");
        // System.out.println(property);

        return property.split(File.pathSeparator);
    }

    public static List<File> getAllFile(File file, List<File> fileList) {
        if (fileList == null) {
            fileList = new LinkedList<File>();
        }
        for (File f : Objects.requireNonNull(file.listFiles())) {
            if (f.isDirectory()) {
                getAllFile(f, fileList);
            } else if (f.isFile()) {
                fileList.add(f);
            }
        }
        return fileList;
    }

    private static String deleteZhushi(String line) {
        int index = line.indexOf("//");
        return index >= 0 ? line.substring(0, index) : line;
    }

    /**
     * generate {@code CompilationUnit} from {@code ICompilationUnit}
     *
     * @param icu
     * @return
     */
    public static CompilationUnit genASTFromICU(ICompilationUnit icu) {
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        astParser.setCompilerOptions(options);
        astParser.setSource(icu);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        return (CompilationUnit) astParser.createAST(null);
    }

    /**
     * generate {@code CompilationUnit} from source code based on the specific type
     * (e.g., {@code ASTParser.K_COMPILATION_UNIT})
     *
     * @param icu
     * @param type
     * @return
     */
    public static CompilationUnit genASTFromSource(String icu, int type) {
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        astParser.setCompilerOptions(options);
        astParser.setSource(icu.toCharArray());
        astParser.setKind(type);
        astParser.setResolveBindings(true);
        return (CompilationUnit) astParser.createAST(null);
    }

    public static CompilationUnit genASTFromFile(String fileName) {
        return (CompilationUnit) genASTFromSource(readFileToString(fileName), ASTParser.K_COMPILATION_UNIT);
    }

    public static CompilationUnit genASTFromFile(File file) {
        return (CompilationUnit) genASTFromSource(readFileToString(file), ASTParser.K_COMPILATION_UNIT);
    }

    public static void main(String[] args) {
        String path = "/Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Chart/Chart_3_buggy/source/org/jfree/data/time/TimeSeries.java";
        // FileIO.normalizeFile(path);
        // System.out.println(FileIO.cleanCommons(FileIO.readFileToString(path)));
        // deleteComments(path);
        CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(path),
                ASTParser.K_COMPILATION_UNIT);
        System.out.println();
    }

}
