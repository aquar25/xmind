package ru.codeanalyzer.views.core;

import java.lang.reflect.Field;

import ru.codeanalyzer.core.model.BuildInfo;

public class LineInfo{

	public LineInfo() {
		super();
	}
	
	public LineInfo(LineInfo info) {
		super();
		
		for (Field f : this.getClass().getDeclaredFields()) {				
			try {
				if (f.getType().isAssignableFrom(BuildInfo.class))
					f.set(this, new BuildInfo((BuildInfo)f.get(info)));
				else
					f.set(this, f.get(info));
			} catch (Exception e) {
			}
		}
	}
	
	public int line;
	public int offset;
	public String title;
	public String name;
	public Boolean export;
	
	public BuildInfo data;
	
	public LineInfo parent;
	
}