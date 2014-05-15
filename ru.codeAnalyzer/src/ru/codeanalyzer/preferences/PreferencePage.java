package ru.codeanalyzer.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.codeanalyzer.CodeAnalyserActivator;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Create the preference page.
	 */
	public PreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		IPreferenceStore store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
		setPreferenceStore(store);
	}

	/**
	 * Create contents of the preference page.
	 */
	@Override
	protected void createFieldEditors() {
		
		Composite top = getFieldEditorParent();
		StringFieldEditor login = new StringFieldEditor(PreferenceConstants.P_LOGIN, "Почта:", -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, top); 
		addField(login);
		StringFieldEditor password = new StringFieldEditor(PreferenceConstants.P_PASSWORD, "Пароль:", -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, top);
		password.getTextControl(top).setEchoChar('*');
		addField(password);
		StringFieldEditor ntp = new StringFieldEditor(PreferenceConstants.P_NTPSERVER, "ntp-сервер:", -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, top);
		ntp.setEmptyStringAllowed(false);
		addField(ntp);
		
		addField(new BooleanFieldEditor(PreferenceConstants.STANDART_PROCEDURE_USING, "Выделять цветом имена процедур, содержащие:", BooleanFieldEditor.DEFAULT, top));
		
		Composite group = new Composite(top, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Composite pv;
		Composite of;
		
		pv = new Composite(group, SWT.NONE);
		of = new Composite(group, SWT.NONE);
		pv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));		
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR1, "", of));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR2, "", of));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT1,"", pv));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT2,"", pv));
		
		pv = new Composite(group, SWT.NONE);
		of = new Composite(group, SWT.NONE);
		pv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR3, "", of));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR4, "", of));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT3,"", pv));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT4,"", pv));
		
		pv = new Composite(group, SWT.NONE);
		of = new Composite(group, SWT.NONE);
		pv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR5, "", of));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR6, "", of));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT5,"", pv));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT6,"", pv));
		
		pv = new Composite(group, SWT.NONE);
		of = new Composite(group, SWT.NONE);
		pv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR7, "", of));
		addField(new ColorFieldEditor(PreferenceConstants.STANDART_PROCEDURE_COLOR8, "", of));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT7,"", pv));
		addField(new StringFieldEditor(PreferenceConstants.STANDART_PROCEDURE_TEXT8,"", pv));
		
		addField(new BooleanFieldEditor(PreferenceConstants.DONOT_SHOW_EQUALENT_IN_COMPARE, "Не показывать одинаковые объекты при сравнении", BooleanFieldEditor.DEFAULT, top));
		addField(new BooleanFieldEditor(PreferenceConstants.OPEN_EDITOR_IN_BOTTOM_WINDOW, "Открывать модули под картой", BooleanFieldEditor.DEFAULT, top));
	}

	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}

}
