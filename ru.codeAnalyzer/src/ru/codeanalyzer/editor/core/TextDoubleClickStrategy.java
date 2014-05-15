package ru.codeanalyzer.editor.core;

import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

import ru.codeanalyzer.editor.EditorSourceViewer;

public class TextDoubleClickStrategy extends DefaultTextDoubleClickStrategy {

	@Override
	public void doubleClicked(ITextViewer text) {

		super.doubleClicked(text);
		
		if(text instanceof EditorSourceViewer)
			((EditorSourceViewer)text).doubleClicked();
		
	}

}
