package ru.codeanalyzer.editor.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.interfaces.IDb;
import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.ITextParser;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.utils.Const;

public class EditorStorage implements IStorage{

	IDbManager dbManager = pico.get(IDbManager.class);
	IEvents events = pico.get(IEvents.class);
	
	public EditorStorage(BuildInfo data) {
		super();
		this.data = data;
		
		if (!events.activeConfigLoadedCheck()) 
			return; 
		
		IDb db = dbManager.getActive();
		this.config_name = db.getName();
	}

	BuildInfo data;
	public String config_name;
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public InputStream getContents() throws CoreException {
		
		try {

			if (!events.activeConfigLoadedCheck()) return new ByteArrayInputStream(
					"".getBytes(Const.DEFAULT_CHARACTER_ENCODING)); 
			
			IDb db = dbManager.getActive();
			this.config_name = db.getName();
			
			String text = "";
			if (data.onlyProc)
			{
				text = db.getProcText(data);
				if(data.compare)
				{
					if (!events.nonActiveConfigLoadedCheck()) return new ByteArrayInputStream(
							"".getBytes(Const.DEFAULT_CHARACTER_ENCODING)); 
					IDb db1 = dbManager.getNonActive();
					String text1 = db1.getProcText(data);
					text = pico.get(ITextParser.class).compare(text, text1);
				}
			}else
				text = db.getModuleText(data);

			return new ByteArrayInputStream(
					text.getBytes(Const.DEFAULT_CHARACTER_ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new CoreException(null);
		}

	}

	@Override
	public IPath getFullPath() {
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

}
