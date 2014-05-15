package ru.codeanalyzer.editor;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

public class EditorSourceViewer extends ProjectionViewer {
	private Editor editor;

	public EditorSourceViewer(Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean showAnnotationsOverview,
			int styles, Editor editor) {
				
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview,
				styles);
		
		this.editor = editor;
	}

	public void doubleClicked() {
		
		editor.doubleClicked();	
	}
}
