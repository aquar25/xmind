package ru.codeanalyzer.dialogs;

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.core.model.DbInfo.SQLConnection;
import ru.codeanalyzer.interfaces.IDb;
import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.ILoaderService.operationType;
import ru.codeanalyzer.utils.Utils;

public class EditDialog extends Dialog {

	IDbManager dbManager = pico.get(IDbManager.class);
	
	private Text nameField;
	private Text pathField;
	private Text db_pathField;
	private Text sql_pathField;
	private Text sql_userField;
	private Text sql_passwordField;
	
	IPreferenceStore store;
	private IDb db;
	
	HashMap<operationType, Button> radioBtns = new HashMap<operationType, Button>(); 
	
	
	
	protected void setValues()
	{
		db.setName(nameField.getText());
		db.setPath(pathField.getText());
		db.setDbPath(db_pathField.getText());
		db.setSQL(sql_pathField.getText(), sql_userField.getText(), sql_passwordField.getText());
		db.save();	
	}
	
	@Override
	protected void okPressed() {

		setValues();

		dbManager.execute(db);

		initContents();

		super.okPressed();
	}

	protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(db.getName());
	   }
	
	@Override
	public boolean close() {
		setValues();
		return super.close();
	}

	protected void initContents()
	{
		nameField.setText(db.getName());
		pathField.setText(db.getPath().toString());
		db_pathField.setText(db.getDbPath().toString());
		SQLConnection sql = db.getSQL();
		sql_pathField.setText(sql.path);
		sql_userField.setText(sql.user);
		sql_passwordField.setText(sql.password);
		
		radioBtns.get(db.getType()).setSelection(true);
		
	}
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public EditDialog(Shell parentShell, IDb db) {
		super(parentShell);		
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
		store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
		this.db = db;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				"Выполнить", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"Закрыть", true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(487, 343);
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setToolTipText("");
		
		GridData gridData;
		Label label;
		Button button;
		Group group;
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//************* Имя ************************
		
		label = new Label(container, SWT.RIGHT);
		label.setAlignment(SWT.RIGHT);
		label.setText("Имя конфигурации:");
		
		nameField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		nameField.setLayoutData(gridData);

	    //************** КАТАЛОГ ДЛЯ ЗАГРУЗКИ ***********************
		
		label = new Label(container, SWT.LEFT);
		label.setText("Каталог для загрузки:");
		
		pathField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();		
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		pathField.setLayoutData(gridData);
		
		button = new Button(container, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseForPath();
			}
		});
		button.setText("...");

		//************** ФАЙЛ БАЗЫ ДАННЫХ ***********************
		
		label = new Label(container, SWT.LEFT);
		label.setText("Файл базы данных:");
				
		db_pathField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();		
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		db_pathField.setLayoutData(gridData);

		button = new Button(container, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseForFile();
			}
		});
		button.setText("...");

		//************** ПУТЬ К SQL ***********************
		
		label = new Label(container, SWT.LEFT);
		label.setText("Строка SQL-соединения:");

		sql_pathField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		sql_pathField.setLayoutData(gridData);
		
		label = new Label(container, SWT.LEFT);
		label.setText("Логин, пароль к базе MS SQL:");
				
		sql_userField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();		
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		sql_userField.setLayoutData(gridData);
		
		sql_passwordField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();		
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		sql_passwordField.setLayoutData(gridData);
		sql_passwordField.setEchoChar('*');
		
		button = new Button(container, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDefaultSQL();
			}
		});
		button.setText("<");
		
		
		//************** ОПЕРАЦИИ ДЛЯ ВЫПОЛНЕНИЯ ***********************
		
		group = new Group(container, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		for (final operationType key : operationType.values()) {
			button = new Button(group, SWT.RADIO);	
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					db.setType(key);
				}
			});
			button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			button.setText(pico.get(IDbManager.class).getOperationName(key));
			radioBtns.put(key, button);
		} 
				 		
		initContents();
		
		return area;
	}

	protected void setDefaultSQL() {
		SQLConnection sql = db.getDefaultSQL();
		sql_pathField.setText(sql.path);
		sql_userField.setText(sql.user);
		sql_passwordField.setText(sql.password);		
	}

	protected void browseForFile() {
		Utils.browseForFile(db_pathField, getShell());
	}

	protected void browseForPath() {
		Utils.browseForPath(pathField, getShell());				
	}
}
