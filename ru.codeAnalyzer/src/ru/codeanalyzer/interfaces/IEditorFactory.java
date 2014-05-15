package ru.codeanalyzer.interfaces;

import org.eclipse.ui.IEditorPart;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.editor.core.EditorInput;

public interface IEditorFactory {

	IEditorPart openEditor(BuildInfo data);
	
	IEditorPart openEditorWithOutHistory(BuildInfo data);

	void deleteInput(EditorInput input);

	void closeEditors();

}