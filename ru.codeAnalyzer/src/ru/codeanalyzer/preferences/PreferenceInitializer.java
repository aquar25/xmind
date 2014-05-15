package ru.codeanalyzer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.pico;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	IColorManager color = pico.get(IColorManager.class);
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CodeAnalyserActivator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_LOGIN, "");
		store.setDefault(PreferenceConstants.P_PASSWORD, "");
		store.setDefault(PreferenceConstants.P_SERIAL, "");
		store.setDefault(PreferenceConstants.P_NTPSERVER, "ptbtime1.ptb.de");	
		
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_USING, true);
		
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT1, "ПередОткрытием, ПередЗаписью, ПередЗаписьюНаСервере");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT2, "ПриОткрытии, ПриЗаписи, ПриЗакрытии, ПриСозданииНаСервере");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT3, "ПослеЗаписи");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT4, "ОбработкаПроведения, ОбработкаУдаленияПроведения");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT5, "ОбработкаВыбора, ОбработкаОповещения, ОбновлениеОтображения, ВнешнееСобытие");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT6, "раздел описания переменных, раздел инициации");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT7, "ПриИзменении");
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_TEXT8, "Печат");

		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR1, color.toString(IColorManager.STANDART_PROCEDURE_1));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR2, color.toString(IColorManager.STANDART_PROCEDURE_2));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR3, color.toString(IColorManager.STANDART_PROCEDURE_3));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR4, color.toString(IColorManager.STANDART_PROCEDURE_4));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR5, color.toString(IColorManager.STANDART_PROCEDURE_5));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR6, color.toString(IColorManager.STANDART_PROCEDURE_6));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR7, color.toString(IColorManager.STANDART_PROCEDURE_7));
		store.setDefault(PreferenceConstants.STANDART_PROCEDURE_COLOR8, color.toString(IColorManager.STANDART_PROCEDURE_8));
		
		store.setDefault(PreferenceConstants.DONOT_SHOW_EQUALENT_IN_COMPARE, true);
		store.setDefault(PreferenceConstants.OPEN_EDITOR_IN_BOTTOM_WINDOW, false);
		
	}

}
