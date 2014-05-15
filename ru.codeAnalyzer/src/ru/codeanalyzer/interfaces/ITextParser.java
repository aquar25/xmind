package ru.codeanalyzer.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.codeanalyzer.exceptions.ProcNotFoundException;

public interface ITextParser {

	public class ProcStartReader{
		public ProcStartReader(BufferedReader bufferedReader) {
			this.bufferedReader = bufferedReader;
		}
		public BufferedReader bufferedReader;
		public StringBuilder temp;
	} 
	
	public abstract class IEntity {
		public String group1;
		public String group2;
		public String module;
	}

	public class Entity extends IEntity {
		public String proc_name;
		public String proc_title;
		public StringBuilder text;
		public Boolean export;
		public String[] params;
	}

	public class ConfigEntity extends IEntity {
		public String property;
		public String value;
	}
	
	public abstract void parseObject(File f, IEntity line) throws Exception;

	public abstract boolean isCommentOrDirective(String file_line);

	public abstract List<String> findProcsInString(String _line, String exclude_name);

	public abstract boolean findCallee(String line, String calleeName);

	public abstract boolean findProcEnd(String file_line);
	
	public abstract boolean findProcStart(String line, String name);

	public abstract int getProcInfo(Entity proc, ArrayList<String> buffer, ArrayList<String> vars) throws ProcNotFoundException;

	public abstract String compare(String text, String text1);

	public abstract boolean findCompareMarker(String line);

	boolean findTextInLine(String line, String text);
	

}