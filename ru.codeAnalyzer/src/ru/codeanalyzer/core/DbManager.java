package ru.codeanalyzer.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import ru.codeanalyzer.interfaces.IDb;
import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.ILoaderService;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IDb.DbState;
import ru.codeanalyzer.interfaces.ILoaderService.operationType;
import ru.codeanalyzer.preferences.PreferenceConstants;
import ru.codeanalyzer.utils.Const;


public class DbManager implements IDbManager{

	ILoaderService loaderService = pico.get(ILoaderService.class);
	HashMap<String,IDb> bases = new HashMap<String,IDb>();
	
	IDb active;
	
	HashMap <operationType, String> operationNames = new HashMap <operationType, String>();
	
	public void init() {
		
		operationNames.put(operationType.fromDirectory, "Загрузить из каталога");
		operationNames.put(operationType.update, "Обновить из каталога");
		operationNames.put(operationType.fromDb, "Подключить ранее загруженную конфигурацию");
		operationNames.put(operationType.fromSQL, "Загрузить через SQL-соединение");
//		operationNames.put(operationType.fillProcLinkTable, "Сформировать таблицу взаимных вызовов");
		
		IDb db1 = pico.get(IDb.class);
		db1.load(PreferenceConstants.DB1_STATE, 1, Const.DB1);
		if(db1.getType()==operationType.fromDb)
			execute(db1);
		bases.put(Const.DB1, db1);
		
		IDb db2 = pico.get(IDb.class);
		db2.load(PreferenceConstants.DB2_STATE, 2, Const.DB2);
		if(db2.getType()==operationType.fromDb)
			execute(db2);
		bases.put(Const.DB2, db2);	
		
		active = db1;
	}
		
	@Override
	public IDb getActive() {
		return active;
	}

	@Override
	public IDb getNonActive() {
		for (IDb db : bases.values()) {
			if(db!=active)
				return db;
		}
		return null;
	}
	
	@Override
	public void setActive(String ID) {
		active = bases.get(ID);
	}

	@Override
	public IDb get(String ID) {
		return bases.get(ID);
	}

	@Override
	public String getOperationName(operationType key) {
		String name = operationNames.get(key); 
		return name == null ? "DBManager.getOperationName" : name;
	}
	
	public void execute(final IDb db) {
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
								
				switch (db.getType()) {
				case fromDirectory:
					loaderService.loadFromDirectory(db, monitor);
					break;
				case fromDb:
					loaderService.loadFromDb(db, monitor);
					break;
//				case fillProcLinkTable:
//					loaderService.fillProcLinkTable(db, monitor);
//					break;	
				case update:
					loaderService.update(db, monitor);
					break;
				case fromSQL:
					loaderService.loadFromSQL(db, monitor);
					break;
				default:
					throw new InterruptedException();
				}	
			}
		};
		
		try {
			
			PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
			
		} catch (InvocationTargetException e) {
			
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Ошибка загрузки конфигурации", e.getMessage());
			
			db.setState(DbState.notLoaded);
			
		} catch (Exception e) {	
			
			db.setState(DbState.notLoaded);
		}
	}
	
}
