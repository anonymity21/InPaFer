package patchfilter.model.Main;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.alibaba.fastjson.JSONObject;

import patchfilter.model.config.Constant;
import patchfilter.model.entity.*;
import patchfilter.model.instrument.visitor.MethodInstrumentVisitor;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import patchfilter.util.LineNumber;

/*
 * 这个类是用来 整理 cache  信息的，
 * 
 * fixedfile information
 * patch_methodname information
 *  patch_method information 即 补丁patch 所修改函数的信息， method_name  method_content method_startline method_endline 
 * 为了 之后插件能直接读取
 */
public class MethodInitialization {
	private Subject subject;

	public MethodInitialization(Subject subject) {
		this.subject = subject;
	}

	public void MainProcess() {
		List<PatchFile> patchList = subject.getPatchList();
		PatchInfo.obainAllMethod(patchList);
		for (PatchFile patchFile : patchList) {
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
		}

	}

	public void MainProcess4TestFile() {
		List<String> failingtests = subject.getFailedTestList();
		// PatchInfo.obainAllMethod(patchList);
		for (String failtest : failingtests) {
			String failingPath =  subject.getHome() + "/" + subject.getTsrc() + "/" + failtest.split("::")[0].replace(".", "/") + ".java";
			LineNumber linenumber = new LineNumber(subject, failingPath);
			linenumber.parseFile();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String project = "Math";
		int Start = 41;
		int end = 41;
		for (int i = Start; i <= end; i++) {
			Subject subject = new Subject(project, i);

			if (subject.initPatchListByPath(Constant.AllPatchPath)) {
				// List<PatchFile> patchList = subject.getPatchList();
				System.out.println("Process : " + subject.toString());
				MethodInitialization methodInitialization = new MethodInitialization(subject);
				methodInitialization.MainProcess();
				methodInitialization.MainProcess4TestFile();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
