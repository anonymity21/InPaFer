package patchfilter.views;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.alibaba.fastjson.JSONObject;

import patchfilter.controller.LineCollection;
import patchfilter.controller.VariableCollection;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.PatchFile;
import patchfilter.util.LineNumber;
import patchfilter.model.entity.Subject;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Method;
import patchfilter.util.LocationLine;
import patchfilter.util.TechNameMap;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.util.TestLine;
import patchfilter.util.VariableLine;
//import patchfilter.util.TraceLine;
import sun.util.logging.resources.logging;

//import clonepedia.views.codesnippet.SnippetInstanceRelation;

public class TraceView extends ViewPart {
	// protected Trace trace;
	// protected TreeViewer listViewer;
	private TableViewer traceTableViewer;
	private TableViewer testTableViewer;
	private TableViewer locationTableViewer;
	private TableViewer variableTableViewer;
	// private TableViewer patchesTableViewer;
	private FormToolkit toolkit;
	private ScrolledForm form;

	private LineCollection lineCollection;
	private VariableCollection variableCollection;
	private Subject subject;
	private static final int MAX_PATCH_NUM = 5;
	private static Random random = new Random();

	/**
	 * value: location，trace
	 */
	private static String selectType = "location";
	private static String selectTraceLine = "";

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		GridLayout parentLayout = new GridLayout(1, true);
		parent.setLayout(parentLayout);
		System.out.println("Plugin Start!");
		if (createSubject()) {
			// createSubmitGroup(parent);
			createResetButton(parent);
			createTestBody(parent);
			// createPatchesBody(parent);
			createLocationBody(parent);
			createTraceBody(parent);
			createVariableBody(parent);
			createPatchBody(parent);
		}
	}

	private void createResetButton(Composite parent) {
		// TODO Auto-generated method stub
		Group feedbackGroup = new Group(parent, SWT.NONE);
		// feedbackGroup.setText("Do you want to reset ?");
		feedbackGroup.setLayoutData(new GridData(SWT.LEFT, SWT.UP, true, false));
		GridLayout gl = new GridLayout(3, true);
		gl.makeColumnsEqualWidth = false;
		gl.marginWidth = 1;

		feedbackGroup.setLayout(gl);
		Button againButton = new Button(feedbackGroup, SWT.NONE);
		againButton.setText("Reset Answer!");
		againButton.setLayoutData(new GridData(SWT.RIGHT, SWT.UP, true, false));
		againButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				lineCollection.restoreList();
				if (traceTableViewer != null) {
					traceTableViewer.refresh();
					// traceTableViewer
				}
				if (locationTableViewer != null) {
					locationTableViewer.refresh();
				}
				if (testTableViewer != null) {
					testTableViewer.refresh();
				}
				if (variableTableViewer != null) {
					variableTableViewer.refresh();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		// againButton.addMouseListener(new FeedbackSubmitListener());
	}

	private void createVariableBody(Composite parent) {

		SashForm variableForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 100;
		gridData.heightHint = 30;
		variableForm.setLayoutData(gridData);
		this.variableTableViewer = createVariableTableViewer(variableForm, "Variable Information:");
		// this.createVariableContent(variableCollection.getVariableList());

		// this.createLocationContent(lineCollection.getLocationLineList());
		variableForm.setWeights(new int[] { 5 });

		variableTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					// String line = ((VariableLine) selection.getFirstElement()).getModifyLine();
					List<PatchFile> formatedPatchFiles = ((VariableLine) selection.getFirstElement()).getPatchFiles();
					// openFile(obtainMethodLine(formatedPatchFiles));
					updateCandidatePatchViews(formatedPatchFiles);
				}
			}
		});
	}

	private TableViewer createVariableTableViewer(SashForm variableForm, String groupName) {
		// TODO Auto-generated method stub
		Group varGroup = new Group(variableForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());

		TableViewer viewer = new TableViewer(varGroup);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		String[] columnNames = new String[] { "VarName", "VarValue", "StateType", "PatchNum", "Yes", "No" };
		int[] columnWidths = new int[] { 200, 100, 100, 100, 50, 50 };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };

		// first column for varName
		TableColumn column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[0]);
		column.setWidth(columnWidths[0]);
		TableViewerColumn varNameColumn = new TableViewerColumn(viewer, column);
		varNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableLine) {
					VariableLine variableLine = (VariableLine) element;
					String varName = variableLine.getVarName();
					return varName;
				}
				return null;
			}
		});

		// second column for varValue
		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[1]);
		column.setWidth(columnWidths[1]);
		TableViewerColumn varValueColumn = new TableViewerColumn(viewer, column);
		varValueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableLine) {
					VariableLine variableLine = (VariableLine) element;
					String varValue = variableLine.getValue();
					return varValue;
				}
				return null;
			}
		});

		// third column for statetype
		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[2]);
		column.setWidth(columnWidths[2]);
		TableViewerColumn stateTypeColumn = new TableViewerColumn(viewer, column);
		stateTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableLine) {
					VariableLine variableLine = (VariableLine) element;
					StateType stateType = variableLine.getStateType();
					return stateType.toString();
				}
				return null;
			}
		});

		// forth column for patchNum
		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[3]);
		column.setWidth(columnWidths[3]);
		TableViewerColumn patchNumColumn = new TableViewerColumn(viewer, column);
		patchNumColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof VariableLine) {
					VariableLine variableLine = (VariableLine) element;
					int patchNum = variableLine.getPatchFiles().size();
					return patchNum + "";
				}
				return null;
			}
		});

		// fifth column for yes button
		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[4]);
		column.setWidth(columnWidths[4]);
		TableViewerColumn yesButtonCol = new TableViewerColumn(viewer, column);
		yesButtonCol.setLabelProvider(new ColumnLabelProvider() {
			// make sure you dispose these buttons when viewer input changes
			Map<Object, Button> buttons = new HashMap<Object, Button>();

			@Override
			public void update(ViewerCell cell) {
				buttons.clear();
				TableItem item = (TableItem) cell.getItem();

				Button button;
				/*
				 * StringBuffer key = new StringBuffer(); if(cell.getElement() instanceof
				 * VariableLine) { VariableLine variableLine = (VariableLine)cell.getElement();
				 * key.append(variableLine.getVarName()).append(" ").append(variableLine.
				 * getValue()).append(" "); }
				 */
				if (buttons.containsKey(cell.getElement())) {
					button = buttons.get(cell.getElement());
				} else {
					button = new Button((Composite) cell.getViewerRow().getControl(), SWT.None);
					button.setText("yes");
					button.pack();
					if (cell.getElement() instanceof VariableLine) {
						VariableLine variableLine = (VariableLine) cell.getElement();
						StateType stateType = variableLine.getStateType();
						if (stateType.equals(stateType.YES)) {
							button.setSelection(true);
						}
					}
					button.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
							getTableViewerSelected(viewer, "yes");

						}

						@Override
						public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub

						}
					});
					buttons.put(cell.getElement(), button);
					TableEditor editor = new TableEditor(item.getParent());
					editor.horizontalAlignment = SWT.LEFT;
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();
				}
			}

		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[5]);
		column.setWidth(columnWidths[5]);
		TableViewerColumn noButtonCol = new TableViewerColumn(viewer, column);
		noButtonCol.setLabelProvider(new ColumnLabelProvider() {
			// make sure you dispose these buttons when viewer input changes
			Map<Object, Button> buttons = new HashMap<Object, Button>();

			@Override
			public void update(ViewerCell cell) {
				buttons.clear();
				TableItem item = (TableItem) cell.getItem();
				Button button;
				/*
				 * StringBuffer key = new StringBuffer(); if(cell.getElement() instanceof
				 * VariableLine) { VariableLine variableLine = (VariableLine)cell.getElement();
				 * key.append(variableLine.getVarName()).append(" ").append(variableLine.
				 * getValue()).append(" "); }
				 */
				if (buttons.containsKey(cell.getElement())) {
					button = buttons.get(cell.getElement());
				} else {
					button = new Button((Composite) cell.getViewerRow().getControl(), SWT.None);
					button.setText("no");
					button.pack();
					if (cell.getElement() instanceof VariableLine) {
						VariableLine variableLine = (VariableLine) cell.getElement();
						StateType stateType = variableLine.getStateType();
						if (stateType.equals(stateType.NO)) {
							button.setSelection(true);
						}
					}
					button.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
							getTableViewerSelected(viewer, "no");
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
						}
					});
					buttons.put(cell.getElement(), button);
					TableEditor editor = new TableEditor(item.getParent());
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();
				}
			}
		});

		viewer.setInput(lineCollection.getVarLineList());
		return viewer;
	}

	/*
	 * private TableViewer createVariableVarGroup(SashForm variableForm, String
	 * groupName) { // TODO Auto-generated method stub Group varGroup = new
	 * Group(variableForm, SWT.NONE); varGroup.setText(groupName);
	 * varGroup.setLayout(new FillLayout());
	 * 
	 * Table table = new Table(varGroup, SWT.H_SCROLL | SWT.V_SCROLL |
	 * SWT.FULL_SELECTION);
	 * 
	 * table.setHeaderVisible(true); table.setLinesVisible(true);
	 * 
	 * String[] columnNames = new String[] { "Variable", "Patches" }; int[]
	 * columnWidths = new int[] { 300, 100 }; int[] columnAlignments = new int[] {
	 * SWT.LEFT, SWT.LEFT }; for (int i = 0; i < columnNames.length; i++) {
	 * TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
	 * tableColumn.setText(columnNames[i]); tableColumn.setWidth(columnWidths[i]); }
	 * return new TableViewer(table);
	 * 
	 * }
	 */

	private void createLocationBody(Composite parent) {
		// TODO Auto-generated method stub
		SashForm locationForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 100;
		gridData.heightHint = 30;
		locationForm.setLayoutData(gridData);
		this.locationTableViewer = createLocationTableViewer(locationForm, "Modification Location Information: ");

		// this.createLocationContent(lineCollection.getLocationLineList());
		// this.setCheckButtonColumn(locationTableViewer);
		locationForm.setWeights(new int[] { 5 });

		locationTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					String line = ((LocationLine) selection.getFirstElement()).getModifyMethod();
					List<PatchFile> formatedPatchFiles = ((LocationLine) selection.getFirstElement()).getPatchList();
					openFile(obtainMethodLine(formatedPatchFiles));
					updateCandidatePatchViews(formatedPatchFiles);
					showDiffWithOriginal(formatedPatchFiles);
					selectType = "location";
				}
			}
		});

	}

	private TableViewer createLocationTableViewer(SashForm locationForm, String groupName) {
		Group varGroup = new Group(locationForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());

		TableViewer viewer = new TableViewer(varGroup);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		String[] columnNames = new String[] { "Bug Location", "StateType", "PatchNum", "Yes", "No" };
		int[] columnWidths = new int[] { 200, 100, 100, 50, 50 };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };

		// first column for buglocation
		TableColumn column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[0]);
		column.setWidth(columnWidths[0]);
		TableViewerColumn modLineColumn = new TableViewerColumn(viewer, column);
		modLineColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LocationLine) {
					LocationLine locationLine = (LocationLine) element;
					String location = locationLine.getModifyMethod();
					String fileName = location.split("#")[0];
					String methodName = location.split("#")[2];

					return fileName + "#" + methodName;
				}
				return null;
			}
		});

		// second column for stateType
		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[1]);
		column.setWidth(columnWidths[1]);
		TableViewerColumn stateTypeColumn = new TableViewerColumn(viewer, column);
		stateTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LocationLine) {
					LocationLine locationLine = (LocationLine) element;
					StateType stateType = locationLine.getStateType();
					return stateType.toString();
				}
				return null;
			}
		});

		// third column for patchNum
		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[2]);
		column.setWidth(columnWidths[2]);
		TableViewerColumn patchNumColumn = new TableViewerColumn(viewer, column);
		patchNumColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LocationLine) {
					LocationLine locationLine = (LocationLine) element;
					int patchNum = locationLine.getPatchList().size();
					return patchNum + "";
				}
				return null;
			}
		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[3]);
		column.setWidth(columnWidths[3]);
		TableViewerColumn yesButtonCol = new TableViewerColumn(viewer, column);
		yesButtonCol.setLabelProvider(new ColumnLabelProvider() {
			// make sure you dispose these buttons when viewer input changes

			Map<Object, Button> buttons = new HashMap<Object, Button>();

			@Override
			public void update(ViewerCell cell) {
				buttons.clear();
				TableItem item = (TableItem) cell.getItem();

				Button button;
				/*
				 * StringBuffer key = new StringBuffer(); if(cell.getElement() instanceof
				 * LocationLine) { LocationLine locationLine = (LocationLine) cell.getElement();
				 * key.append(locationLine.getModifyLine()); }
				 */
				if (buttons.containsKey(cell.getElement())) {
					button = buttons.get(cell.getElement());
				} else {
					button = new Button((Composite) cell.getViewerRow().getControl(), SWT.None);
					button.setText("yes");
					button.pack();
					if (cell.getElement() instanceof LocationLine) {
						LocationLine locationLine = (LocationLine) cell.getElement();
						StateType stateType = locationLine.getStateType();
						if (stateType.equals(stateType.YES)) {
							button.setSelection(true);
						}
					}
					button.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
							getTableViewerSelected(viewer, "yes");

						}

						@Override
						public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub

						}
					});
					buttons.put(cell.getElement(), button);
					TableEditor editor = new TableEditor(item.getParent());
					editor.horizontalAlignment = SWT.LEFT;
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();
				}
			}
		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[4]);
		column.setWidth(columnWidths[4]);
		TableViewerColumn noButtonCol = new TableViewerColumn(viewer, column);
		noButtonCol.setLabelProvider(new ColumnLabelProvider() {
			// make sure you dispose these buttons when viewer input changes
			Map<Object, Button> buttons = new HashMap<Object, Button>();

			@Override
			public void update(ViewerCell cell) {
				buttons.clear();
				TableItem item = (TableItem) cell.getItem();
				Button button;
				/*
				 * StringBuffer key = new StringBuffer(); if(cell.getElement() instanceof
				 * LocationLine) { LocationLine locationLine = (LocationLine) cell.getElement();
				 * key.append(locationLine.getModifyLine()); }
				 */
				if (buttons.containsKey(cell.getElement())) {
					button = buttons.get(cell.getElement());
				} else {
					button = new Button((Composite) cell.getViewerRow().getControl(), SWT.None);
					button.setText("no");
					button.pack();
					if (cell.getElement() instanceof LocationLine) {
						LocationLine locationLine = (LocationLine) cell.getElement();
						StateType stateType = locationLine.getStateType();
						if (stateType.equals(stateType.NO)) {
							button.setSelection(true);
						}
					}
					button.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
							getTableViewerSelected(viewer, "no");
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
						}
					});
					buttons.put(cell.getElement(), button);
					TableEditor editor = new TableEditor(item.getParent());
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();
				}
			}
		});

		viewer.setInput(lineCollection.getLocationLineList());
		return viewer;
	}

	private void createPatchBody(Composite parent) {
		// TODO Auto-generated method stub

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Candidate Patches");
		form.setAlwaysShowScrollBars(true);
		TableWrapLayout tableLayout = new TableWrapLayout();
		tableLayout.numColumns = 1;

		for (Control control : form.getBody().getChildren()) {
			if (control instanceof Section) {
				control.dispose();
			}
			if (control instanceof Button) {
				control.dispose();
			}
		}

		TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB);
		tableWrapData.heightHint = 100;
		form.getBody().setLayout(tableLayout);
		// form.getBody().setLayoutData(tableWrapData);
		parent.redraw();

		List<PatchFile> patchLines = lineCollection.getCurrentPatches();
		HashSet<String> patchKindSet = new HashSet<String>();
		for (PatchFile patchFile : patchLines) {
			String patchKind = patchFile.getPatchName().split("-")[0];
			if (!patchKindSet.contains(patchKind)) {
				patchKindSet.add(patchKind);
				createCodeSection(form.getBody(), patchFile);
			}
		}
	}

	private void createCodeSection(Composite body, PatchFile formatedPatchFile) {
		// TODO Auto-generated method stubget
		// body.redraw();
		// String techName = formatedPatchFile.getPatchName().split("-")[0];
		// String postfix = formatedPatchFile.getPatchName().split("-")[1];
		// String textName = TechNameMap.getMap().get(techName) + "-" + postfix;

		Button deleteButton = toolkit.createButton(body, "Delete " + formatedPatchFile.getAliaName(), SWT.BUTTON1);
		deleteButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				lineCollection.deleteCandidatePatches(formatedPatchFile);
				lineCollection.updateTestLineList();
				if (traceTableViewer != null) {
					traceTableViewer.refresh();
					// traceTableViewer
				}
				if (locationTableViewer != null) {
					locationTableViewer.refresh();
				}
				if (testTableViewer != null) {
					testTableViewer.refresh();
				}
				if (variableTableViewer != null) {
					variableTableViewer.refresh();
				}
				updateCandidatePatchViews(lineCollection.getCurrentPatches());

			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

		});

		Section section = toolkit.createSection(body, Section.TWISTIE | Section.EXPANDED | Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setExpanded(false);
		section.setLayout(new TableWrapLayout());
		section.setText(formatedPatchFile.getAliaName());

		section.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				String fileName = formatedPatchFile.getFixedFile();
				// int startLine = formatedPatchFile.getStarLine();
				int fixedLine = 0;
				int lineNumber = formatedPatchFile.getStarLine();
				for (String line : formatedPatchFile.getPatch().split("\n")) {
					if (line.startsWith("-")) {
						fixedLine = lineNumber;
						break;
					}
					if (line.startsWith("+")) {
						fixedLine = lineNumber;
						break;
					}
					lineNumber++;
				}

				System.out.println("Section mouseDown" + fileName + "#" + fixedLine);
				openFile(fileName + "#" + fixedLine);
				if (selectType.equalsIgnoreCase("location")) {
					showDiffWithOriginal(Collections.singletonList(formatedPatchFile));
				}
				if (selectType.equalsIgnoreCase("trace") && StringUtils.isNotBlank(selectTraceLine)) {
					showDiffWithOriginalInTrace(Collections.singletonList(formatedPatchFile), selectTraceLine);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		FormText text = toolkit.createFormText(section, true);

		FormColors colors = toolkit.getColors();
		colors.createColor("CounterRelation", colors.getSystemColor(SWT.COLOR_RED));
		text.setColor("CounterRelation", colors.getColor("CounterRelation"));
		text.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				// TODO Auto-generated method stub
				String fileName = formatedPatchFile.getFixedFile();
				int fixedLine = 0;
				int lineNumber = formatedPatchFile.getStarLine();
				for (String line : formatedPatchFile.getPatch().split("\n")) {
					if (line.startsWith("-")) {
						fixedLine = lineNumber;
						break;
					}
					if (line.startsWith("+")) {
						fixedLine = lineNumber;
						break;
					}
					lineNumber++;
				}
				System.out.println("Section mouseDown" + fileName + "#" + fixedLine);
				openFile(fileName + "#" + fixedLine);
				if (selectType.equalsIgnoreCase("location")) {
					showDiffWithOriginal(Collections.singletonList(formatedPatchFile));
				}
				if (selectType.equalsIgnoreCase("trace") && StringUtils.isNotBlank(selectTraceLine)) {
					showDiffWithOriginalInTrace(Collections.singletonList(formatedPatchFile), selectTraceLine);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		colors.createColor("Distant", colors.getSystemColor(SWT.COLOR_DARK_BLUE));
		text.setColor("Distant", colors.getColor("Distant"));
		// text.setBackground(colors.createColor(key, rgb));
		// setFormTextColorAndFont(text);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setWhitespaceNormalized(false);
		String patch = formatedPatchFile.getPatch();
		int lineNumber = formatedPatchFile.getStarLine();
		StringBuilder linePatch = new StringBuilder();
		// int lineNumber = originalLine;
		for (String line : patch.split("\n")) {
			if (line.startsWith("+")) {
				linePatch.append("   ").append(line).append("\n");
				continue;
			}
			linePatch.append(lineNumber).append(line).append("\n");
			lineNumber++;
		}
		try {
			text.setText(linePatch.toString(), false, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		section.setClient(text);
	}

	/**
	 * 获取当前项目 project id 并且初始化 traceLineCollection
	 * 
	 * @return
	 */
	private boolean createSubject() {
		// TODO Auto-generated method stub
		IProject currentProject = getCurrentSelectedProject();
		if (currentProject != null) {
			String projectName = currentProject.getName();
			System.out.println("current project: " + projectName);
			this.subject = new Subject(projectName.split("_")[0], Integer.parseInt(projectName.split("_")[1]));
			subject.initPatchListByPath(Constant.AllPatchPath);
			lineCollection = new LineCollection(subject);
			lineCollection.initSet();
			return true;
		}
		/*
		 * else { System.out.println("current project: Chart3"); this.subject = new
		 * Subject("Chart", 3); subject.initPatchListByPath(Constant.AllPatchPath);
		 * lineCollection = new LineCollection(subject); lineCollection.initSet();
		 * return true; }
		 */
		return false;
	}

// 得到当前选中的project
	public IProject getCurrentSelectedProject() {
		IProject project = null;
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();

			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof PackageFragmentRoot) {
				IJavaProject jProject = ((PackageFragmentRoot) element).getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element).getJavaProject();
				project = jProject.getProject();
			}
		}
		return project;
	}

	private void createTestBody(Composite parent) {
		// TODO Auto-generated method stub
		SashForm testForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 100;
		gridData.heightHint = 30;

		testForm.setLayoutData(gridData);
		this.testTableViewer = createTestVarGroup(testForm, "TestCase Information: ");

		this.createTestContent(lineCollection.getFailTestList());
		testForm.setWeights(new int[] { 5 });
		testTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					String failingTest = ((TestLine) selection.getFirstElement()).getTestCaseString();
					openFile(testPatchTransform(failingTest));
					updateCandidatePatchViews(lineCollection.getCurrentPatches());
					System.out.println("Now selecting " + selection.getFirstElement());
					System.out.println("Now TraceLine List " + lineCollection.getTraceLinelist());
					// showMessage("Now selecting "+selection.getFirstElement().toString());
				}
			}
		});

	}

	private TableViewer createTestVarGroup(SashForm testForm, String groupName) {
		// TODO Auto-generated method stub
		Group varGroup = new Group(testForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());

		Table table = new Table(varGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION); // to check what means

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 30;
		table.setLayoutData(data);

		String[] columnNames = new String[] { "TestCase", "Remain Patch" };
		int[] columnWidths = new int[] { 300, 100 };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(columnWidths[i]);
		}
		/*
		 * for (int i = 0; i < 3; i++) { new TableItem(table, SWT.NONE); }
		 */
		return new TableViewer(table);
	}

	private void createTestContent(List<TestLine> testlist) {
		this.testTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		this.testTableViewer.setLabelProvider(new TestLabelProvider());
		this.testTableViewer.setInput(testlist);

		this.testTableViewer.refresh(true);
	}

	private void createTraceBody(Composite parent) {
		// TODO Auto-generated method stub
		SashForm traceForm = new SashForm(parent, SWT.VERTICAL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 100;
		gridData.heightHint = 30;
		traceForm.setLayoutData(gridData);
		this.traceTableViewer = createTraceTViewer(traceForm, "Trace Information: ");
		// createVarGroup(traceForm, "Trace Information: ");
		traceForm.setWeights(new int[] { 4 });

		// TODO get project information

		// this.createTraceContent(lineCollection.getTraceLinelist());
		// this.setCheckButton(traceTableViewer);
		traceTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					String fileName = ((LineInfo) selection.getFirstElement()).getLineName();
					List<PatchFile> patchList = ((LineInfo) selection.getFirstElement()).getPatchList();
					updateCandidatePatchViews(patchList);
					showDiffWithOriginalInTrace(patchList, ((LineInfo) selection.getFirstElement()).getLineName());
					openFile(tracePathTransform(fileName));
					System.out.println("Now selecting " + selection.getFirstElement());
					// System.out.println("Now tableViewer " + traceTableViewer.getInput());
					// showMessage("Now selecting "+selection.getFirstElement().toString());
					selectType = "trace";
					selectTraceLine = fileName;
				}
			}
		});

	}

	private TableViewer createTraceTViewer(SashForm traceForm, String groupName) {
		// TODO Auto-generated method stub
		Group varGroup = new Group(traceForm, SWT.NONE);
		varGroup.setText(groupName);
		varGroup.setLayout(new FillLayout());

		TableViewer viewer = new TableViewer(varGroup);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] { "Trace Line", "Trace State", "PatchNum", "Yes", "No" };
		int[] columnWidths = new int[] { 100, 100, 100, 50, 50 };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };

		TableColumn column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[0]);
		column.setWidth(columnWidths[0]);

		TableViewerColumn modLineColumn = new TableViewerColumn(viewer, column);
		modLineColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LineInfo) {
					LineInfo lineInfo = (LineInfo) element;
					String fileName = lineInfo.getLineName().split("#", 2)[0];
					fileName = fileName.split("\\.")[fileName.split("\\.").length - 1];
					String lineNumber = lineInfo.getLineName().split("#")[lineInfo.getLineName().split("#").length - 1];
					String traceLine = fileName + "#" + lineNumber;
					return traceLine;
				}
				return null;
			}
		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[1]);
		column.setWidth(columnWidths[1]);
		TableViewerColumn stateColumn = new TableViewerColumn(viewer, column);
		stateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LineInfo) {
					LineInfo lineInfo = (LineInfo) element;
					StateType stateType = lineInfo.getStateType();
					return stateType.toString();
				}
				return null;
			}
		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[2]);
		column.setWidth(columnWidths[2]);
		TableViewerColumn patchNumColumn = new TableViewerColumn(viewer, column);
		patchNumColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LineInfo) {
					LineInfo lineInfo = (LineInfo) element;
					int size = lineInfo.getPatchList().size();
					return String.valueOf(size);
				}
				return null;
			}
		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[3]);
		column.setWidth(columnWidths[3]);
		TableViewerColumn yesButtonCol = new TableViewerColumn(viewer, column);
		yesButtonCol.setLabelProvider(new ColumnLabelProvider() {
			// make sure you dispose these buttons when viewer input changes
			Map<Object, Button> buttons = new HashMap<Object, Button>();

			@Override
			public void update(ViewerCell cell) {
				buttons.clear();
				TableItem item = (TableItem) cell.getItem();
				Button button;
				/*
				 * StringBuffer key = new StringBuffer(); if(cell.getElement() instanceof
				 * LineInfo) { LineInfo lineInfo = (LineInfo) cell.getElement();
				 * key.append(lineInfo.getLineName()); }
				 */
				if (buttons.containsKey(cell.getElement())) {
					button = buttons.get(cell.getElement());
				} else {
					button = new Button((Composite) cell.getViewerRow().getControl(), SWT.None);
					button.setText("yes");
					button.pack();
					button.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
							getTableViewerSelected(viewer, "yes");
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
						}
					});
					buttons.put(cell.getElement(), button);
					TableEditor editor = new TableEditor(item.getParent());
					editor.horizontalAlignment = SWT.LEFT;
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();
				}
			}
		});

		column = new TableColumn(viewer.getTable(), columnAlignments[0]);
		column.setText(columnNames[4]);
		column.setWidth(columnWidths[4]);
		TableViewerColumn noButtonCol = new TableViewerColumn(viewer, column);
		noButtonCol.setLabelProvider(new ColumnLabelProvider() {
			// make sure you dispose these buttons when viewer input changes
			Map<Object, Button> buttons = new HashMap<Object, Button>();

			@Override
			public void update(ViewerCell cell) {
				buttons.clear();
				TableItem item = (TableItem) cell.getItem();
				Button button;
				/*
				 * StringBuffer key = new StringBuffer(); if(cell.getElement() instanceof
				 * LineInfo) { LineInfo lineInfo = (LineInfo) cell.getElement();
				 * key.append(lineInfo.getLineName()); }
				 */
				if (buttons.containsKey(cell.getElement())) {
					button = buttons.get(cell.getElement());
				} else {
					button = new Button((Composite) cell.getViewerRow().getControl(), SWT.None);
					button.setText("no");
					button.pack();
					button.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
							getTableViewerSelected(viewer, "no");
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
							// TODO Auto-generated method stub
						}
					});
					buttons.put(cell.getElement(), button);
					TableEditor editor = new TableEditor(item.getParent());
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(button, item, cell.getColumnIndex());
					editor.layout();
				}
			}
		});
		viewer.setInput(lineCollection.getTraceLinelist());
		return viewer;
	}

	private void updateCandidatePatchViews(List<PatchFile> formatedPatchFiles) {
		System.out.println("Now FormatedPatchFile: " + formatedPatchFiles.size());
		for (Control control : form.getBody().getChildren()) {
			if (control instanceof Section) {
				control.dispose();
			}
			if (control instanceof Button) {
				control.dispose();
			}
		}
		HashSet<String> patchSet = new HashSet<String>();
		if (formatedPatchFiles.size() <= MAX_PATCH_NUM) {
			formatedPatchFiles.forEach(formatedPatchFile -> createCodeSection(form.getBody(), formatedPatchFile));
		} else {
			Map<String, String> patchTraceMap = getPatchTraceMap(formatedPatchFiles);
			Map<String, List<String>> traceKindMap = patchTraceMap.entrySet().stream()
					.filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
					.collect(Collectors.groupingBy(Map.Entry<String, String>::getValue,
							Collectors.mapping(Map.Entry<String, String>::getKey, Collectors.toList())));
			Map<String, PatchFile> formatedPatchFileMap = formatedPatchFiles.stream()
					.collect(Collectors.toMap(PatchFile::getPatchName, Function.identity(), (v1, v2) -> v2));

			traceKindMap.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
					.forEach(entry -> {
						List<PatchFile> currentPatchFileList = entry.getValue().stream().filter(StringUtils::isNotBlank)
								.map(formatedPatchFileMap::get).filter(Objects::nonNull).collect(Collectors.toList());
						PatchFile formatedPatchFile = currentPatchFileList.get(0);
						patchSet.add(formatedPatchFile.getPatchName());
						createCodeSection(form.getBody(), formatedPatchFile);
					});
			if (patchSet.size() < MAX_PATCH_NUM) {
				formatedPatchFiles.stream()
						.filter(formatedPatchFile -> !patchSet.contains(formatedPatchFile.getPatchName()))
						.limit(MAX_PATCH_NUM - patchSet.size())
						.forEach(formatedPatchFile -> createCodeSection(form.getBody(), formatedPatchFile));
			}
		}
		form.getBody().redraw();
		form.getBody().update();
	}

	private Map<String, String> getPatchTraceMap(List<PatchFile> patchFileList) {
		if (CollectionUtils.isEmpty(patchFileList)) {
			return Collections.emptyMap();
		}
		Map<String, String> patchTraceMap = new HashMap<String, String>();
		for (PatchFile patchFile : patchFileList) {
			if (Objects.isNull(patchFile)) {
				continue;
			}
			String traceFile = BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), patchFile.getSubject());
			if (StringUtils.isEmpty(traceFile)) {
				continue;
			}
			List<String> patchFileTraceList = Arrays.asList(FileIO.readFileToString(traceFile).split("\n"));
			patchTraceMap.put(patchFile.getPatchName(),
					patchFileTraceList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining()));
		}
		return patchTraceMap;
	}

	private int randomIndex(int length) {
		if (length == 1) {
			return 0;
		}
		return random.nextInt(length - 1);
	}

	public boolean getTableViewerSelected(TableViewer tableViewer, String answer) {
		Table t = tableViewer.getTable();
		int index = t.getSelectionIndex();

		if (index == -1) {
			showMessage("Must choose a line");
			return false;
		} else if (tableViewer.equals(traceTableViewer)) {
			// TODO: update the traceLineList by buttonId and index
			// tmp
			lineCollection.updateTraceLinelist(index, answer);

		} else if (tableViewer.equals(locationTableViewer)) {
			lineCollection.updateLocationLineList(index, answer);
		} else if (tableViewer.equals(variableTableViewer)) {
			lineCollection.updateVariableLineList(index, answer);
		}

		lineCollection.updateTestLineList();
		// System.out.println();
		if (testTableViewer != null) {
			testTableViewer.refresh();
		}
		if (locationTableViewer != null) {
			locationTableViewer.refresh();
		}
		if (this.traceTableViewer != null) {
			this.traceTableViewer.refresh();
		}
		if (this.variableTableViewer != null) {
			this.variableTableViewer.refresh();
		}

		return true;
	}

	private void openFile(String fileName) {
		// System.out.println(fileName);
		String filePath = fileName.split("#")[0].contains("$") ? fileName.split("#")[0].split("\\$")[0] + ".java"
				: fileName.split("#")[0];
		int line = Integer.valueOf(fileName.split("#")[1]);
		final IFile inputFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation(Path.fromOSString(filePath));
		if (inputFile != null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorPart openEditor;
			try {
				openEditor = IDE.openEditor(page, inputFile);

				if (openEditor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) openEditor;
					IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
					textEditor.selectAndReveal(document.getLineOffset(line - 1), document.getLineLength(line - 1));
					/*
					 * if (textEditor != null) { IResource resource = (IResource)
					 * textEditor.getEditorInput().getAdapter(IResource.class); LineLabel
					 * lineBreakpoint = new LineLabel(resource, line);
					 * DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint)
					 * ; }
					 */
				}
			} catch (BadLocationException | CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void showDiffWithOriginal(List<PatchFile> patchFiles) {
		DiffView viewpart = (DiffView) getSite().getWorkbenchWindow().getActivePage()
				.findView("patchfilter.views.DiffView");
		if (viewpart != null) {
			viewpart.createCodeSections(patchFiles);
		}
	}

	private void showDiffWithOriginalInTrace(List<PatchFile> patchFiles, String traceLine) {
		DiffView viewpart = (DiffView) getSite().getWorkbenchWindow().getActivePage()
				.findView("patchfilter.views.DiffView");
		if (viewpart != null) {
			viewpart.createCodeSectionsInTrace(patchFiles, traceLine);
		}
	}

	private String tracePathTransform(String relativePath) {
		String filePath = relativePath.split("#")[0];
		int line = Integer.valueOf(relativePath.split("#")[relativePath.split("#").length - 1]);
		String absolutePath = subject.getHome() + "/" + subject.getSsrc() + "/" + filePath.replace(".", "/") + ".java";
		return absolutePath + "#" + line;
	}

	private String obtainMethodLine(List<PatchFile> patchFiles) {
		PatchFile patchFile = patchFiles.get(0);
		String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
				+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
		Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
		return method.getFilePathString() + "#" + method.getStarLineInteger();
	}

	private String testPatchTransform(String relativePath) {
		String filePath = relativePath.split("::")[0];
		String testCase = relativePath.split("::")[1];
		// int line = Integer.valueOf(relativePath.split("#")[1]);
		String absolutePath = subject.getHome() + "/" + subject.getTsrc() + "/" + filePath.replace(".", "/") + ".java";
		LineNumber linenumber = new LineNumber(subject, absolutePath);
		linenumber.parseFile();
		int lineNum = linenumber.getLine(testCase);

		return absolutePath + "#" + lineNum;
	}

	public void showMessage(String message) {
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		box.setMessage(message);
		box.open();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
