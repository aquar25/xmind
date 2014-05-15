package ru.codeanalyzer.views;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.interfaces.IEditorFactory;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.views.core.LineInfo;

public class ProcCall extends ViewPart {

	public static final String ID = "ru.codeanalyzer.views.ProcCall"; //$NON-NLS-1$
	private TreeViewer treeViewer;

	public ProcCall() {
	}

	private static class contentProvider implements ITreeContentProvider  {

//		IEvents events = pico.get(IEvents.class);
		
		private static final Object[] EMPTY_ARRAY = new Object[0];
//		private OutlineView view;
		 
		public contentProvider(ProcCall view) {
//			view = outlineView;
		}

		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Collection)
				return ((Collection<LineInfo>) inputElement).toArray();
			else
				return EMPTY_ARRAY;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}
	
	private static class labelProvider extends StyledCellLabelProvider {
		
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString text = new StyledString();
			
//			cell.setImage(ResourceManager.getPluginImage("ru.codeAnalyzer", "icons/markers/codeanalyzer-proc.png"));
			
			if (element instanceof LineInfo) {
				LineInfo info = (LineInfo) element;
				text.append(info.title + (info.export ? " Ёкспорт": ""));
				text.append(" : " + info.data.object_title.concat("." + info.data.module_title), StyledString.QUALIFIER_STYLER);
//				if(info.parent==null)
//					text.append(" : " + (view.showCalls?"вызываетс€":"вызывает"), StyledString.COUNTER_STYLER);
			}
			
			cell.setText(text.toString());
		    cell.setStyleRanges(text.getStyleRanges());
		    super.update(cell);
		}
	}
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		{
			treeViewer = new TreeViewer(parent, SWT.BORDER);
			treeViewer.setLabelProvider(new labelProvider());
			treeViewer.setContentProvider(new contentProvider(this));
		}

		createActions();
		initializeToolBar();
		initializeMenu();
		
		hookDoubleClickAction();
	}

	private void hookDoubleClickAction() {
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection)treeViewer.getSelection();
				if (selection.getFirstElement() instanceof LineInfo) {
					LineInfo line = (LineInfo)selection.getFirstElement();
					BuildInfo data = new BuildInfo(line.data);
					data.name = line.name;
//					data.calleeName = new ArrayList<String>();	
					pico.get(IEditorFactory.class).openEditor(data);
				}				
			}
		});
		
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
//		IToolBarManager toolbarManager = getViewSite().getActionBars()
//				.getToolBarManager();
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

	public void setInput(ArrayList<LineInfo> list) {
		treeViewer.setInput(list);
		
	}

}
