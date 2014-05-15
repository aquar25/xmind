package ru.codeanalyzer.editor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PartStack;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.editor.core.EditorInput;
import ru.codeanalyzer.editor.core.ReconcilingStrategy;
import ru.codeanalyzer.interfaces.IEditorFactory;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.views.OutlineView;

//FUTURE возможность редактирования/сохранения/выгрузки базы в виде txt???
//FUTURE каширование результатов иерархии вызовов
//DONE оптимизация поиска вызывающих процедур

@SuppressWarnings("restriction")
public class Editor extends TextEditor {

	IEvents events = pico.get(IEvents.class);
	
	public final static String ID = "ru.codeAnalyzer.Editor";
	public final static String CONTEXT_ID = "ru.codeAnalyzer.editor.context";
	
	ProjectionSupport projectionSupport;

//	IColorManager colorManager;

	List<ProjectionAnnotation> Annotations = new ArrayList<ProjectionAnnotation>();

	ProjectionAnnotationModel annotationModel;
	
//	EditorOutlinePage outlinePage;
	
	IEditorFactory factory = pico.get(IEditorFactory.class);
	
	ReconcilingStrategy reconciler;
	

	IAction collapseAll;
	IAction expandAll;
	private ArrayList<SimpleMarkerAnnotation> current_markers = new ArrayList<SimpleMarkerAnnotation>();

//	private Editor linkedEditor;
//	private IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);

	
	public Editor() {
		super();
		setSourceViewerConfiguration(new EditorConfiguration(this));
		setDocumentProvider(new DocumentProvider());
		setEditorContextMenuId(CONTEXT_ID);
		makeActions();

	}

	private void makeActions() {
		
		collapseAll = new Action() {
			public void run() {				
				annotationModel.collapseAll(0, getDocument().getLength());			
			}
		};
		collapseAll.setText("Свернуть всё");
//		collapseAll.setToolTipText("Показать список вызываемых процедур");
//		collapseAll.setAccelerator(SWT.CTRL&SWT.SHIFT&SWT.ARROW_RIGHT);
		
		expandAll = new Action() {
			public void run() {				
				annotationModel.expandAll(0, getDocument().getLength());			
			}
		};
		expandAll.setText("Развернуть всё");
//		collapseAll.setToolTipText("Показать список вызываемых процедур");
//		expandAll.setAccelerator(SWT.CTRL&SWT.SHIFT&SWT.ARROW_RIGHT);
	}
	
	public IDocument getDocument()
	{
		return getDocumentProvider().getDocument(getEditorInput());
	}
	
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
//		menu.add(collapseAll);
		super.editorContextMenuAboutToShow(menu);

		 // Create a menu flyout.
//        MenuManager submenu = new MenuManager("Перспективы"); //$NON-NLS-1$
//        submenu.add(new ChangeToPerspectiveMenu(getSite().getWorkbenchWindow(),""));
//        menu.add(submenu);
	}
	
	public void dispose() {
		IEditorInput input = getEditorInput();
		if(input instanceof EditorInput)
			factory.deleteInput((EditorInput)input);
		
//		colorManager.dispose();
//		if(linkedEditor!=null)
//			linkedEditor.deletLink();
		super.dispose();		
	}

	public void createPartControl(Composite parent) {
		
//		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		super.createPartControl(parent);
//		TreeViewer outline = new TreeViewer(parent, SWT.BORDER);
		
//		sashForm.setWeights(new int[] {1, 1});
		
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(viewer,
				getAnnotationAccess(), getSharedColors());
		projectionSupport.install();

//		projectionSupport.addSummarizableAnnotationType(annotationType);
		
		// turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = viewer.getProjectionAnnotationModel();

		((StyledText)this.getAdapter(org.eclipse.swt.widgets.Control.class)).addCaretListener(new CaretListener() {
			
			@Override
			public void caretMoved(CaretEvent event) {
				
				setCurrentInOutline(event);				
				
//				setCurrentInLinkedEditor(event);
			}
		});
	}

//	protected void setCurrentInLinkedEditor(CaretEvent event) {
//		if(linkedEditor==null)
//			return;
//		
////		IDocumentProvider provider = getDocumentProvider();
////		IDocument document = provider.getDocument(getEditorInput());
//		
//		IDocument LinkedDocument = linkedEditor.getDocumentProvider().getDocument(linkedEditor.getEditorInput());
//		
//		ITextSelection textSelection = (ITextSelection) getSelectionProvider().getSelection();
//		int line = textSelection.getOffset();
//		
////		IRegion line = null;
//		try {
////			line = document.getLineInformationOfOffset(offset);
//			
//			IRegion region = LinkedDocument.getLineInformationOfOffset(line);
//			TextSelection selection = new TextSelection(LinkedDocument, region.getOffset(), 0);
//			linkedEditor.getSelectionProvider().setSelection(selection);
//			
//		} catch (BadLocationException e) {
//
//			e.printStackTrace();
//		}
//
//	}

	protected void setCurrentInOutline(CaretEvent event) {
		int offset = event.caretOffset;
		int h = 0;
//		System.out.println(" - " + offset);
		for (ProjectionAnnotation item : Annotations) {
			Position p = annotationModel.getPosition(item);
			
			String[] data = item.getText().split(":");
			int l = Integer.parseInt(data[1]);
			if (item.isCollapsed())
				h += (p.length - l - 1);
			
//			System.out.println(p.offset + ":" + p.length + ":" + h + ":" + data[0]);
			
			if(offset + h < p.offset)
				break;
				
			if((offset + h) > p.offset && (offset + h < p.offset + p.length))
			{
				reconciler.setCurrentInOutline(data[0]);
				break;
			}
								
		}
		
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new EditorSourceViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles, this);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		// do any custom stuff
		
		return viewer;
	}

	public ISourceViewer getViewer()
	{
		return getSourceViewer();
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
//		if (adapter.equals(outlineView.class)) {
//			if (outlinePage == null)
//				outlinePage = new EditorOutlinePage();
//
//			if(reconciler!=null)
//				reconciler.setOutlinePage(outlinePage);
//			
//			return outlinePage;
//		}
		return super.getAdapter(adapter);
	}
	
	public void updateCurrentLine()
	{
		if(reconciler==null) return;
//		annotationModel.collapseAll(0, getDocument().getLength());	
		reconciler.updateCurrentLine();
//		((Document)getDocument()).repairLineInformation();
//		getSourceViewerConfiguration().getReconciler(null).getReconcilingStrategy(null).
		
	}

	public void setReconcilerStrategy(ReconcilingStrategy reconciler)
	{
		this.reconciler = reconciler;

	}
	
	public void updateOutineView(OutlineView view)
	{
		if(reconciler!=null)
			reconciler.updateOutineView(view);

	}
	
	public void updateMarkers(ArrayList<Position> markers) {
		
		
		IDocumentProvider idp = getDocumentProvider();
		IDocument document = idp.getDocument(getEditorInput());
		IAnnotationModel iamf = idp.getAnnotationModel(getEditorInput());
		
		iamf.connect(document);

		try {
			
			for (SimpleMarkerAnnotation ma : current_markers) {
				iamf.removeAnnotation(ma);
			}
			current_markers.clear();
			
//			IResource resource = ResourcesPlugin.getWorkspace().getRoot();
//			resource.deleteMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
			
			for (int i = 0; i < markers.size(); i++) {
				Position position = markers.get(i);
				
//				IMarker marker = createMarker(0, 0, 10);
				IMarker marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.BOOKMARK);
//				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
//				marker.setAttribute(IMarker.CHAR_START, position.offset);
//				marker.setAttribute(IMarker.CHAR_END, position.offset + 10);
//				HashMap<String, Object> map= new HashMap<String, Object>();
////				map.put(IMarker.LOCATION, cu.getElementName());
//				map.put(IMarker.MESSAGE, "Test marker");
//				map.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
////				map.put(IMarker.LINE_NUMBER, 100);
//				map.put(IMarker.CHAR_START, 0);
//				map.put(IMarker.CHAR_END, 10);
//				marker.setAttributes(map);
				
				MarkerAnnotation ma = new MarkerAnnotation(marker);
				iamf.addAnnotation(ma, position);
				current_markers.add(ma);
			}
//			createMarker(0, 0, 10);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		iamf.disconnect(document);
	}
	
	public void updateFoldingStructure(LinkedHashMap<String, Position> fPositions) {
		annotationModel.removeAllAnnotations();
		Annotations.clear();
		
		for (String item : fPositions.keySet()) {
			
//		}
//		for (int i = 0; i < fPositions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation(false);
			annotationModel.addAnnotation(annotation, fPositions.get(item));
			annotation.setText(item);
			Annotations.add(annotation);
		}

//		addAnnotation(new Position(10, 10));
		
		// this will hold the new annotations along
		// with their corresponding positions
//		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();

//			newAnnotations.put(annotation, positions.get(i));
//
//			annotations[i] = annotation;
			 
//			IPath path = new Path("icons\test.bmp");
//			IFile file = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.MARKER)
//			IMarker marker;
//			try {
//				marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.MARKER);
//				marker.setAttribute(IMarker.MESSAGE, "This is my marker");
//				marker.setAttribute("Age", 5);
//				SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(marker);
//				annotationModel.addAnnotation((Annotation)ma, positions.get(i));
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}			
//		}
		
//		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
//		annotationModel.expandAll(0, getDocument().getLength());
//		annotationModel.collapseAll(0, getDocument().getLength());
		
		
//		IDocument document = getDocumentProvider().getDocument(getEditorInput());
//		annotationModel.disconnect(document);
//		annotationModel.connect(document);
//		
//		oldAnnotations = annotations;
	}
		
	public void highlightLine(int line)
	{
		IDocumentProvider provider = getDocumentProvider();
		EditorInput input = (EditorInput)getEditorInput();
		IDocument document = provider.getDocument(input);
//		BuildInfo data = input.getData();
//		System.out.println(data.calleeIndex);
		try {
			TextSelection selection;
//			IResource resource = ResourcesPlugin.getWorkspace().getRoot();
//			IMarker[] markers = resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
//			if (data.calleeIndex!= 0) {
//				IMarker m = markers[data.calleeIndex-1];
//				AbstractMarkerAnnotationModel iamf = (AbstractMarkerAnnotationModel) provider.getAnnotationModel(input);
//				Position region = iamf.getMarkerPosition(m);
//				selection = new TextSelection(document, region.getOffset(), region.getLength());
//			}else
			if (current_markers.size() == 1) {
				IMarker m = current_markers.get(0).getMarker();
				AbstractMarkerAnnotationModel iamf = (AbstractMarkerAnnotationModel) provider.getAnnotationModel(input);
				Position region = iamf.getMarkerPosition(m);
				selection = new TextSelection(document, region.getOffset(), region.getLength());
			} else {
				IRegion region = document.getLineInformation(line);
				selection = new TextSelection(document, region.getOffset(), region.getLength());				
			}
			if(!input.getData().getSearch().isEmpty())
				expandAll();
			else
			{
				if(current_markers.size()>1)
					collapseAll();
				getSelectionProvider().setSelection(selection);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}		

	}
	
	public void setOffset(int offset)
	{
		IDocumentProvider provider = getDocumentProvider();
		IDocument document = provider.getDocument(getEditorInput());

		TextSelection selection = new TextSelection(document, offset, 0);
		getSelectionProvider().setSelection(selection);

	}
	
	public int getOffset()
	{
		ITextSelection textSelection = (ITextSelection) getSelectionProvider().getSelection();
		int offset = textSelection.getOffset();
		return offset;

	}
	
	/**
	 * Split the editor area if there is at least two editors in it.
	 * http://eclipse.dzone.com/tips/programmatically-split-editor-
	 * @param right 
	 */
	public void splitEditorArea(boolean right) {
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IEditorReference[] editorReferences = workbenchPage.getEditorReferences();
		
		int editorCount = 0;
		for (int i = 0; i < editorReferences.length; i++) {			
			if (editorReferences[i].getId().equalsIgnoreCase(Editor.ID)) {
				editorCount++;	
			}
		}
		boolean alreadyHaveEditor = editorCount>1;
		
		IEditorPart activeEditor = workbenchPage.getActiveEditor();
		
		EditorPane partPaneSource = null;
		EditorPane layoutPartSource = null;
		EditorStack stackSource = null;
//		EditorSashContainer sachSource = null;

		EditorPane partPane;
		EditorPane layoutPart;
		EditorStack stack;
		
		EditorSashContainer sach;

		partPane = (EditorPane) ((PartSite) workbenchPage.getActiveEditor().getSite()).getPane();
		layoutPart = (EditorPane) partPane.getPart();
		stack = (EditorStack) layoutPart.getContainer();
		sach = (EditorSashContainer) stack.getContainer();
		//! sach==sachSource
		//! sach.getPage() == workbenchPage
		
		for (int i = 0; i < editorReferences.length; i++) {
	
			if (!alreadyHaveEditor) {
				if (editorReferences[i].getId().equalsIgnoreCase("org.xmind.ui.MindMapEditor")) {
					
					partPaneSource = (EditorPane) ((PartSite) editorReferences[i].getEditor(false).getSite()).getPane();
					layoutPartSource = (EditorPane) partPaneSource.getPart();
					stackSource = (EditorStack) layoutPartSource.getContainer();
//					sachSource = (EditorSashContainer) stackSource.getContainer();
				}
			} else {
				
				if (editorReferences[i].getId().equalsIgnoreCase(Editor.ID)) {
					
					if(editorReferences[i].getEditor(false)==activeEditor)
						continue;
		
					partPaneSource = (EditorPane) ((PartSite) editorReferences[i].getEditor(false).getSite()).getPane();
					layoutPartSource = (EditorPane) partPaneSource.getPart();
					stackSource = (EditorStack) layoutPartSource.getContainer();
//					sachSource = (EditorSashContainer) stackSource.getContainer();
				}
			}
		}

		if(stackSource==null) return;		
		
		PartStack newPart = EditorStack.newEditorWorkbook(sach, sach.getPage());
		sach.stack(layoutPart, newPart);
//		
		if (!alreadyHaveEditor)
		{			
			sach.add((LayoutPart) newPart, IPageLayout.BOTTOM, 0.60f, sach.findBottomRight());
		}
		else {
			if (right) {
				
				sach.add((LayoutPart) newPart, IPageLayout.RIGHT, 0.50f, sach.findBottomRight());
				
			} else {
				partPane.setWorkbook(stackSource);
				sach.addEditor((EditorPane) partPane, stackSource);
				workbenchPage.activate(activeEditor);
			}

		}

	}

	//*******************************************************
	
	public void expandAll()
	{	
//		annotationModel.expandAll(0, getDocument().getLength());
		for (ProjectionAnnotation item : Annotations) {
			annotationModel.expand(item);						
		}
	}
	
	public void collapseAll()
	{	
//		annotationModel.collapseAll(0, getDocument().getLength());
		for (ProjectionAnnotation item : Annotations) {
			annotationModel.collapse(item);						
		}
	}
	
	public void goToProcedure() {

		BuildInfo context = ((EditorInput)getEditorInput()).getData();
		
		IDocumentProvider provider = getDocumentProvider();
		IDocument document = provider.getDocument(getEditorInput());
		
		ITextSelection textSelection = (ITextSelection) getSelectionProvider().getSelection();
		int offset = textSelection.getOffset();
		
		IRegion line = null;
		try {
			line = document.getLineInformationOfOffset(offset);
			String _line = document.get(line.getOffset(), line.getLength());
			
			events.goToProcedure(_line, context);						
			
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
		
	}

	public void lightWord(String text) {
		((EditorConfiguration)getSourceViewerConfiguration()).lightWord(text);
		
	}

	public void doubleClicked() {

		IDocumentProvider provider = getDocumentProvider();
		IDocument document = provider.getDocument(getEditorInput());
		
		ITextSelection textSelection = (ITextSelection) getSelectionProvider().getSelection();
		String _line;
		try {
			_line = document.get(textSelection.getOffset(), textSelection.getLength());
			lightWord(_line);
		} catch (BadLocationException e) {
		
			e.printStackTrace();
		}					
	}
	 
}
