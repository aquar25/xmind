package ru.codeanalyzer.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ru.codeanalyzer.exceptions.LiscenseException;
import ru.codeanalyzer.exceptions.LoadConfigException;
import ru.codeanalyzer.interfaces.IAuthorize;
import ru.codeanalyzer.interfaces.ICData;
import ru.codeanalyzer.interfaces.IDb;
import ru.codeanalyzer.interfaces.IDb.DbState;
import ru.codeanalyzer.interfaces.ILoaderService;
import ru.codeanalyzer.interfaces.ITextParser;
import ru.codeanalyzer.interfaces.ITextParser.Entity;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.utils.Const;
import ru.codeanalyzer.utils.Utils;

//DONE избавиться от таблицы взаимных вызовов - список будет формироваться динамически

public class LoaderService implements ILoaderService{

	IAuthorize sign = pico.get(IAuthorize.class);
	ITextParser parser = pico.get(ITextParser.class);
	DbService service = new DbService();
	SQLService sql_service = new SQLService();

	//loadFromDb ****************************************************************************************
	
	@Override
	public void loadFromDb(IDb db, IProgressMonitor monitor) throws InvocationTargetException {

		//ПРОВЕРКИ ******************************************************
		
		File folder = db.getDbPath().toFile();
		if (!folder.exists())
			throw new InvocationTargetException(new LoadConfigException(),
					Const.ERROR_CONFIG_PATH);

		//ЗАГРУЗКА ******************************************************
		
		try {

			monitor.beginTask(Const.MSG_CONFIG_CHECK, 0);
			
			checkSructure(db);

//			if (linkTableFilled(db))
//				db.setState(DbState.LoadedWithLinkTable);
//			else
//				db.setState(DbState.Loaded);
			
			if(!sign.check())	
				if(!checkLisence(db))
					throw new LiscenseException();
			
			db.setState(DbState.Loaded);
			
		} catch (LiscenseException e) {
			throw new InvocationTargetException(e, e.message);
		} catch (Exception e) {
			throw new InvocationTargetException(e,
					Const.ERROR_CONFIG_OPEN_DATABASE);
		} finally {
			monitor.done();
		}		
		
	}
	
	private void checkSructure(IDb db) throws IllegalAccessException,
			FileNotFoundException, InstantiationException,
			ClassNotFoundException, SQLException {

		final class checker {
			boolean checkColumns(DatabaseMetaData metadata, String table,
					String str_columns) throws SQLException {
				String[] clmns = str_columns.split(",");

				List<String> columns = new ArrayList<String>();
				ResultSet rs = metadata.getColumns(null, null, table, "%");
				while (rs.next())
					columns.add(rs.getString("COLUMN_NAME"));
				rs.close();

				boolean haveColumns = clmns.length != 0;
				for (String clmn : clmns) {
					haveColumns = haveColumns && columns.contains(clmn.trim());
				}

				return haveColumns;
			}
		}

		IPath path = db.getDbPath();
		
		if (!path.isValidPath(path.toString()))
			throw new FileNotFoundException();

		Connection con = null;
		boolean haveStructure;
		try {
			con = db.getConnection(true);

			DatabaseMetaData metadata = con.getMetaData();

			checker ch = new checker();
			haveStructure = ch.checkColumns(metadata, "OBJECTS",
					"GROUP1, GROUP2, TAG")
					&& ch.checkColumns(metadata, "MODULES", "OBJECT, NAME")
					&& ch.checkColumns(metadata, "PROCS",
							"OBJECT, MODULE, NAME, TITLE, EXPORT")
					&& ch.checkColumns(metadata, "PROCS_TEXT", "TEXT, HASH")
					&& ch.checkColumns(metadata, "OBJECT_REFS", "MODULE, OBJECT")
//					&& ch.checkColumns(metadata, "PROCS_LINK",
//							"PROC1, PROC2, OBJECT1, OBJECT2, MODULE1, MODULE2")
//					&& ch.checkColumns(metadata, "CONFIG",
//							"OBJECT, PROPERTY, VALUE")
//					&& ch.checkColumns(metadata, "CONFIG_INFO",
//							"VERSION")
					&& ch.checkColumns(metadata, "PROCS_PARAMETERS",
							"KEY, VALUE");
			
			
		} catch (Exception e) {
			throw new SQLException();
		} finally {
			con.close();
		}

		if (!haveStructure)
			throw new SQLException();

	}

	private boolean checkLisence(IDb db) throws FileNotFoundException, SQLException {
		
		IPath path = db.getDbPath();
		if (!path.isValidPath(path.toString()))
			throw new FileNotFoundException();

		Connection con = null;		
		try {
			con = db.getConnection(true);
			ResultSet rs;
			Statement stat = con.createStatement();
			rs = stat.executeQuery("Select COUNT(ID) from MODULES");
			try {
				int count = 0;
				if (rs.next())
					count = rs.getInt(1);

				return count<Const.DEFAULT_FREE_FILES_COUNT;
				
			} finally {
				rs.close();
			}	
		} catch (Exception e) {
			throw new SQLException();
		} finally {
			con.close();
		}
	}
	
	//loadFromDirectory ****************************************************************************************
	
	@Override
	public void loadFromDirectory(IDb db, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		
		//ПРОВЕРКИ ******************************************************
		
		File folder = db.getPath().toFile();
		if (!folder.exists())
			throw new InvocationTargetException(new LoadConfigException(),
					Const.ERROR_CONFIG_PATH);
		
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {

				String extension = Utils.getExtension(pathname);
				return extension.equalsIgnoreCase("txt");
						//|| extension.equalsIgnoreCase("meta");
			}
		});
		int length = files.length;
		if (length == 0)
			throw new InvocationTargetException(new LoadConfigException(),
					Const.ERROR_CONFIG_EMPTY);

		if (!sign.check())
		{
			if(files.length>Const.DEFAULT_FREE_FILES_COUNT)
			{		
				throw new InvocationTargetException(new LoadConfigException(),
						Const.ERROR_PRO_ACCESS_LOAD);
			}
		}
		
		//ЗАГРУЗКА ******************************************************
		
		Connection con = null;
		try {

			db.initDbPath();
			
			createSructure(db);
			con = db.getConnection(false);			
			
			monitor.beginTask("Загрузка конфигурации...", length);

			loadFromDirectoryDoWork(con, monitor, files);

//			monitor.beginTask(Const.MSG_CONFIG_FILL_LINK_TABLE,
//					cfg.getProcCount(con));

//			fillProcLinkTableDoWork(monitor, cfg, con);
			if (!sign.check()) 			
				if(!checkLisence(db))
					throw new InvocationTargetException(new InterruptedException(),
							Const.ERROR_PRO_ACCESS_LOAD);
			
			db.setState(DbState.Loaded);

			monitor.beginTask("Удаление файлов...", length);
			monitor.subTask("");
			for (File f : files) {				
				f.delete();
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
				monitor.worked(1);
			}
		} catch (InterruptedException e) {
			throw new InvocationTargetException(new InterruptedException(),
					Const.ERROR_CONFIG_INTERRUPT);
		} catch (Exception e) {
			throw new InvocationTargetException(e,
					e.getMessage());
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				throw new InvocationTargetException(e,
						Const.ERROR_CONFIG_CREATE_DATABASE);
			}
			monitor.done();
		}
		
	}
		
	private void loadFromDirectoryDoWork(Connection con, IProgressMonitor monitor, File[] files)
			throws Exception {				
		
//		createVersion(con);
		
		for (File f : files) {

			monitor.subTask(f.getName());

			if (!f.isDirectory()) {
				String extension = Utils.getExtension(f);

				if (extension.equalsIgnoreCase("txt")) {
					loadModuleFile(con, f);
				}
			}
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}
	}
	
	private void loadModuleFile(Connection con, File f)
			throws InterruptedException, InvocationTargetException {
		
		Entity proc = new Entity();
		
		BufferedReader bufferedReader = null;
		try {

			parser.parseObject(f, proc);

			Integer object = service.addObject(con, proc);
			Integer module = service.addModule(con, proc, object);

			service.deleteProcs(con, object, module);

			Reader in = new InputStreamReader(new FileInputStream(f), "UTF-8");
			bufferedReader = new BufferedReader(in);

			ArrayList<String> buffer = new ArrayList<String>();
			ArrayList<String> vars =new ArrayList<String>();
			
			Boolean procWasFound = false;
			String file_line = null;


			while ((file_line = bufferedReader.readLine()) != null) {
				
				buffer.add(file_line + "\n");
				
				if (parser.findProcEnd(file_line)) {
				
					parser.getProcInfo(proc, buffer, vars);
					if(!procWasFound && !vars.isEmpty())
					{
						Entity var = new Entity();
						var.proc_name = Const.STRING_VARS;
						var.proc_title = Const.STRING_VARS_TITLE;
						var.text = new StringBuilder();
						for (String string : vars) {
							var.text.append(string);
						}
						var.export = false;
						service.addProcedure(con, var, object, module);
						
						
					}
					
					proc.text = new StringBuilder();
					for (String string : buffer) {
						proc.text.append(string);
					}
					
					service.addProcedure(con, proc, object, module);
					
					buffer.clear();
					procWasFound = true;
				}
								
			}
			
			if (!buffer.isEmpty()) {
				proc.proc_name = Const.STRING_INIT;
				proc.proc_title = Const.STRING_INIT_TITLE;
				proc.text = new StringBuilder();
				for (String string : buffer) {
					proc.text.append(string);
				}
				proc.export = false;
				service.addProcedure(con, proc, object, module);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvocationTargetException(null,
					Const.ERROR_CONFIG_READFILE + f.getName());
		} finally {
			try {
				bufferedReader.close();
			} catch (Exception e) {
				throw new InvocationTargetException(null,
						Const.ERROR_CONFIG_READFILE + f.getName());
			}
		}
	}

	private void createSructure(IDb db) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		IPath path = db.getDbPath();
		if (!path.isValidPath(path.toString()))
			throw new IllegalAccessException();

		File f = path.toFile();

		File folder = f.getParentFile();
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {

				String extension = Utils.getExtension(pathname);
				return extension.equalsIgnoreCase("db");
			}
		});
		for (File _f : files) {
			_f.delete();
		}

		Connection con = db.getConnection(false);

		Statement stat = con.createStatement();

		// create table
		try {
			
//			stat.execute("CREATE TABLE CONFIG_INFO (ID INTEGER AUTO_INCREMENT, "
//					+ "VERSION VARCHAR(36));");
			
			stat.execute("CREATE TABLE OBJECTS (ID INTEGER AUTO_INCREMENT, "
					+ "GROUP1 VARCHAR(200), GROUP2 VARCHAR(200),  "
					+ "TITLE1 VARCHAR(200), TITLE2 VARCHAR(200),  "
					+ "TAG VARCHAR(200), "
					+ "PRIMARY KEY (ID));"
					+ "CREATE INDEX IDXTITLE1 ON OBJECTS(TITLE1);"
					+ "CREATE INDEX IDXTITLE2 ON OBJECTS(TITLE2);"
					+ "CREATE INDEX IDXGROUP1 ON OBJECTS(GROUP1);"
					+ "CREATE INDEX IDXGROUP2 ON OBJECTS(GROUP2)");

			stat.execute("CREATE TABLE MODULES (ID INTEGER AUTO_INCREMENT, "
					+ "OBJECT INTEGER, NAME VARCHAR(200), TITLE VARCHAR(200),  "
					+ "PRIMARY KEY (ID), "
					+ "FOREIGN KEY(OBJECT) REFERENCES OBJECTS(ID) ON UPDATE CASCADE ON DELETE CASCADE);"
					+ "CREATE INDEX MODULE_NAME ON MODULES(NAME);");

			stat.execute("CREATE TABLE PROCS (ID INTEGER AUTO_INCREMENT, "
					+ "OBJECT INTEGER, MODULE INTEGER, NAME VARCHAR(200), TITLE VARCHAR(500), EXPORT BOOL, "
					+ "PRIMARY KEY (ID), "
					+ "FOREIGN KEY(OBJECT) REFERENCES OBJECTS(ID) ON UPDATE CASCADE ON DELETE CASCADE, "
					+ "FOREIGN KEY(MODULE) REFERENCES MODULES(ID) ON UPDATE CASCADE ON DELETE CASCADE);"
					+ "CREATE INDEX PROC_NAME ON PROCS(NAME);"
					+ "CREATE INDEX PROC_OBJECT ON PROCS(OBJECT);"
					+ "CREATE INDEX PROC_MODULE ON PROCS(MODULE);"
					+ "CREATE INDEX PROC_EXPORT ON PROCS(EXPORT);"
					);

			stat.execute("CREATE TABLE PROCS_TEXT (ID INTEGER AUTO_INCREMENT, "
					+ "PROC INTEGER, TEXT CLOB, HASH VARCHAR(500), "
					+ "PRIMARY KEY (ID), "
					+ "FOREIGN KEY(PROC) REFERENCES PROCS(ID) ON UPDATE CASCADE ON DELETE CASCADE)");
			
			stat.execute("CREATE TABLE PROCS_PARAMETERS (ID INTEGER AUTO_INCREMENT, "
					+ "PROC INTEGER, KEY VARCHAR(200), VALUE VARCHAR(200),"
					+ "PRIMARY KEY (ID), "
					+ "FOREIGN KEY(PROC) REFERENCES PROCS(ID) ON UPDATE CASCADE ON DELETE CASCADE)");

			stat.execute("CREATE TABLE OBJECT_REFS (ID INTEGER AUTO_INCREMENT, "
					+ "MODULE INTEGER, OBJECT INTEGER, "
					+ "PRIMARY KEY (ID), "
					+ "FOREIGN KEY(OBJECT) REFERENCES OBJECTS(ID) ON UPDATE CASCADE ON DELETE CASCADE, "
					+ "FOREIGN KEY(MODULE) REFERENCES MODULES(ID) ON UPDATE CASCADE ON DELETE CASCADE);"
					+ "CREATE INDEX REF_OBJECT ON OBJECT_REFS(OBJECT);"
					+ "CREATE INDEX REF_MODULE ON OBJECT_REFS(MODULE);");
			
//			stat.execute("CREATE TABLE PROCS_LINK (ID INTEGER AUTO_INCREMENT, "
//					+ "PROC1 INTEGER, NAME1 VARCHAR(200), PROC2 INTEGER, NAME2 VARCHAR(200), "
//					+ "OBJECT1 INTEGER, OBJECT2 INTEGER, MODULE1 INTEGER, MODULE2 INTEGER, LINE INTEGER, "
//					+ "PRIMARY KEY (ID), "
//					+ "FOREIGN KEY(OBJECT1) REFERENCES OBJECTS(ID) ON UPDATE CASCADE ON DELETE CASCADE, "
//					+ "FOREIGN KEY(MODULE1) REFERENCES MODULES(ID) ON UPDATE CASCADE ON DELETE CASCADE,"
//					+ "FOREIGN KEY(PROC1) REFERENCES PROCS(ID) ON UPDATE CASCADE ON DELETE CASCADE,"
//					+ "FOREIGN KEY(OBJECT2) REFERENCES OBJECTS(ID) ON UPDATE CASCADE ON DELETE CASCADE, "
//					+ "FOREIGN KEY(MODULE2) REFERENCES MODULES(ID) ON UPDATE CASCADE ON DELETE CASCADE,"
//					+ "FOREIGN KEY(PROC2) REFERENCES PROCS(ID) ON UPDATE CASCADE ON DELETE CASCADE"
//					+ ")");
//
//			stat.execute("CREATE TABLE CONFIG (ID INTEGER AUTO_INCREMENT, "
//					+ "OBJECT INTEGER, "
//					+ "PROPERTY VARCHAR(200), "
//					+ "VALUE VARCHAR(200), "
//					+ "PRIMARY KEY (ID), "
//					+ "FOREIGN KEY(OBJECT) REFERENCES OBJECTS(ID) ON UPDATE CASCADE ON DELETE CASCADE)");
			
//			stat.execute("CREATE TABLE HISTORY (ID INTEGER AUTO_INCREMENT, "
//					+ "PROC INTEGER, "
//					+ "DATE SMALLDATETIME, "
//					+ "PRIMARY KEY (ID), "
//					+ "FOREIGN KEY(PROC) REFERENCES PROCS(ID) ON UPDATE CASCADE ON DELETE CASCADE)");
			
		} catch (Exception e) {
			throw new SQLException();
		} finally {
			con.close();
		}
	}

	//update ****************************************************************************************
	
	@Override
	public void update(IDb db, IProgressMonitor monitor) throws InvocationTargetException {

		// ПРОВЕРКИ ******************************************************

		File folder = db.getPath().toFile();
		if (!folder.exists())
			throw new InvocationTargetException(new LoadConfigException(),
					Const.ERROR_CONFIG_PATH);

		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {

				String extension = Utils.getExtension(pathname);
				return extension.equalsIgnoreCase("txt")
						|| extension.equalsIgnoreCase("meta");
			}
		});
		int length = files.length;
		if (length == 0)
			throw new InvocationTargetException(new LoadConfigException(),
					Const.ERROR_CONFIG_EMPTY);

		if (!sign.check()) {
			if (files.length > Const.DEFAULT_FREE_FILES_COUNT) {
				throw new InvocationTargetException(new LoadConfigException(),
						Const.ERROR_PRO_ACCESS_LOAD);
			}
		}

		// ЗАГРУЗКА ******************************************************

		Connection con = null;
		try {

			monitor.beginTask(Const.MSG_CONFIG_CHECK, 0);
			
			checkSructure(db);
			
			con = db.getConnection(true);

			monitor.beginTask("", length);

			loadFromDirectoryDoWork(con, monitor, files);

//			monitor.beginTask(Const.MSG_CONFIG_CLEAR_LINK_TABLE, 0);

//			service.deleteProcsLink(con);

			if (!sign.check()) 			
				if(!checkLisence(db))
					throw new InvocationTargetException(new InterruptedException(),
							Const.ERROR_PRO_ACCESS_LOAD);
			
			db.setState(DbState.Loaded);

		} catch (InterruptedException e) {
			throw new InvocationTargetException(new InterruptedException(),
					Const.ERROR_CONFIG_INTERRUPT);
		} catch (Exception e) {
			throw new InvocationTargetException(e, e.getMessage());
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				throw new InvocationTargetException(e,
						Const.ERROR_CONFIG_OPEN_DATABASE);
			}
			monitor.done();
		}

	}

	//loadFromSQL ****************************************************************************************
	
	@Override
	public void loadFromSQL(IDb db, IProgressMonitor monitor) throws InvocationTargetException {
		
		Connection con = null;
		Connection sql = null;
		try {

			db.initDbPath();
			
			createSructure(db);
			con = db.getConnection(false);			
			
			sql = db.getSQLConnection();
			//TODO !загрузка метаданных
			monitor.beginTask("Загрузка метаданных...", sql_service.getCount(sql));
			
			loadmetadata(con, sql, monitor);
			
			monitor.beginTask("Установка связей метаданных...", sql_service.getCount(sql));
			
			monitor.beginTask("Загрузка модулей...", sql_service.getCount(sql));

			if (!sign.check()) 			
				if(!checkLisence(db))
					throw new InvocationTargetException(new InterruptedException(),
							Const.ERROR_PRO_ACCESS_LOAD);
			
			db.setState(DbState.Loaded);

//			monitor.beginTask("Удаление файлов...", length);
//			monitor.subTask("");
//			for (File f : files) {				
//				f.delete();
//				if (monitor.isCanceled()) {
//					throw new InterruptedException();
//				}
//				monitor.worked(1);
//			}
//		} catch (InterruptedException e) {
//			throw new InvocationTargetException(new InterruptedException(),
//					Const.ERROR_CONFIG_INTERRUPT);
		} catch (Exception e) {
			throw new InvocationTargetException(e,
					e.getMessage());
		} finally {
			try {
				con.close();
				sql.close();
			} catch (Exception e) {
				throw new InvocationTargetException(e,
						Const.ERROR_CONFIG_CREATE_DATABASE);
			}
			monitor.done();
		}
	}

	private void loadmetadata(Connection con, Connection sql,
			IProgressMonitor monitor) throws InvocationTargetException {
		
		List<String> filenames = sql_service.getMetaNames(sql);
		
		for (String name : filenames) {
			ICData data = sql_service.getConfigData(sql, name);
			
			if(!data.needLoading())
				continue;
			
			
			Entity proc = new Entity();
			
			try {
				proc.group1 = data.getType().getCaption();
				proc.group2 = data.getName();
				service.addObject(con, proc);
				
			} catch (SQLException e) {

				e.printStackTrace();
				throw new InvocationTargetException(null,
						Const.ERROR_CONFIG_READOBJECT + name);
			}
			
			
			System.out.println(data.getType());
//			switch (data.getType()) {
//			case Catalog:
//				System.out.println("Справочик");
//				break;
//			case Enum:
//				System.out.println("Перечисление");
//				break;
//			default:
//				break;
//			}
			
		}			
	}

	
	
}

//private void createVersion(Connection con) throws SQLException {
//String SQL = "DELETE FROM CONFIG_INFO";
//Statement stat = con.createStatement();
//stat.execute(SQL);
//
//SQL = "INSERT INTO CONFIG_INFO (VERSION) VALUES (?)";
//PreparedStatement prep = con.prepareStatement(SQL,
//		Statement.RETURN_GENERATED_KEYS);
//prep.setString(1, UUID.randomUUID().toString());
//
//int affectedRows = prep.executeUpdate();
//if (affectedRows == 0)
//	throw new SQLException();
//
//}























