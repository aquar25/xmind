package ru.codeanalyzer.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class EditorPerspective implements IPerspectiveFactory {

	public static String ID = "ru.codeanalyzer.perspectives.EditorPerspective";
	
	private IPageLayout factory;

	public EditorPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
		addPerspectiveShortcuts();
		addViewShortcuts();
	}

	private void addViews() {
	
		IFolderLayout topRight =
			factory.createFolder(
				"topRight",
				IPageLayout.RIGHT,
				0.75f,
				factory.getEditorArea());
		topRight.addView(ru.codeanalyzer.views.MainView.ID);
		
		IFolderLayout bottomRight =
				factory.createFolder(
					"bottomRight",
					IPageLayout.BOTTOM,
					0.5f,
					"topRight");
		bottomRight.addView(ru.codeanalyzer.views.OutlineView.ID);
		bottomRight.addView("org.xmind.ui.OverviewView");
		
		
//		factory.addFastView("org.xmind.ui.OverviewView",0.20f); 
//		factory.addFastView(IPageLayout.ID_OUTLINE, 0.10f); 
//		factory.addFastView(ru.codeanalyzer.views.OutlineView.ID, 0.10f);
		
	}

	private void addPerspectiveShortcuts() {
		factory.addPerspectiveShortcut(EditorPerspective.ID);
	}

	private void addViewShortcuts() {
		factory.addShowViewShortcut(ru.codeanalyzer.views.MainView.ID); 
		factory.addShowViewShortcut(ru.codeanalyzer.views.OutlineView.ID);
	}

}
