package ru.codeanalyzer.views.core;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.codeanalyzer.editor.Editor;
import ru.codeanalyzer.interfaces.ILinkedWithEditorView;

public class LinkWithEditorPartListener implements IPartListener2 {

	private final ILinkedWithEditorView view;

	public LinkWithEditorPartListener(ILinkedWithEditorView view) {
		this.view = view;
	}

	@Override
	public void partActivated(IWorkbenchPartReference ref) {
		if (ref.getPart(true) instanceof IEditorPart) {
			view.editorActivated(view.getViewSite().getPage().getActiveEditor());
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference ref) {
		if (ref.getPart(true) == view) {
			view.editorActivated(view.getViewSite().getPage().getActiveEditor());
		}

	}

	@Override
	public void partVisible(IWorkbenchPartReference ref) {
		if (ref.getPart(true) == view) {
			IEditorPart editor = view.getViewSite().getPage().getActiveEditor();
			if (editor != null) {
				view.editorActivated(editor);
			}
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference ref) {
		if (ref.getPart(true) == view) {
			view.editorActivated(view.getViewSite().getPage().getActiveEditor());
		}

	}
	
	@Override
	public void partClosed(IWorkbenchPartReference ref) {
		if (ref.getId().equalsIgnoreCase(Editor.ID)) {
			IEditorReference[] editorReferences = view.getViewSite().getPage().getEditorReferences();
			
			int editorCount = 0;
			for (int i = 0; i < editorReferences.length; i++) {			
				if (editorReferences[i].getId().equalsIgnoreCase(Editor.ID)) {
					editorCount++;	
				}
			}
			
			if(editorCount==0)
				view.editorClosed();
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {

	}

}
