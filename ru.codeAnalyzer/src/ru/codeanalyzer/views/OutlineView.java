package ru.codeanalyzer.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.editor.Editor;
import ru.codeanalyzer.editor.core.EditorInput;
import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.IEditorFactory;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.ILinkedWithEditorView;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IEvents.searchType;
import ru.codeanalyzer.views.core.HierarchyContentProvider;
import ru.codeanalyzer.views.core.LineInfo;
import ru.codeanalyzer.views.core.LinkWithEditorPartListener;
import ru.codeanalyzer.views.core.OutlineContentProvider;
import ru.codeanalyzer.views.core.TreeFilter;

//DONE поле поиска по тексту - маркеры + фильтр имени процедуры

public class OutlineView extends ViewPart implements ILinkedWithEditorView{
	
	IEvents events = pico.get(IEvents.class);
	
	@Override
	public void dispose() {
		getSite().getPage().removePartListener(linkWithEditorPartListener);
		super.dispose();
	}

	private static class OutlineLabelProvider extends StyledCellLabelProvider {
		
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString text = new StyledString();
			final IColorManager color = pico.get(IColorManager.class);
//			cell.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/markers/codeanalyzer-proc.png"));
			
			if (element instanceof LineInfo) {
				final LineInfo info = (LineInfo) element;
				text.append(info.title + (info.export ? " Экспорт": ""), new Styler() {					
					@Override
					public void applyStyles(TextStyle textStyle) {
						
						RGB rgb = color.getStandartProcedureColor(info.name);
						if(rgb!=null)
							textStyle.foreground = color.getColor(rgb);
						
					}
				});
			}
			
			cell.setText(text.toString());
		    cell.setStyleRanges(text.getStyleRanges());
		    super.update(cell);
		}
	}
	/**
	 * http://www.vogella.com/articles/EclipseJFaceTree/article.html
	 * @author Enikeev M.A.
	 *
	 */	
	private static class HierarchyLabelProvider extends StyledCellLabelProvider{
		private OutlineView view;

		public HierarchyLabelProvider(OutlineView outlineView) {
			this.view = outlineView;
		}

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString text = new StyledString();
			
//			cell.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/markers/codeanalyzer-proc.png"));
			
			if (element instanceof LineInfo) {
				LineInfo info = (LineInfo) element;
				text.append(info.title + (info.export ? " Экспорт": ""));
				text.append(" : " + info.data.object_title.concat("." + info.data.module_title), StyledString.QUALIFIER_STYLER);
				if(info.parent==null)
					text.append(" : " + (view.showCalls?"вызывается":"вызывает"), StyledString.COUNTER_STYLER);
			}
			
			cell.setText(text.toString());
		    cell.setStyleRanges(text.getStyleRanges());
		    super.update(cell);
		}
	}

//	Collection<ReconcilingStrategy> strategies = new ArrayList<ReconcilingStrategy>();
	
	public static final String ID = "ru.codeAnalyzer.views.outline";

	private static final Object[] EMPTY_ARRAY = new Object[0];
	 
	private IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);
	private  Collection<LineInfo> model;

	private TreeViewer outline;

	private Editor editor;

	private BuildInfo data;

	public BuildInfo getData() {
		return data;
	}

	private Action hierarchyType;

	public boolean showGroups = true;
	public boolean showCalls = true;
	public boolean callsInObject = false;
	
	private TreeViewer hierarchy;
	private Text search;

	private TreeFilter filter = new TreeFilter();

	private Combo combo;
	
	public OutlineView() {
	}

	//DONE порядок процедур как в модуле
	//DONE подчиненные элементы: вызывается
	//DONE подчиненные элементы: вызывает
	//FUTURE группировка/выделение процедур по типу
	//DONE история переходов
	//DONE имя окна = имя модуля
	//DONE переход к процедуре по двойном щелчку - если она в другом модуле

	public boolean openWithEditor(Editor editor)
	{
		return this.editor == editor;
	}
	
	public void update(Editor editor, LinkedHashMap<String, LineInfo> model) {
		
		this.editor = editor;
		this.data = ((EditorInput) editor.getEditorInput()).getData();
		
//		if(this.model!=model.values())
//		{
			this.model = model.values();
			outline.setInput(model.values());
			for (LineInfo line : this.model) {
				if(line.name.equalsIgnoreCase(data.name)){
					outline.setSelection(new StructuredSelection(line), true);
					break;
				}
			}
//		}
//		selectCurrentItem();
		
		setPartName(this.editor.getEditorInput().getName());
	}
	
	public void setHierarchyData(ArrayList<LineInfo> data) {
		
		hierarchy.setInput(data);
		
	}
	
	public void setCurrentLine(LineInfo line) {
		
		LineInfo current = null;
		StructuredSelection selection = (StructuredSelection)outline.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			current = (LineInfo)selection.getFirstElement();
		}	
		if(line!=current)
			outline.setSelection(new StructuredSelection(line), true);
		
	}
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		Composite composite = new Composite(sashForm, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		combo = new Combo(composite, SWT.READ_ONLY);
		FormData fd_combo = new FormData();
		fd_combo.top = new FormAttachment(0, 6);
		fd_combo.left = new FormAttachment(0, 5);
		combo.setLayoutData(fd_combo);
		combo.add(events.searchTypeData(searchType.text).caption);
		combo.add(events.searchTypeData(searchType.proc).caption);
		combo.select(0);
		
		search = new Text(composite, SWT.BORDER);
		search.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character=='\r')
					doSearch();
			}
		});
		FormData fd_search = new FormData();
		fd_search.left = new FormAttachment(combo, 6);
		fd_search.top = new FormAttachment(0, 7);
		search.setLayoutData(fd_search);
		
		Button btnNewButton = new Button(composite, SWT.FLAT);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();				
			}
		});
		fd_search.right = new FormAttachment(btnNewButton, -6);
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.top = new FormAttachment(combo, -2, SWT.TOP);
		fd_btnNewButton.right = new FormAttachment(100, -10);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Найти");
		
		outline = new TreeViewer(sashForm, SWT.BORDER);
		Tree tree = outline.getTree();
		Menu outlineMenu = new Menu(tree);
		tree.setMenu(outlineMenu);
		
		MenuItem item2 = new MenuItem(outlineMenu, SWT.NONE);
		item2.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/list.gif"));
		item2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showHierarchy(false, false);				
			}
		});
		item2.setText("Показать вызываемые...");
		
		MenuItem item1 = new MenuItem(outlineMenu, SWT.NONE);
		item1.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/hierarchy.png"));
		item1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showHierarchy(true, false);					
			}
		});
		item1.setText("Показать вызывающие...");
		
		MenuItem item7 = new MenuItem(outlineMenu, SWT.NONE);
		item7.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showHierarchy(true, true);	
			}
		});
		item7.setText("Показать вызывающие в текущем объекте...");
		
		new MenuItem(outlineMenu, SWT.SEPARATOR);
		
		MenuItem item3 = new MenuItem(outlineMenu, SWT.NONE);
		item3.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/goto.gif"));
		item3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				outlineDblClick();
			}
		});
		item3.setText("Перейти к определению");
		
		MenuItem item6 = new MenuItem(outlineMenu, SWT.NONE);
//		item3.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/goto.gif"));
		item6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				outlineShowProc();
			}
		});
		item6.setText("Показать в отдельном окне");
		
		MenuItem item8 = new MenuItem(outlineMenu, SWT.NONE);
		item8.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				makeTopicWithProc();
			}
		});
		item8.setText("Создать топик с ссылкой на процедуру");
		
		outline.setLabelProvider(new OutlineLabelProvider());
		outline.setContentProvider(new OutlineContentProvider(this));
		
		hierarchy = new TreeViewer(sashForm, SWT.BORDER);
		Tree tree_1 = hierarchy.getTree();
		hierarchy.setAutoExpandLevel(1);
		
		Menu hierarchyMenu = new Menu(tree_1);
		tree_1.setMenu(hierarchyMenu);
		
		MenuItem item4 = new MenuItem(hierarchyMenu, SWT.NONE);
		item4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hierarchyGo();
			}
		});
		item4.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/goto.gif"));
		item4.setText("Перейти к определению");
		
		MenuItem item5 = new MenuItem(hierarchyMenu, SWT.NONE);
		item5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hierarchyDblClick();
			}
		});
		item5.setText("Перейти к вызову");
				
		hierarchy.setLabelProvider(new HierarchyLabelProvider(this));
		hierarchy.setContentProvider(new HierarchyContentProvider(this));
		
		sashForm.setWeights(new int[] {2, 14, 14});
		
		hookDoubleClickAction();
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		getSite().getPage().addPartListener(linkWithEditorPartListener);
		
	}

	protected void makeTopicWithProc() {
		StructuredSelection selection = (StructuredSelection)outline.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			LineInfo line = (LineInfo)selection.getFirstElement();
			BuildInfo data = new BuildInfo(line.data);
			data.name = line.name;
			data.title = line.title;
			data.export = line.export;
			
			events.makeTopicWithProcLink(data);
			
		}		
	}

	protected void doSearch() {
		switch (combo.getSelectionIndex()) {
		case 0:
			searchText();
			break;
		case 1:
			searchProc();					
			break;

		default:
			break;
		} 		
	}

	protected void searchProc() {
		
		String text = search.getText();
		filter.setText(text);
		if (text.isEmpty())
			outline.removeFilter(filter);
		else
			outline.addFilter(filter);
		
	}

	protected void searchText() {
		
		outline.removeFilter(filter);
		if(editor==null) return;
		
		EditorInput input = ((EditorInput)editor.getEditorInput());
		BuildInfo data = input.getData();
		data.setSearch(search.getText());
		input.setData(data);
		editor.updateCurrentLine();		
		editor.lightWord(search.getText());
	}


	protected void hierarchyGo() {
		StructuredSelection selection = (StructuredSelection)hierarchy.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			LineInfo line = (LineInfo)selection.getFirstElement();
			line.data.name = line.name;
			pico.get(IEditorFactory.class).openEditor(line.data);
		}	
	}
	
	protected void showHierarchy(boolean showCalls, boolean inObject) {
		
		StructuredSelection selection = (StructuredSelection)outline.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			this.showCalls = showCalls;
			this.callsInObject = inObject;
			LineInfo _line = (LineInfo)selection.getFirstElement();
			LineInfo line = new LineInfo(_line);
			line.data.name = line.name;
			ArrayList<LineInfo> input = new ArrayList<LineInfo>();
			input.add(line);
			hierarchy.setInput(input);
//			hierarchy.setSelection(new StructuredSelection(line), true);
			hierarchy.expandToLevel(line, 1);
//			TreeItem[] sel = hierarchy.exgetTree().getSelection();
//			if(sel.length!=0)
//				sel[0].setExpanded(true);
		}	
		
	}

	private void hookDoubleClickAction() {
		outline.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				outlineDblClick();				
			}
		});
		
//		hierarchy.addSelectionChangedListener(new ISelectionChangedListener() {			
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//					
//			}
//		});
		
		hierarchy.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				hierarchyDblClick();			
			}
		});
	}
	
	protected void hierarchyDblClick() {
		StructuredSelection selection = (StructuredSelection)hierarchy.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			LineInfo line = (LineInfo)selection.getFirstElement();
			List<String> calleeName = new ArrayList<String>();
			BuildInfo data;
			if (showCalls) {
				data = line.data;
				data.name = line.name;
			
				if (line.parent != null)
					calleeName.add(line.parent.name);
				data.setCalleeName(calleeName);
				
			} else {
				
				data = line.parent != null ? line.parent.data : line.data;
						
				if(line.parent != null)
				{
					calleeName.add(line.name);					
				}else
				{
					TreeItem[] sel = hierarchy.getTree().getSelection();
					if(sel.length==0) return;
					for (TreeItem treeItem : sel[0].getItems()) {
						LineInfo _data = (LineInfo)treeItem.getData();
						if(_data!=null)
							calleeName.add(_data.name);
					}
				}					
				data.setCalleeName(calleeName);
				
			}
			data.setSearch("");
			pico.get(IEditorFactory.class).openEditor(data);
		}		
	}

	protected void outlineDblClick() {
		StructuredSelection selection = (StructuredSelection)outline.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			LineInfo line = (LineInfo)selection.getFirstElement();
			BuildInfo data = new BuildInfo(line.data);
			data.name = line.name;
			data.setSearch("");
//			data.calleeName = new ArrayList<String>();	
			pico.get(IEditorFactory.class).openEditor(data);
		}		
	}

	protected void outlineShowProc() {
		StructuredSelection selection = (StructuredSelection)outline.getSelection();
		if (selection.getFirstElement() instanceof LineInfo) {
			LineInfo line = (LineInfo)selection.getFirstElement();
			BuildInfo data = new BuildInfo(line.data);
			data.name = line.name;
//			data.calleeName = new ArrayList<String>();	
			data.onlyProc = true;
			pico.get(IEditorFactory.class).openEditor(data);
		}		
	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		hierarchyType = new Action() {
			public void run() {				
				showGroups = !showGroups;	
				hierarchyType.setChecked(showGroups);
			}
		};
		hierarchyType.setChecked(true);
		hierarchyType.setText("Группировать по типам");
		hierarchyType.setImageDescriptor(ResourceManager.getPluginImageDescriptor("ru.codeAnalyzer", "icons/param_list.gif"));
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
//		IToolBarManager toolbarManager = getViewSite().getActionBars()
//				.getToolBarManager();
//		toolbarManager.add(hierarchyType);
		
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
//		IMenuManager menuManager = getViewSite().getActionBars()
//				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void editorActivated(IEditorPart activeEditor) {
		if(activeEditor instanceof Editor)
		{
			((Editor)activeEditor).updateOutineView(this);
		}
	}

	@Override
	public void editorClosed() {
		outline.setInput(EMPTY_ARRAY);
		IResource resource = ResourcesPlugin.getWorkspace().getRoot();
		try {
			resource.deleteMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}
	
	public void copySearch(String text)
	{
		search.setText(text);		
	}
}
