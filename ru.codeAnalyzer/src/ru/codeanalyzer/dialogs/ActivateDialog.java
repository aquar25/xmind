package ru.codeanalyzer.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.core.model.ActivationInfo;
import ru.codeanalyzer.interfaces.IAuthorize;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.preferences.PreferenceConstants;
import ru.codeanalyzer.utils.Const;
import ru.codeanalyzer.utils.Utils;

public class ActivateDialog extends Dialog {
	private Text loginField;
	private Text passwordField;

	IPreferenceStore store;
	private Text serialField;
	private Text statusField;
	private Text ntpField;
		
	protected void setValues()
	{
		store.setValue(PreferenceConstants.P_LOGIN, loginField.getText());
		store.setValue(PreferenceConstants.P_PASSWORD, passwordField.getText());
		store.setValue(PreferenceConstants.P_SERIAL, serialField.getText());
		
	}
	
	protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Анализатор кода 1С");
	   }
	
	@Override
	public boolean close() {
		setValues();
		return super.close();
	}

	protected void initContents()
	{
		loginField.setText(store.getString(PreferenceConstants.P_LOGIN));
		passwordField.setText(store.getString(PreferenceConstants.P_PASSWORD));
		ntpField.setText(store.getString(PreferenceConstants.P_NTPSERVER));
		serialField.setText(store.getString(PreferenceConstants.P_SERIAL));

		ActivationInfo info = pico.get(IAuthorize.class).getInfo();
		statusField.setText(info.FullMessage());
		
	}
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ActivateDialog(Shell parentShell) {
		super(parentShell);		
		setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
		store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID,
				"Закрыть", true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(673, 464);
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
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//************* ПОЧТА ************************
		
		label = new Label(container, SWT.LEFT);
		label.setText("Почта:");
		
		loginField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		loginField.setLayoutData(gridData);

	    //************** ПАРОЛЬ ***********************
		
		label = new Label(container, SWT.LEFT);
		label.setText("Пароль:");
		
		passwordField = new Text(container, SWT.SINGLE | SWT.BORDER);
		passwordField.setEchoChar('*');
		gridData = new GridData();		
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		passwordField.setLayoutData(gridData);

	    //************* NTP-server ************************
		
		label = new Label(container, SWT.LEFT);
		label.setText("NTP-сервер:");
		
		ntpField = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		ntpField.setLayoutData(gridData);
		ntpField.setEditable(false);

	    //************** СЕРИЙНИК ***********************
		
		label = new Label(container, SWT.LEFT);
		label.setText("Серийный номер:");
		gridData = new GridData();
	    gridData.horizontalSpan = 3;
	    label.setLayoutData(gridData);
	    
	    serialField = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    GridData gridData_1 = new GridData();
		gridData_1.heightHint = 100;
		gridData_1.horizontalAlignment = GridData.FILL;
		gridData_1.grabExcessHorizontalSpace = true;
		gridData_1.horizontalSpan = 2;
		serialField.setLayoutData(gridData_1);
		
	    //**************** СТАТУС *********************
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		button = new Button(composite, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnCompUUIDOnClick();
			}
		});
		button.setText("UUID");
		
		button = new Button(composite, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnActivateOnClick();
			}
		});
		button.setFont(SWTResourceManager.getFont("Tahoma", 8, SWT.BOLD));
		button.setText("Активировать");
		
		button = new Button(composite, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnCheckOnClick();	
			}
		});
		button.setText("Проверить");
		
		button = new Button(composite, SWT.FLAT);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnRegisterOnClick();
			}
		});
		button.setText("Продлить");
		
		statusField = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
//		statusField.setTopMargin(10);
//		statusField.setBottomMargin(10);
//		statusField.setRightMargin(10);
//		statusField.setLeftMargin(10);
//		statusField.setAlignment(SWT.LEFT);
		statusField.setFont(SWTResourceManager.getFont("Tahoma", 10, SWT.BOLD));
		statusField.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND));
		statusField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		statusField.setEditable(false);
		
		initContents();
		
		return area;
	}

	protected void btnCompUUIDOnClick() {
		try {
			statusField.setText(ActivationInfo.getComputerSerial());
		} catch (Exception e) {
			statusField.setText(Const.MSG_GETID);
		}
	}

	protected void btnRegisterOnClick() {
		close();
		Utils.OpenLink(Const.URL_proLinkOpen);		
	}

	protected void btnCheckOnClick() {
		
		setValues();
		ActivationInfo info = pico.get(IAuthorize.class).getInfo();
		statusField.setText(info.FullMessage());
			
	}

	protected void btnActivateOnClick() {
		Shell shell = getShell();
		ActivationInfo info;
		final String login = loginField.getText();
		final String password = passwordField.getText();	
		
		Cursor cursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT); 
		shell.setCursor(cursor); 
		
		try {
			info = pico.get(IAuthorize.class).Activate(login, password);
			statusField.setText(info.message);
			if(!info.serial.isEmpty())
				{
				serialField.setText(info.serial);
				setValues();
				}					
		} catch (Exception e1) {
			e1.printStackTrace();
			statusField.setText(e1.getMessage());
		}
		
		shell.setCursor(null); 
		cursor.dispose(); 
		
	}
}
