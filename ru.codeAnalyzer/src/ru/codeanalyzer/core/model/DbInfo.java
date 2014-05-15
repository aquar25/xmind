package ru.codeanalyzer.core.model;

import java.io.Serializable;
import java.lang.reflect.Field;

import ru.codeanalyzer.interfaces.ILoaderService.operationType;

public class DbInfo implements Serializable{
	
	public class SQLConnection implements Serializable{

//		jdbc:microsoft:sqlserver://192.168.0.1:1433;databaseName=work;selectMethod=cursor
		public SQLConnection(String path, String user, String password) {
			this.path = path;
			this.user = user;
			this.password = password;
		}
		private static final long serialVersionUID = 9219264161306294280L;
		
		public String getConnectionString(){
			String _p = path.replace('\\', '-');
			String[] _path = _p.split("-");
			if(_path.length!=2) return "";
			
			return "jdbc:sqlserver://" + _path[0] + 
					";databaseName=" + _path[1] +
					";selectMethod=cursor";
		}
		
		public String user;
		public String password;
		public String path;
	}
	
	public DbInfo() {
		super();
		for (Field f : this.getClass().getDeclaredFields()) {				
			try {
				if (f.getType().isAssignableFrom(Boolean.class))
					f.set(this, false);
				else if (f.getType().isAssignableFrom(operationType.class))
					f.set(this, operationType.fromDirectory);						
				else
					f.set(this, "");
			} catch (Exception e) {
			}
		}
	}
	private static final long serialVersionUID = -6530408724026513483L;

	public String name;
	public operationType type;
	public String path;
	public String db_path;

	public SQLConnection sql;

}
