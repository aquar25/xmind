package ru.codeanalyzer.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ru.codeanalyzer.views.core.LineInfo;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class BuildInfo implements Serializable {

	public BuildInfo() {
		super();
	}
	
	public BuildInfo(BuildInfo info) {
		super();
		
		for (Field f : this.getClass().getDeclaredFields()) {				
			try {
				f.set(this, f.get(info));
			} catch (Exception e) {
			}
		}
	}

	private static final long serialVersionUID = 47969974452366435L;
	
	public static BuildInfo readExtension(String value) {
		
		BuildInfo result = null;
		
		ObjectInputStream ois = null;
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] data = decoder.decodeBuffer(value);
			ois = new ObjectInputStream(
					new ByteArrayInputStream(data));

			result = (BuildInfo) ois.readObject();
			
		} catch (Exception e) {
			
		} finally{
			try {
				if(ois!=null)
					ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
		
	}
	
	public String buildExtension() {
		String value = "";
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			BASE64Encoder encoder = new BASE64Encoder();
			value = encoder.encodeBuffer(baos.toByteArray());
		} catch (Exception e) {
			value = "";
		}
		return value;
	}
	
	public static ArrayList<LineInfo> toLineInfo(List<BuildInfo> source, LineInfo parent){
	
		ArrayList<LineInfo> result = new ArrayList<LineInfo>();
		
		for (BuildInfo buildInfo : source) {
			LineInfo item = new LineInfo(); 
			item.data = buildInfo;
			item.name = buildInfo.name;
			item.title = buildInfo.title;
			item.export = buildInfo.export;
			item.parent = parent;
			result.add(item);
		}
		
		return result;
	}
	
	public static BuildInfo fromLineInfo(LineInfo parentElement) {
		BuildInfo info = new BuildInfo(parentElement.data);
		info.id = 0;
		info.title = parentElement.title.replace(" Ёкспорт", "");
		info.name = parentElement.name;
		info.export = parentElement.export;
		return info;
	}
	
	public int object;
	public String group1;
	public String group2;
	public String object_title;
	public int module;
	public String module_name;
	public String module_title;
	public int id;
	public String title;
	public String name;
	public Boolean export;
	
	private List<String> calleeName;
	
	public List<String> getCalleeName() {
		return calleeName==null ? new ArrayList<String>() : calleeName;

	}

	public void setCalleeName(List<String> calleeName) {
		this.calleeName = new ArrayList<String>();
		this.calleeName.addAll(calleeName);
	}

	public String version;
	public boolean onlyProc;
	public boolean compare;
//	public int calleeIndex;
	private String search;

	public String getSearch() {
		return search==null ? "" : search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

}