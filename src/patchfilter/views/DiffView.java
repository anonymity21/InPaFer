package patchfilter.views;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import com.alibaba.fastjson.JSONObject;

import patchfilter.model.config.Constant;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.Pair;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import patchfilter.model.entity.Subject;

public class DiffView extends ViewPart {

	private Composite parentComposite;
	private FormToolkit toolkit;
	private ScrolledForm form;
	private final String Cover = "Cover";
	private final String NotCover = "NotCover";
	private final String CommonCover = "CommonCover";

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		parentComposite = parent;
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Diff Traces");
		TableWrapLayout tableLayout = new TableWrapLayout();
		tableLayout.numColumns = 5;
		form.getBody().setLayout(tableLayout);

		parent.redraw();
		// test for convenience
	/*	Subject subject = new Subject("Chart", 3);
		subject.initPatchListByPath(Constant.AllPatchPath);
		createCodeSections(subject.getPatchList());*/
	}
	
	public void initScrolledForm() {
		toolkit = new FormToolkit(parentComposite.getDisplay());
		form = toolkit.createScrolledForm(parentComposite);
		form.setText("Diff Traces");
		TableWrapLayout tableLayout = new TableWrapLayout();
		tableLayout.numColumns = 5;
		form.getBody().setLayout(tableLayout);
		
	}

	public void createCodeSectionsInTrace(List<PatchFile> patchFiles, String traceLine) {
		// TODO Auto-generated method stub
		PatchFile patchFile = patchFiles.get(0);
		Subject subject = patchFile.getSubject();
		String tracedFile = subject.getHome() + "/" + subject.getSsrc() + "/" + traceLine.split("#")[0].replaceAll("\\.", "/").split("\\$")[0] + ".java";

        String MethodRangeFile = tracedFile.split("/")[tracedFile.split("/").length-1];
        String tmpMethodRecord = Constant.CACHE + patchFile.getSubject().getName()
                + "/" + patchFile.getSubject().getId() + "/" + MethodRangeFile;
        Map<String, Pair<Integer, Integer>> methodRangeMap = PatchInfo.str2Map(FileIO.readFileToString(tmpMethodRecord));
        if (MapUtils.isEmpty(methodRangeMap)) {
			return;
		}
        int traceLineNum = Integer.parseInt(traceLine.split("#")[traceLine.split("#").length - 1]);
        
        Map.Entry<String, Pair<Integer, Integer>> methodEntry = methodRangeMap.entrySet().stream()
        		.filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
        		.filter(entry -> traceLineNum >= entry.getValue().getKey() && traceLineNum <= entry.getValue().getValue()).findAny().orElse(null);
        
        if (Objects.isNull(methodEntry)) {
			return;
		}
        String methodName = methodEntry.getKey();
        
		String traceLineFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject());
		String initTraceLineFile = BuildFilePath.tmpLine("initial", patchFile.getSubject());
		Method method = new Method(methodName, tracedFile);
		method.setStarLineInteger(methodEntry.getValue().getKey());
		method.setEndLineInteger(methodEntry.getValue().getValue());
		createCodeSection(method, patchFile, traceLineFile, initTraceLineFile);
	}
	
	public void createCodeSections(List<PatchFile> patchFiles) {
		PatchInfo.obainAllMethod(patchFiles);
		// 假设一组patches 的 trace是相同的
		Map<String, List<PatchFile>> patchMap = patchFiles.stream().filter(Objects::nonNull)
				.collect(Collectors.groupingBy(PatchFile::getModifiedMethod, LinkedHashMap::new,
						Collectors.toCollection(LinkedList::new)));
		// for(PatchFile patchFile: patchFiles) {
		if (patchMap.size() > 1) {
			System.err.println("different methods, same trace");
		}
		PatchFile patchFile = patchFiles.get(0);
		// for (PatchFile patchFile : patchFiles) {
		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
		String traceLineFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject());
		String initTraceLineFile = BuildFilePath.tmpLine("initial", patchFile.getSubject());
		Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
		createCodeSection(method, patchFile, traceLineFile, initTraceLineFile);   // 只展示修改函数的执行trace
		// }
	}

	private String getColorTrace(List<String> traceContentList, String methodStartLine, long methodCnt) {
		if (CollectionUtils.isEmpty(traceContentList) || StringUtils.isBlank(methodStartLine)) {
			return "";
		}
		long currentMethodCnt = 0;
		StringBuilder resultContentString = new StringBuilder();
		for (String traceLine : traceContentList) {
			if (StringUtils.isBlank(traceLine.trim())) {
				continue;
			}
			if (traceLine.startsWith(methodStartLine)) {
				++currentMethodCnt;
			}
			if (currentMethodCnt == methodCnt) {
				resultContentString.append(traceLine).append("\n");
			}
		}
		return resultContentString.toString();
	}

	

	// private void createCodeSection
	private void createCodeSection(Method method, PatchFile patchFile, String traceFile, String initFile) {

		String methodStartLine = method.getMethodNameString() + " START#0";

		List<String> initFileTraceList = Arrays.asList(FileIO.readFileToString(initFile).split("\n"));
		List<String> patchFileTraceList = Arrays.asList(FileIO.readFileToString(traceFile).split("\n"));

		Long methodCnt = initFileTraceList.stream().map(String::trim).filter(StringUtils::isNoneBlank)
				.filter(traceLine -> traceLine.startsWith(methodStartLine)).count();

		String initColorContentString = getColorTrace(initFileTraceList, methodStartLine, methodCnt);

		String patchColorContentString = getColorTrace(patchFileTraceList, methodStartLine, methodCnt);
		
		form.dispose();
		initScrolledForm();
		parentComposite.pack();
		
		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(true);
		section.setLayout(new TableWrapLayout());
		section.setText("Initial Trace");

		FormText text = toolkit.createFormText(section, true);

		FormColors colors = toolkit.getColors();
		colors.createColor("Cover", colors.getSystemColor(SWT.COLOR_RED)); //new RGB(147, 196, 125));
		text.setColor("Cover", colors.getColor("Cover"));

		colors.createColor("NotCover", colors.getSystemColor(SWT.COLOR_BLACK));
		text.setColor("NotCover", colors.getColor("NotCover"));
		
		colors.createColor("CommonCover", colors.getSystemColor(SWT.COLOR_DARK_GREEN));
		text.setColor("CommonCover", colors.getColor("CommonCover"));
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		
		List<String> initContentList = codeFormatting(method, initColorContentString);
		List<String> patchContentList = codeFormatting(method, patchColorContentString);
		int index = initContentList.size() - 1;
		while (index >= 0) {
			String initLineString = initContentList.get(index);
			String patchLineString = patchContentList.get(index);
			if (initLineString.contains("Cover") && patchLineString.contains("Cover") 
					&& (!initLineString.contains("NotCover") && (!patchLineString.contains("NotCover")))) {
				initContentList.set(index, "<span color=\"" + CommonCover + "\">" + initLineString.split("<span color=\"" + Cover + "\">")[1]);
				patchContentList.set(index, "<span color=\"" + CommonCover + "\">" + initLineString.split("<span color=\"" + Cover + "\">")[1]);
			}
			--index;
		}
		try {
			text.setText(initContentList.stream().collect(Collectors.joining()), true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		section.setClient(text);

		String textName = patchFile.getAliaName();
		
		section = toolkit.createSection(form.getBody(), Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(true);
		section.setLayout(new TableWrapLayout());
		section.setText(textName);

		text = toolkit.createFormText(section, true);

		colors = toolkit.getColors();
		colors.createColor("Cover", colors.getSystemColor(SWT.COLOR_RED));
		text.setColor("Cover", colors.getColor("Cover"));

		colors.createColor("NotCover", colors.getSystemColor(SWT.COLOR_BLACK));
		text.setColor("NotCover", colors.getColor("NotCover"));
		
		colors.createColor("CommonCover", colors.getSystemColor(SWT.COLOR_DARK_GREEN));
		text.setColor("CommonCover", colors.getColor("CommonCover"));
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		
		try {
			// text.setText(codeFormatting(method, initColorContentString), true, false);
			text.setText(patchContentList.stream().collect(Collectors.joining()), true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		section.setClient(text);
	}

	private List<String> codeFormatting(Method method, String traceFileCnt) {
		// TODO Auto-generated method stub
		method.updateMethodContent();
		//System.out.println(method.getContentString());
		String codeString[] = StringUtils.splitPreserveAllTokens(method.getContentString(), "\n");
		//String codeString[] = method.getContentString().split("\n");
		List<String> result = new LinkedList<String>();
		int start = method.getStarLineInteger();
		int end = method.getEndLineInteger();

		result.add("<form><p>");
		for (int i = start; i <= end; i++) {
			String line = codeString[i - start];
			if(line.contains("&")) {
				line  = line.replaceAll("&", "&amp;");
			}
			if(line.contains("<")) {
				line = line.replaceAll("<", "&lt;");
			}
			if(line.contains(">")) {
				line  = line.replaceAll(">", "&gt;");
			}
			//System.out.println(line);
			boolean covered = false;
			for (String traceLine : traceFileCnt.split("\n")) {
				if (traceLine.startsWith(method.getMethodNameString())) {
					int number = Integer.parseInt(traceLine.split("#")[traceLine.split("#").length - 1]);
					if (number == 0) {
						continue;
					} else if (number < i) {
						continue;
					} else if (number == i) {
						line = "<span color=\"" + Cover + "\">" + line + "</span><br/>";
						covered = true;
						break;
					} else {
						line = "<span color=\"" + NotCover + "\">" + line + "</span><br/>";
						covered = true;
						break;
					}
				} else {
					continue;
				}
			}
			if (!covered) {
				line = "<span color=\"" + NotCover + "\">" + line + "</span><br/>";
				covered = true;
			}
			result.add(line);
		}
		result.add("</p></form>");
		// codeString = "<form><p><span color=\"NotCover\">" + codeString +
		// "</span></p></form>";
		//System.out.println(result.toString());
		return result;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}


}
