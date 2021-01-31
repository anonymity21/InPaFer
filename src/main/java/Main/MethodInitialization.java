package Main;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.LineNumber;
import entity.Method;
import entity.PatchFile;
import entity.Subject;
import instrument.visitor.MethodInstrumentVisitor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import util.FileIO;
import util.PatchInfo;

import java.awt.print.PrinterJob;
import java.util.List;

/**
 * 这个类是用来 整理 cache  信息的，
 * <p>
 * fixedfile information
 * patch_methodname information
 */
@Slf4j
public class MethodInitialization {
    private Subject subject;

    public MethodInitialization(Subject subject) {
        this.subject = subject;
    }

    public void MainProcess() {
        List<PatchFile> patchList = subject.getPatchList();
        PatchInfo.obainAllMethod(patchList);
      /*  for (PatchFile patchFile : patchList) {
            String methodName = patchFile.getModifiedMethod();
            String fixedFile = patchFile.getFixedFile();
            Method method = new Method(methodName, fixedFile);
            MethodInstrumentVisitor methodVisitor = new MethodInstrumentVisitor();
            methodVisitor.setMethod(method);
            CompilationUnit compilationUnit = FileIO.genASTFromSource(FileIO.readFileToString(fixedFile),
                    ASTParser.K_COMPILATION_UNIT);
            compilationUnit.accept(methodVisitor);
            method = methodVisitor.getMethod();
            String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
                    + patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
            FileIO.writeStringToFile(patchMethodFile, JSONObject.toJSONString(method), false);
        }*/

    }

    public void MainProcess4TestFile() {
        List<String> failingtests = subject.getFailedTestList();
        // PatchInfo.obainAllMethod(patchList);
        for (String failtest : failingtests) {
            String failingPath = subject.getHome() + "/" + subject.getTsrc() + "/" + failtest.split("::")[0].replace(".", "/") + ".java";
            LineNumber linenumber = new LineNumber(subject, failingPath);
            linenumber.parseFile();
        }

    }

    public static void main(String[] args) {
        String project = "Math";
        int start = 15;
        int end = 15;

        for(int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-p=")) {
               // Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
                project = args[i].substring("-p=".length());
            } else if (args[i].startsWith("-s=")) {
                start = Integer.parseInt(args[i].substring("-s=".length()));
            } else if (args[i].startsWith("-e=")) {
                end = Integer.parseInt(args[i].substring("-e=".length()));
            }
        }
        log.info("Start Running <<" + project +">> MethodInitialization from " + start + " to " + end );
        for (int i = start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                // List<PatchFile> patchList = subject.getPatchList();
                log.info("Process : " + subject.toString());
                MethodInitialization methodInitialization = new MethodInitialization(subject);
                methodInitialization.MainProcess();
                //methodInitialization.MainProcess4TestFile();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
