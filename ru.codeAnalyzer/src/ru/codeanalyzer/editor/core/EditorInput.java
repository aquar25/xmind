package ru.codeanalyzer.editor.core;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import ru.codeanalyzer.core.model.BuildInfo;

public class EditorInput implements IStorageEditorInput{

	BuildInfo data;
	
	public EditorInput(BuildInfo data) {
		super();
		this.data = data;
	}

	public BuildInfo getData() {
		return data;
	}
	
	public void setData(BuildInfo data) {
		this.data = data;
	}
	
	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		if(data.onlyProc)
			try {
				return ((EditorStorage)getStorage()).config_name + ": " + data.title;
			} catch (CoreException e) {
				e.printStackTrace();
				return data.title;				
			}
		else
			return data.object_title.concat("." + data.module_title);
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		
		return data.title==null ? data.group2 : data.title;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
//		if (IResource.class.equals(adapter)) {
//			return ResourcesPlugin.getWorkspace().getRoot();
//		}
		return null;
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return new EditorStorage(data);
	}

}
