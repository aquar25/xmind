package ru.codeanalyzer.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.interfaces.IEvents.CompareResults;
import ru.codeanalyzer.interfaces.ITextParser;
import ru.codeanalyzer.interfaces.ITextParser.Entity;
import ru.codeanalyzer.interfaces.ITextParser.IEntity;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.utils.AesCrypt;
import ru.codeanalyzer.utils.Const;

public class DbService {

	ITextParser parser = pico.get(ITextParser.class);

	//ADD *****************************************************

	public Integer addObject(Connection con, IEntity line) throws SQLException {

		Integer index = 0;

		String SQL = "Select ID from OBJECTS WHERE GROUP1=? AND GROUP2=?";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, line.group1.toUpperCase());
		prep.setString(2, line.group2.toUpperCase());
		ResultSet rs = prep.executeQuery();
		try {
			if (rs.next())
				return rs.getInt(1);
		} finally {
			rs.close();
		}

		SQL = "INSERT INTO OBJECTS (GROUP1, GROUP2, TITLE1, TITLE2) VALUES (?,?,?,?)";
		prep = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);

		prep.setString(1, line.group1.toUpperCase());
		prep.setString(2, line.group2.toUpperCase());
		prep.setString(3, line.group1);
		prep.setString(4, line.group2);

		ResultSet generatedKeys = null;
		try {
			int affectedRows = prep.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException();

			generatedKeys = prep.getGeneratedKeys();
			if (generatedKeys.next())
				index = generatedKeys.getInt(1);
			else
				throw new SQLException();
		} finally {
			generatedKeys.close();
		}
		return index;
	}

	public Integer addModule(Connection con, Entity line, Integer object)
			throws SQLException {

		Integer index = 0;

		String SQL = "Select ID from MODULES WHERE OBJECT=? AND NAME=?";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, object);
		prep.setString(2, line.module.toUpperCase());
		ResultSet rs = prep.executeQuery();
		try {
			if (rs.next())
				return rs.getInt(1);
		} finally {
			rs.close();
		}

		SQL = "INSERT INTO MODULES (OBJECT, NAME, TITLE) VALUES (?,?,?)";
		prep = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);

		prep.setInt(1, object);
		prep.setString(2, line.module.toUpperCase());
		prep.setString(3, line.module);

		ResultSet generatedKeys = null;
		try {
			int affectedRows = prep.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException();

			generatedKeys = prep.getGeneratedKeys();
			if (generatedKeys.next())
				index = generatedKeys.getInt(1);
			else
				throw new SQLException();
		} finally {
			generatedKeys.close();
		}
		return index;

	}

	public void addProcedure(Connection con, Entity line, Integer object,
			Integer module) throws SQLException {

		String SQL = "INSERT INTO PROCS (OBJECT, MODULE, NAME, TITLE, EXPORT) VALUES (?,?,?,?,?)";
		PreparedStatement prep = con.prepareStatement(SQL,
				Statement.RETURN_GENERATED_KEYS);

		prep.setInt(1, object);
		prep.setInt(2, module);
		prep.setString(3, line.proc_name.toUpperCase());
		prep.setString(4, line.proc_title);
		prep.setBoolean(5, line.export);
		
		int affectedRows = prep.executeUpdate();
		if (affectedRows == 0)
			throw new SQLException();

		Integer index = 0;
		ResultSet generatedKeys = null;
		try {
			generatedKeys = prep.getGeneratedKeys();
			if (generatedKeys.next())
				index = generatedKeys.getInt(1);
			else
				throw new SQLException();
		} finally {
			generatedKeys.close();
		}

		SQL = "INSERT INTO PROCS_TEXT (PROC, TEXT, HASH) VALUES (?,?,?)";
		prep = con.prepareStatement(SQL);

		prep.setInt(1, index);
		prep.setCharacterStream(2, new BufferedReader(new StringReader(
				line.text.toString())));
		prep.setString(3, AesCrypt.getHash(line.text.toString().getBytes()));
		
		affectedRows = prep.executeUpdate();
		if (affectedRows == 0)
			throw new SQLException();
		
		if(line.params==null)
			return;
		
		for (String parameter : line.params) {
			SQL = "INSERT INTO PROCS_PARAMETERS (PROC, KEY) VALUES (?,?)";
			prep = con.prepareStatement(SQL);

			prep.setInt(1, index);
			prep.setString(2, parameter.trim());

			affectedRows = prep.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException();	
		}		

	}

	//DELETE *****************************************************

	public int deleteProcs(Connection con, Integer object, Integer module)
			throws SQLException {
		String SQL = "DELETE FROM PROCS WHERE OBJECT=? AND MODULE=?";
		PreparedStatement prep = con.prepareStatement(SQL);

		prep.setInt(1, object);
		prep.setInt(2, module);

		return prep.executeUpdate();

	}

	//SERVICE *************************************************************
	
	private String prepareString(String t) {
		return t.replace(" ", "").toUpperCase();		
	}

	private BuildInfo getProc(Connection con, int index) throws SQLException {
		
		BuildInfo item = new BuildInfo();
				
		String SQL = "Select T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T.ID, T.TITLE, T.NAME, T.EXPORT FROM "
				+ "PROCS AS T "
				+ "JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID "
				+ "JOIN MODULES AS T2 ON T.MODULE = T2.ID " + "WHERE T.ID = ?";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, index);
		ResultSet rs = prep.executeQuery();

		try {
			if (rs.next()) {

				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);

				String t1 = prepareString(item.group1);
				String t2 = prepareString(item.group2);
				if (t1.equalsIgnoreCase(t2))
					item.object_title = rs.getString(2);
				else
					item.object_title = rs.getString(2) + "." + rs.getString(3);
				item.module = rs.getInt(4);
				item.module_name = rs.getString(5);
				item.module_title = rs.getString(6);

				item.id = rs.getInt(7);
				item.title = rs.getString(8);
				// if (rs.getBoolean(10))
				// item.title = item.title.concat(" Ёкспорт");
				item.name = rs.getString(9);
				item.export = rs.getBoolean(10);

			} else
				throw new SQLException();
		} finally {
			rs.close();
		}

		return item;
	}
	
	public int getProcCount(Connection con) throws SQLException {

		String SQL = "Select COUNT(ID) from PROCS";
		Statement stat = con.createStatement();
		ResultSet rs = stat.executeQuery(SQL);

		try {
			if (rs.next())
				return rs.getInt(1);
			else
				throw new SQLException();
		} finally {
			rs.close();
		}
	}
	
	//FINDING *************************************************************
	
	private void findProcs(List<BuildInfo> result, Connection con,
			String func_name, BuildInfo context) throws SQLException {

//		String version = getVersion(con);
//		if (version == null) return;
		
		func_name = func_name.trim();

		// выделим слово перед точкой
		ArrayList<Integer> objects = new ArrayList<Integer>();
		String _p;
		Pattern _r;
		Matcher _m;

		_p = ".*\\.";
		_r = Pattern.compile(_p);
		_m = _r.matcher(func_name);
		if (_m.find()) {
			objects = findObject(con, _m.group().replace(".", ""));
			if (objects.isEmpty())
				return;
			else
				func_name = func_name.replace(_m.group(), "");
		}

		BuildInfo item;
		String SQL;
		PreparedStatement prep;
		ResultSet rs;

		if (objects.isEmpty()) {
			SQL = "Select ID from PROCS WHERE (NAME = ? AND (MODULE = ? OR OBJECT = ?))";
			prep = con.prepareStatement(SQL);
			prep.setString(1, func_name);
			prep.setInt(2, context.module);
			prep.setInt(3, context.object);
		} else {
			SQL = "Select ID from PROCS WHERE (NAME = ? AND OBJECT IN(?))";
			prep = con.prepareStatement(SQL);
			prep.setString(1, func_name);
			prep.setObject(2, objects.toArray());
		}
		// prep.setString(3, func_name);
		// prep.setInt(4, context.module);
		rs = prep.executeQuery();

		try {
			boolean added = false;
			while (rs.next()) {
				item = getProc(con, rs.getInt(1));
//				item.version = version;
				result.add(item);
				added = true;
			}
			if (added)
				return;

			rs.close();
			SQL = "Select ID from PROCS WHERE (NAME = ? AND MODULE != ? AND EXPORT)";
			prep = con.prepareStatement(SQL);
			prep.setString(1, func_name);
			prep.setInt(2, context.module);
			rs = prep.executeQuery();
			while (rs.next()) {
				// DONE ошибка при переходе: Ќаборƒвижений. онтрольќстатков
				item = getProc(con, rs.getInt(1));
//				item.version = version;
				result.add(item);
			}

			return;

		} finally {
			rs.close();
		}

	}

	private ArrayList<Integer> findObject(Connection con, String name)
			throws SQLException {
	
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		name = name.trim();
		
		String SQL;
		PreparedStatement prep;
		ResultSet rs;
	
		SQL = "Select ID from OBJECTS WHERE (GROUP1 = ? OR GROUP2 = ?)";
		prep = con.prepareStatement(SQL);
		prep.setString(1, name);
		prep.setString(2, name);
		
		rs = prep.executeQuery();
	
		try {
			while(rs.next()) {
				result.add(rs.getInt(1));
				
			} 
			
			return result;
			
		} finally {
			rs.close();
		}
	
	}

	private int findProc(Connection con, int module, String name)
			throws SQLException {
	
		String SQL = "SELECT T.ID FROM " + "PROCS AS T "
				+
				// "JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID "
				+ "WHERE T.MODULE=? AND T.NAME = ?";

		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, module);
		prep.setString(2, name);
		ResultSet rs = prep.executeQuery();

		try {
			if (rs.next()) {

				return rs.getInt(1);
			}
			return 0;

		} finally {
			rs.close();
		}
	
	}
	
	private int findModule(Connection con, BuildInfo data)
			throws SQLException {
	
		String SQL;
		PreparedStatement prep;
		ResultSet rs;
	
		SQL = "SELECT T2.ID FROM " +
				"OBJECTS AS T " +
				"JOIN MODULES AS T2 ON T.ID = T2.OBJECT " +
				"WHERE (T.TITLE1 = ? AND T.TITLE2 = ? AND T2.NAME = ?)";
		prep = con.prepareStatement(SQL);
		prep.setString(1, data.group1);
		prep.setString(2, data.group2);
		prep.setString(3, data.module_name);
		
		rs = prep.executeQuery();
	
		try {
			if (rs.next()) {
				return rs.getInt(1);
			} 
			return 0;
			
		} finally {
			rs.close();
		}
	
	}
		
	//SEARCH *************************************************************
	
	public void getTextSearchList(List<BuildInfo> list, Connection con, String titleText
			, IProgressMonitor monitor) throws SQLException {
		//DONE глобальный поиск: SELECT * FROM PROCS_TEXT where UPPER(TEXT) regexp UPPER('–егис“–—¬≈ƒ≈Ќ»….÷еныЌоменклатуры')
		
//		String version = getVersion(con);
//		if (version==null) return;
		
//		String SQL = "SELECT T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T3.PROC, T.TITLE, T.NAME, T.EXPORT FROM " +
//				"PROCS_TEXT AS T3 " +
//				"JOIN PROCS AS T ON T3.PROC = T.ID " +
//				"JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +
//				"JOIN MODULES AS T2 ON T.MODULE = T2.ID " +
//				"WHERE UPPER(T3.TEXT) REGEXP UPPER(?)";
//		PreparedStatement prep = con.prepareStatement(SQL);
//		prep.setString(1, titleText.replace("(", "\\(").replace(")", "\\)"));
//		ResultSet rs = prep.executeQuery();
//
//		try {
//			while (rs.next()) {
//
//				BuildInfo item = new BuildInfo();
////				item.version = version;
//				
//				item.object = rs.getInt(1);
//				item.group1 = rs.getString(2);
//				item.group2 = rs.getString(3);
//				
//				String t1 = prepareString(item.group1);
//				String t2 = prepareString(item.group2);				
//				if(t1.equalsIgnoreCase(t2))
//					item.object_title = rs.getString(2);
//				else
//					item.object_title = rs.getString(2) + " - " + rs.getString(3);
//				item.module = rs.getInt(4);
//				item.module_name = rs.getString(5);
//				item.module_title = rs.getString(6);
//				item.id = rs.getInt(7);
//				item.title = rs.getString(8);
//				//DONE Ѕыло бы здорово видеть выделение цветом (или значком?) экспортных процедур.
//				if(rs.getBoolean(10))
//					item.title = item.title.concat(" Ёкспорт");
//				item.name = rs.getString(9);
//				item.export = rs.getBoolean(10);
//				
//				result.add(item);
//			} 
//		} finally {
//			rs.close();
//		}

		
		String SQL = "SELECT count(T.ID) FROM " +
				"OBJECTS AS T";
		PreparedStatement prep = con.prepareStatement(SQL);
		ResultSet rs = prep.executeQuery();
		
		try {
			if (rs.next()) {
				monitor.beginTask("јнализ объектов конфигурации...", rs.getInt(1));	
			}
		} finally {
			rs.close();
		}	
		
		SQL = "SELECT T.ID, T.TITLE1, T.TITLE2 FROM " +  
				"OBJECTS AS T ";
		
		prep = con.prepareStatement(SQL);
		ResultSet rs1 = prep.executeQuery();
		
		
		SQL = "SELECT T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T.ID, T.TITLE, T.NAME, T.EXPORT, T0.TEXT FROM " +  
				"PROCS_TEXT AS T0 " +
				"JOIN PROCS AS T ON T0.PROC = T.ID " +
				"JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID" +
				"	AND T.OBJECT=(?)";		
		
		try {
			while (rs1.next()) {
				
				monitor.subTask(rs1.getString(2) + "." + rs1.getString(3));
				
				prep = con.prepareStatement(SQL);
//				prep.setString(1, "[ \\s]*" + data.name + "\\(");
//				prep.setString(1, data.name);
				prep.setInt(1, rs1.getInt(1));
				
				rs = prep.executeQuery();
				try {
					while (rs.next()) {
						
						Reader in = rs.getCharacterStream(11);
						BufferedReader bufferedReader = new BufferedReader(in);
						String line;
						boolean finded = false;
						while ((line = bufferedReader.readLine()) != null) {
							if (parser.findTextInLine(line, titleText)){
								finded = true;
								break;
							}
						}
						if (finded) {
							BuildInfo item = new BuildInfo();

							item.object = rs.getInt(1);
							item.group1 = rs.getString(2);
							item.group2 = rs.getString(3);

							String t1 = prepareString(item.group1);
							String t2 = prepareString(item.group2);
							if (t1.equalsIgnoreCase(t2))
								item.object_title = rs.getString(2);
							else
								item.object_title = rs.getString(2) + "."
										+ rs.getString(3);
							item.module = rs.getInt(4);
							item.module_name = rs.getString(5);
							item.module_title = rs.getString(6);

							item.id = rs.getInt(7);
							item.title = rs.getString(8);

							item.name = rs.getString(9);
							item.export = rs.getBoolean(10);
							list.add(item);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new SQLException();
				} finally {
					rs.close();
					monitor.worked(1);
				}
			}
		} finally {
			rs1.close();
		}	
		
	}

	public void getObjectSearchList(List<BuildInfo> result, Connection con, String titleText) throws SQLException {
		
//		String version = getVersion(con);
//		if (version==null) return;
		
		String SQL = "SELECT T.ID, T.TITLE1, T.TITLE2 FROM " +
				"OBJECTS AS T " +
				"WHERE UPPER(T.TITLE1) REGEXP UPPER(?) OR UPPER(T.TITLE2) REGEXP UPPER(?)";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, titleText);
		prep.setString(2, titleText);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);
				
//				String t1 = prepareString(item.group1);
//				String t2 = prepareString(item.group2);				
//				if(t1.equalsIgnoreCase(t2))
//					item.object_title = rs.getString(2);
//				else
//					item.object_title = rs.getString(2) + " - " + rs.getString(3);
//				item.module = rs.getInt(4);
//				item.module_name = rs.getString(5);
//				item.module_title = rs.getString(6);
//				item.id = rs.getInt(7);
//				item.title = rs.getString(8);
//				//DONE Ѕыло бы здорово видеть выделение цветом (или значком?) экспортных процедур.
//				if(rs.getBoolean(10))
//					item.title = item.title.concat(" Ёкспорт");
//				item.name = rs.getString(9);
//				item.line = rs.getInt(10);
				
				result.add(item);
			} 
		} finally {
			rs.close();
		}

	}

	public void getProcsSearchList(List<BuildInfo> result, Connection con, String titleText) throws SQLException {
		
//		String version = getVersion(con);
//		if (version==null) return;
		
		String SQL = "SELECT T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T.ID, T.TITLE, T.NAME, T.EXPORT FROM " +  
				"PROCS AS T " + 
				"JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +  
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID " + 
				"WHERE UPPER(T.NAME ) REGEXP UPPER(?)";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, titleText);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);
				
				String t1 = prepareString(item.group1);
				String t2 = prepareString(item.group2);				
				if(t1.equalsIgnoreCase(t2))
					item.object_title = rs.getString(2);
				else
					item.object_title = rs.getString(2) + " - " + rs.getString(3);
				item.module = rs.getInt(4);
				item.module_name = rs.getString(5);
				item.module_title = rs.getString(6);
				item.id = rs.getInt(7);
				item.title = rs.getString(8);
				//DONE Ѕыло бы здорово видеть выделение цветом (или значком?) экспортных процедур.
				if(rs.getBoolean(10))
					item.title = item.title.concat(" Ёкспорт");
				item.name = rs.getString(9);
				item.export = rs.getBoolean(10);
				
				result.add(item);
			} 
		} finally {
			rs.close();
		}

	}

	//BUILD *************************************************************
	
	public void getObjectList(List<BuildInfo> list, Connection con,
			String titleText, boolean exact) throws SQLException {

//		String version = getVersion(con);
//		if (version==null) return;
		
		String SQL = "SELECT T.ID, T.TITLE1, T.TITLE2 FROM " +  
				"OBJECTS AS T ";
		if(exact)
			SQL = SQL.concat("WHERE UPPER(T.TITLE1)=UPPER(?) ");
		else
			SQL = SQL.concat("WHERE UPPER(T.TITLE1) REGEXP UPPER(?) ");
		
		SQL = SQL.concat("ORDER BY T.TITLE1, T.TITLE2");
		
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, titleText);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);
				
				list.add(item);
			} 
		} finally {
			rs.close();
		}
		
	}

	public void getConcreteObjectList(List<BuildInfo> list, Connection con,
			String parentText, String topicText, boolean exact) throws SQLException {

//		String version = getVersion(con);
//		if (version==null) return;
		
		String SQL = "SELECT T.ID, T.TITLE1, T.TITLE2, T2.ID, T2.NAME, T2.TITLE FROM " +  
				"OBJECTS AS T " +
				"JOIN MODULES AS T2 ON T2.OBJECT = T.ID " +
				"WHERE UPPER(T.TITLE1)=UPPER(?) ";
		if(exact)
			SQL = SQL.concat("AND UPPER(T.TITLE2) = UPPER(?)");
		else
			SQL = SQL.concat("AND UPPER(T.TITLE2) REGEXP UPPER(?)");
	
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, parentText);
		prep.setString(2, topicText);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);
				
				if (exact) {
					String t1 = prepareString(item.group1);
					String t2 = prepareString(item.group2);
					if (t1.equalsIgnoreCase(t2))
						item.object_title = rs.getString(2);
					else
						item.object_title = rs.getString(2) + " - "
								+ rs.getString(3);
					item.module = rs.getInt(4);
					item.module_name = rs.getString(5);
					item.module_title = rs.getString(6);
				}
				list.add(item);
			} 
		} finally {
			rs.close();
		}
		
	}

	public void getRootList(List<BuildInfo> list, Connection con) throws SQLException {
		
//		String version = getVersion(con);
//		if (version==null) return;
		
		String SQL = "SELECT T.ID, T.TITLE1, T.TITLE2 FROM " +  
				"OBJECTS AS T ";
		SQL = SQL.concat("ORDER BY T.TITLE1, T.TITLE2");
		
		PreparedStatement prep = con.prepareStatement(SQL);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);
				
				list.add(item);
			} 
		} finally {
			rs.close();
		}
		
	}
	
	public void getModuleList(List<BuildInfo> list, Connection con, BuildInfo data) throws SQLException {
		
		int module = findModule(con, data);
		if(module==0) return;
		
//		String version = getVersion(con);
//		if (version==null) return;
		
		String SQL = "SELECT T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T.ID, T.TITLE, T.NAME, T.EXPORT FROM " +  
				"PROCS AS T " +
				"JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID " +
				"WHERE T.MODULE=? ";
		
	
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, module);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);

				String t1 = prepareString(item.group1);
				String t2 = prepareString(item.group2);
				if (t1.equalsIgnoreCase(t2))
					item.object_title = rs.getString(2);
				else
					item.object_title = rs.getString(2) + "."
							+ rs.getString(3);
				item.module = rs.getInt(4);
				item.module_name = rs.getString(5);
				item.module_title = rs.getString(6);
				
				item.id = rs.getInt(7);
				item.title = rs.getString(8);
				if (rs.getBoolean(10))
					item.title = item.title.concat(" Ёкспорт");
				item.name = rs.getString(9);
				item.export = rs.getBoolean(10);
				list.add(item);
			}
		} finally {
			rs.close();
		}
		
	}

	public void getParamsList(List<String> paramsList, Connection con, BuildInfo data) throws SQLException {

		int module = findModule(con, data);
		if(module==0) return;
	
		int proc = findProc(con, module, data.name);
		if(proc==0) return;
		
		String SQL = "Select T.KEY, T.VALUE FROM " +
				"PROCS_PARAMETERS AS T " + 
				"WHERE T.PROC = ? ORDER BY T.ID";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, proc);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				paramsList.add(rs.getString(1));
			} 
		} finally {
			rs.close();
		}
		
	}
	
	//GET TEXT *************************************************************
	
	public String getProcText(Connection con, BuildInfo data) throws SQLException, IOException {
		int module = findModule(con, data);
		if(module==0) return Const.MODULE_NOT_FOUND;
		
		int proc = findProc(con, module, data.name);
		if(proc==0) return Const.PROC_NOT_FOUND;
		
		return getProcText(con, proc);

	}
	
	public String getProcText(Connection con, int id)
			throws SQLException, IOException {

		StringBuilder result = new StringBuilder();

		String SQL = "Select TEXT from PROCS_TEXT WHERE ID=?";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, id);
		ResultSet rs = prep.executeQuery();
		BufferedReader bufferedReader = null;

		try {
			if (rs.next()) {

				Reader in = rs.getCharacterStream(1);
				bufferedReader = new BufferedReader(in);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line + "\n");
				}
			}
		} finally {
			rs.close();
		}
		return result.toString();
	}
	
	public String getModuleText(Connection con, BuildInfo data) throws SQLException, IOException {
		
		int module = findModule(con, data);
		if(module==0) return Const.MODULE_NOT_FOUND;
		
		StringBuilder result = new StringBuilder();

		String SQL = "Select T2.TEXT FROM " +
				"PROCS AS T " +
				"JOIN PROCS_TEXT AS T2 ON T.ID = T2.PROC " +
				"WHERE T.MODULE=?";
		
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, module);
		ResultSet rs = prep.executeQuery();
		BufferedReader bufferedReader = null;

		try {
			while (rs.next()) {

				Reader in = rs.getCharacterStream(1);
				bufferedReader = new BufferedReader(in);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line + "\n");
				}
				result.append("\n");
			}
		} finally {
			rs.close();
		}
		return result.toString();
		
	}

	@SuppressWarnings("unused")
	private String getObjectText(Connection con, BuildInfo data) throws SQLException, IOException {
			
		StringBuilder result = new StringBuilder();

		String SQL = "Select T2.TEXT FROM " +
				"PROCS AS T " +
				"JOIN PROCS_TEXT AS T2 ON T.ID = T2.PROC " +
				"JOIN OBJECTS AS T3 ON T.OBJECT = T3.ID " +
				"WHERE T3.TITLE1=? AND T3.TITLE2=? " +
				"ORDER BY T.NAME ";
		
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, data.group1);
		prep.setString(2, data.group2);
		
		ResultSet rs = prep.executeQuery();
		BufferedReader bufferedReader = null;

		try {
			while (rs.next()) {

				Reader in = rs.getCharacterStream(1);
				bufferedReader = new BufferedReader(in);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line + "\n");
				}
				result.append("\n");
			}
		} finally {
			rs.close();
		}
		return result.toString();
	}

	//HIERARCHY *************************************************************
	
	public void getCalls(List<BuildInfo> list, Connection con, BuildInfo data, boolean callsInObject, IProgressMonitor monitor) throws SQLException {

		int module = findModule(con, data);
		if(module==0) return;
		
//		String version = getVersion(con);
//		if (version==null) return;
		
		if(data.export && !callsInObject)
		{
			getCallsExport(list, con, data, monitor);
			return;
		}
		
		String SQL = "SELECT T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T.ID, T.TITLE, T.NAME, T.EXPORT FROM " +  
				"PROCS_TEXT AS T0 " +
				"JOIN PROCS AS T ON T0.PROC = T.ID " +
				"JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID " +
				"WHERE UPPER(T0.TEXT) REGEXP UPPER(?) " +
				"	AND T.NAME!=(?) ";
		if(!data.export)
			SQL = SQL.concat("AND T.MODULE = ? ");
		if(callsInObject)
			SQL = SQL.concat("AND T.OBJECT = ? ");
//		else
//			SQL = SQL.concat("AND UPPER(T.TITLE2) REGEXP UPPER(?)");
	
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, "[ \\s]*" + data.name + "\\(");
		prep.setString(2, data.name);
		if(!data.export)
		{
			prep.setInt(3, module);
			if(callsInObject)
				prep.setInt(4, data.object);
		}else
			if(callsInObject)
				prep.setInt(3, data.object);
		
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				BuildInfo item = new BuildInfo();
//				item.version = version;
				
				item.object = rs.getInt(1);
				item.group1 = rs.getString(2);
				item.group2 = rs.getString(3);

				String t1 = prepareString(item.group1);
				String t2 = prepareString(item.group2);
				if (t1.equalsIgnoreCase(t2))
					item.object_title = rs.getString(2);
				else
					item.object_title = rs.getString(2) + "."
							+ rs.getString(3);
				item.module = rs.getInt(4);
				item.module_name = rs.getString(5);
				item.module_title = rs.getString(6);
				
				item.id = rs.getInt(7);
				item.title = rs.getString(8);
//				if (rs.getBoolean(10))
//					item.title = item.title.concat(" Ёкспорт");
				item.name = rs.getString(9);
				item.export = rs.getBoolean(10);
				list.add(item);
			}
		} finally {
			rs.close();
		}
		
//		return list;
	}

	private void getCallsExport(List<BuildInfo> list, Connection con,
			BuildInfo data, IProgressMonitor monitor) throws SQLException {

		
		String SQL = "SELECT count(T.ID) FROM " +
				"OBJECTS AS T";
		PreparedStatement prep = con.prepareStatement(SQL);
		ResultSet rs = prep.executeQuery();
		
		try {
			if (rs.next()) {
				monitor.beginTask("јнализ объектов конфигурации...", rs.getInt(1));	
			}
		} finally {
			rs.close();
		}	
		
		SQL = "SELECT T.ID, T.TITLE1, T.TITLE2 FROM " +  
				"OBJECTS AS T ";
		
		prep = con.prepareStatement(SQL);
		ResultSet rs1 = prep.executeQuery();
		
		
		SQL = "SELECT T.OBJECT, T1.TITLE1, T1.TITLE2, T.MODULE, T2.NAME, T2.TITLE, T.ID, T.TITLE, T.NAME, T.EXPORT, T0.TEXT FROM " +  
				"PROCS_TEXT AS T0 " +
				"JOIN PROCS AS T ON T0.PROC = T.ID " +
				"JOIN OBJECTS AS T1 ON T.OBJECT = T1.ID " +
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID " +
				"WHERE " +
				//"UPPER(T0.TEXT) REGEXP UPPER(?) " +
				"	T.NAME!=(?) " +
				"	AND T.OBJECT=(?)";		
		
		try {
			while (rs1.next()) {
				
				monitor.subTask(rs1.getString(2) + "." + rs1.getString(3));
				
				prep = con.prepareStatement(SQL);
//				prep.setString(1, "[ \\s]*" + data.name + "\\(");
				prep.setString(1, data.name);
				prep.setInt(2, rs1.getInt(1));
				
				rs = prep.executeQuery();
				try {
					while (rs.next()) {
						
						Reader in = rs.getCharacterStream(11);
						BufferedReader bufferedReader = new BufferedReader(in);
						String line;
						boolean finded = false;
						while ((line = bufferedReader.readLine()) != null) {
							if (parser.findCallee(line, data.name)) {
								finded = true;
								break;
							}
						}
						if (finded) {
							BuildInfo item = new BuildInfo();

							item.object = rs.getInt(1);
							item.group1 = rs.getString(2);
							item.group2 = rs.getString(3);

							String t1 = prepareString(item.group1);
							String t2 = prepareString(item.group2);
							if (t1.equalsIgnoreCase(t2))
								item.object_title = rs.getString(2);
							else
								item.object_title = rs.getString(2) + "."
										+ rs.getString(3);
							item.module = rs.getInt(4);
							item.module_name = rs.getString(5);
							item.module_title = rs.getString(6);

							item.id = rs.getInt(7);
							item.title = rs.getString(8);

							item.name = rs.getString(9);
							item.export = rs.getBoolean(10);
							list.add(item);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new SQLException();
				} finally {
					rs.close();
					monitor.worked(1);
				}
			}
		} finally {
			rs1.close();
		}		
		
	}

	public void getCalled(List<BuildInfo> list, Connection con, BuildInfo context) throws SQLException, IOException {
		
		int module = findModule(con, context);
		if(module==0) return;
			
		Set<String> procs = new LinkedHashSet<String>();

		StringBuilder result = new StringBuilder();

		String SQL = "Select T0.TEXT FROM " +
				"PROCS_TEXT AS T0 " +
				"JOIN PROCS AS T ON T.ID = T0.PROC " +
				"JOIN MODULES AS T2 ON T.MODULE = T2.ID " +
				"WHERE T.MODULE=? " +
				"  AND T.NAME=?";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, module);
		prep.setString(2, context.name);
		ResultSet rs = prep.executeQuery();
		BufferedReader bufferedReader = null;

		try {
			if (rs.next()) {

				Reader in = rs.getCharacterStream(1);
				bufferedReader = new BufferedReader(in);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line + "\n");
					List<String> parse = parser.findProcsInString(line, context.name); 
					procs.addAll(parse);
				}
			}
		} finally {
			rs.close();
		}

		List<BuildInfo> list1 = new ArrayList<BuildInfo>();
		
		for (String proc : procs) {
			
			list1.clear();
			findProcs(list1, con, proc, context);
			switch (list1.size()) {
			case 0:				
				break;
			case 1:
				list.add(list1.get(0));
				break;
			default:
				BuildInfo item = new BuildInfo();
				item.module = -1;
				item.id = -1;
				item.name = proc;
				item.title = proc + "(...)";
				item.group1 = "не опознан";
				item.group2 = "не опознан";
				item.export = false;
				item.object_title = "объект";
				item.module_title = "модуль";
				list.add(item);
				break;
			}	

		}	
//		Collections.sort(list,new Comparator<BuildInfo>() {
//
//			@Override
//			public int compare(BuildInfo o1, BuildInfo o2) {
//				 return o1.name.compareTo(o2.name);
//			}
//		});
		
	}
	
	public void getProcsInLine(List<BuildInfo> list, Connection con, String line, BuildInfo context) throws SQLException {

		List<String> procs = parser.findProcsInString(line, "");

		for (String proc : procs) {

			findProcs(list, con, proc, context);
		}
	}

	//COMPARE *************************************************************
	
	public void compareModules(CompareResults compareResults, Connection con1,
			Connection con2, BuildInfo data, IProgressMonitor monitor) throws SQLException, IOException, InterruptedException {

//		String version1 = getVersion(con1);
//		if (version1==null) return;
		
		
		List<BuildInfo> list1 = new ArrayList<BuildInfo>();
		getModuleList(list1, con1, data);
				
		List<BuildInfo> list2 = new ArrayList<BuildInfo>();
		getModuleList(list2, con2, data);
		
		List<BuildInfo> _list2 = new ArrayList<BuildInfo>(list2);
		
//		monitor.beginTask(Const.COMPARE_WORK, list1.size());
		
		monitor.beginTask(Const.COMPARE_WORK, list1.size());
		
		for (BuildInfo item : list1) {
			
			monitor.subTask(item.title);
			
			String proc1 = getProcHash(con1, item.id);
			boolean equals = false;
			boolean added = true;
			boolean changed = false;
			
			for (BuildInfo item2 : list2) {

				if (item.name.equalsIgnoreCase(item2.name)) {

					added = false;
					
					String proc2 = getProcHash(con2, item2.id);

					if (proc1.equalsIgnoreCase(proc2))
						equals = true;
					else
						changed = true;

					list2.remove(item2);
					monitor.worked(1);
					break;
				}				
			}
			
			if (equals)  compareResults.equals.add(item);
			if (changed) compareResults.changed.add(item);
			if (added)   compareResults.added.add(item);		
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
		monitor.beginTask(Const.COMPARE_WORK, _list2.size());
		
		for (BuildInfo item2 : _list2) {
			
			monitor.subTask(item2.title);
			
			boolean removed = true;
			for (BuildInfo item1 : list1) {
				if (item2.name.equalsIgnoreCase(item1.name)) 
				{
					removed = false;
					list1.remove(item1);
					break;
				}
			}
			
			if (removed)   compareResults.removed.add(item2);		
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
	}

	public void compareObjects(CompareResults compareResults, Connection con1,
			Connection con2, String parentText, String topicText, IProgressMonitor monitor) throws SQLException, IOException, InterruptedException {

		List<BuildInfo> list1 = new ArrayList<BuildInfo>();
		getConcreteObjectList(list1, con1, parentText, topicText, true);
				
		List<BuildInfo> list2 = new ArrayList<BuildInfo>();
		getConcreteObjectList(list2, con2, parentText, topicText, true);
		
		List<BuildInfo> _list2 = new ArrayList<BuildInfo>(list2);
		
		monitor.beginTask(Const.COMPARE_WORK_ACTIVE, list1.size());
		
		for (BuildInfo item : list1) {
			
			monitor.subTask(item.group1 + "." + item.group2);
			
			String text1 = getModuleHash(con1, item);
			boolean equals = false;
			boolean added = true;
			boolean changed = false;
			
			for (BuildInfo item2 : list2) {

				if (item.object_title.equalsIgnoreCase(item2.object_title)) {

					added = false;
					
					String text2 = getModuleHash(con2, item2);

					if (text1.equalsIgnoreCase(text2))
						equals = true;
					else
						changed = true;

					list2.remove(item2);
					break;
				}				
			}
			
			if (equals)  compareResults.equals.add(item);
			if (changed) compareResults.changed.add(item);
			if (added)   compareResults.added.add(item);		
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
		monitor.beginTask(Const.COMPARE_WORK_NON_ACTIVE, _list2.size());
		
		for (BuildInfo item2 : _list2) {
			
			monitor.subTask(item2.group1 + "." + item2.group2);
			
			boolean removed = true;
			for (BuildInfo item1 : list1) {
				if (item2.object_title.equalsIgnoreCase(item1.object_title)) 
				{
					removed = false;
					list1.remove(item1);
					break;
				}
			}
			
			if (removed)   compareResults.removed.add(item2);	
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
	}

	public void compareObjects(CompareResults compareResults, Connection con1,
			Connection con2, String titleText, IProgressMonitor monitor) throws SQLException, IOException, InterruptedException {

		List<BuildInfo> list1 = new ArrayList<BuildInfo>();
		getObjectList(list1, con1, titleText, true);
				
		List<BuildInfo> list2 = new ArrayList<BuildInfo>();
		getObjectList(list2, con2, titleText, true);
		
		List<BuildInfo> _list2 = new ArrayList<BuildInfo>(list2);
		
		monitor.beginTask(Const.COMPARE_WORK_ACTIVE, list1.size());
		
		for (BuildInfo item : list1) {
			
			monitor.subTask(item.group1 + "." + item.group2);
			
			String text1 = getObjectHash(con1, item);
			boolean equals = false;
			boolean added = true;
			boolean changed = false;
			
			for (BuildInfo item2 : list2) {

				if (item.group1.equalsIgnoreCase(item2.group1)&&
						item.group2.equalsIgnoreCase(item2.group2)) {

					added = false;
					
					String text2 = getObjectHash(con2, item2);

					if (text1.equalsIgnoreCase(text2))
						equals = true;
					else
						changed = true;

					list2.remove(item2);
					break;
				}				
			}
			
			if (equals)  compareResults.equals.add(item);
			if (changed) compareResults.changed.add(item);
			if (added)   compareResults.added.add(item);	
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
		monitor.beginTask(Const.COMPARE_WORK_NON_ACTIVE, _list2.size());
		
		for (BuildInfo item2 : _list2) {
			
			monitor.subTask(item2.group1 + "." + item2.group2);
			
			boolean removed = true;
			for (BuildInfo item1 : list1) {
				if (item2.group1.equalsIgnoreCase(item1.group1)&&
						item2.group2.equalsIgnoreCase(item1.group2)) 
				{
					removed = false;
					list1.remove(item1);
					break;
				}
			}
			
			if (removed)   compareResults.removed.add(item2);		
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
	}

	public void compareObjects(CompareResults compareResults, Connection con1,
			Connection con2, IProgressMonitor monitor) throws SQLException, IOException, InterruptedException {

		List<BuildInfo> list1 = new ArrayList<BuildInfo>();
		getRootList(list1, con1);
				
		List<BuildInfo> list2 = new ArrayList<BuildInfo>();
		getRootList(list2, con2);
		
		List<BuildInfo> _list2 = new ArrayList<BuildInfo>(list2);
		
		monitor.beginTask(Const.COMPARE_WORK_ACTIVE, list1.size());
		
		for (BuildInfo item : list1) {
			
			monitor.subTask(item.group1 + "." + item.group2);
			
			String text1 = getObjectHash(con1, item);
			boolean equals = false;
			boolean added = true;
			boolean changed = false;
			
			for (BuildInfo item2 : list2) {

				if (item.group1.equalsIgnoreCase(item2.group1)&&
						item.group2.equalsIgnoreCase(item2.group2)) {

					added = false;
					
					String text2 = getObjectHash(con2, item2);

					if (text1.equalsIgnoreCase(text2))
						equals = true;
					else
						changed = true;

					list2.remove(item2);
					break;
				}				
			}
			
			if (equals)  compareResults.equals.add(item);
			if (changed) compareResults.changed.add(item);
			if (added)   compareResults.added.add(item);		
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
		monitor.beginTask(Const.COMPARE_WORK_NON_ACTIVE, _list2.size());
		
		for (BuildInfo item2 : _list2) {
			
			monitor.subTask(item2.group1 + "." + item2.group2);
			
			boolean removed = true;
			for (BuildInfo item1 : list1) {
				if (item2.group1.equalsIgnoreCase(item1.group1)&&
						item2.group2.equalsIgnoreCase(item1.group2)) 
				{
					removed = false;
					list1.remove(item1);
					break;
				}
			}
			
			if (removed)   compareResults.removed.add(item2);		
			
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
		
	}

	private String getObjectHash(Connection con, BuildInfo data) throws SQLException, IOException {
		StringBuilder result = new StringBuilder();

		String SQL = "Select T2.HASH FROM " +
				"PROCS AS T " +
				"JOIN PROCS_TEXT AS T2 ON T.ID = T2.PROC " +
				"JOIN OBJECTS AS T3 ON T.OBJECT = T3.ID " +
				"WHERE T3.TITLE1=? AND T3.TITLE2=? " +
				"ORDER BY T.NAME ";
		
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, data.group1);
		prep.setString(2, data.group2);
		
		ResultSet rs = prep.executeQuery();
		try {
			while (rs.next()) {
				
				String line = rs.getString(1);;
				result.append(line + "\n");
			}
		} finally {
			rs.close();
		}
		return result.toString();
	}


	private String getModuleHash(Connection con, BuildInfo data) throws SQLException {
		int module = findModule(con, data);
		if(module==0) return Const.MODULE_NOT_FOUND;
		
		StringBuilder result = new StringBuilder();

		String SQL = "Select T2.HASH FROM " +
				"PROCS AS T " +
				"JOIN PROCS_TEXT AS T2 ON T.ID = T2.PROC " +
				"WHERE T.MODULE=?";
		
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, module);
		ResultSet rs = prep.executeQuery();

		try {
			while (rs.next()) {

				String line = rs.getString(1);;
				result.append(line + "\n");
			}
		} finally {
			rs.close();
		}
		return result.toString();
	}

	private String getProcHash(Connection con, int id) throws SQLException {
		StringBuilder result = new StringBuilder();

		String SQL = "Select HASH from PROCS_TEXT WHERE ID=?";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setInt(1, id);
		ResultSet rs = prep.executeQuery();

		try {
			if (rs.next()) {

				String line = rs.getString(1);;
				result.append(line + "\n");
			}
		} finally {
			rs.close();
		}
		return result.toString();
	}



//	public String getVersion(Connection con) throws SQLException {
//	
//	String SQL = "SELECT T.VERSION FROM " +
//			"CONFIG_INFO AS T ";
//	PreparedStatement prep = con.prepareStatement(SQL);
//	ResultSet rs = prep.executeQuery();
//
//	try {
//		if (rs.next()) {
//			return rs.getString(1);
//		} else
//			throw new SQLException();
//	} finally {
//		rs.close();
//	}
//}







	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
