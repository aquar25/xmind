package ru.codeanalyzer.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.editor.core.EditorInput;
import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.IEditorFactory;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.IHistory;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.preferences.PreferenceConstants;

public class EditorFactory implements IEditorFactory {

	HashMap<String, EditorInput> inputs = new HashMap<String, EditorInput>();
	IHistory history = pico.get(IHistory.class);
	IDbManager DbManager = pico.get(IDbManager.class);
	IPreferenceStore store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
	
	@Override
	public IEditorPart openEditorWithOutHistory(BuildInfo data) {
		IEditorPart result = open(data);
		return result;
	}
	
	@Override
	public IEditorPart openEditor(BuildInfo data) {
		if(data.id<0)
		{
			pico.get(IEvents.class).goToProcedure(data.title, data);
			return null;
		}
		
		IEditorPart result = open(data);
		if(result!=null)
			history.setCurrent(data);
		return result;
	}
	
	private IEditorPart open(BuildInfo data)
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		if (page == null)
			return null;
		IEditorPart editor = null;
		try {
			
			if (!pico.get(IEvents.class).activeConfigLoadedCheck()) return null;
			
			EditorInput input = getInput(data);
			editor = page.findEditor(input);
			if (editor == null)
			{
				editor = page.openEditor(input, Editor.ID, true);
				if(store.getBoolean(PreferenceConstants.OPEN_EDITOR_IN_BOTTOM_WINDOW))
					((Editor)editor).splitEditorArea(false);
			}
			else
			{
				//editor.init((IEditorSite) editor.getSite(), input);
				page.openEditor(input, Editor.ID, true);
				((Editor)editor).updateCurrentLine();
		
			}
			
//			IWorkbench workbench = PlatformUI.getWorkbench();
//			@SuppressWarnings("restriction")
//			IAdaptable pageInput = ((Workbench) workbench).getDefaultPageInput();
//			workbench.openWorkbenchWindow(EditorPerspective.ID, pageInput);
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().openPage(EditorPerspective.ID, pageInput);
			
			pico.get(IEvents.class).openPerspective();			
			
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}

		return editor;
	}
	
	@Override
	public void deleteInput(EditorInput input) {
		String key = getKey(input.getData());
		inputs.remove(key);
	}
	
	//***********************************************************************************
	
	private EditorInput getInput(BuildInfo data)
	{
		String key = getKey(data);
		
		EditorInput input = inputs.get(key);
		if (input == null) {
			input = new EditorInput(data);
			inputs.put(key, input);
		} else
			input.setData(data);
		return input;
		
	}

	private String getKey(BuildInfo data) {
		String key;
		if(data.onlyProc)
			key = DbManager.getActive().getId().concat(Integer.toString(data.module)).concat(data.name);
		else
			key = DbManager.getActive().getId().concat(Integer.toString(data.module));	
		return key;
	}

	@Override
	public void closeEditors() {
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IEditorReference[] editorReferences = workbenchPage.getEditorReferences();
		
		ArrayList<IEditorReference> editors = new ArrayList<IEditorReference>();
		
		for (int i = 0; i < editorReferences.length; i++) {			
			if (editorReferences[i].getId().equalsIgnoreCase(Editor.ID)) {
				editors.add(editorReferences[i]);
			}
		}		
		IEditorReference[] editorRefs = editors.toArray(new IEditorReference[editors.size()]);
		workbenchPage.closeEditors(editorRefs, false);
				
	}
	
	



	


	
}
