package ru.codeanalyzer.core.model;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.preference.IPreferenceStore;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.interfaces.IAuthorize;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.preferences.PreferenceConstants;
import ru.codeanalyzer.utils.Const;
import ru.codeanalyzer.utils.NtpMessage;
import ru.codeanalyzer.utils.jWMI;

/**
 * информация по ключу
 * @author Enikeev M.A.
 *
 */	
public class ActivationInfo {

	
	public ActivationInfo() {
		super();
		
		for (Field f : this.getClass().getDeclaredFields()) {				
			try {
				if (f.getType().isAssignableFrom(Boolean.class))
					f.set(this, false);
				else
					f.set(this, "");
			} catch (Exception e) {
			}
		}
	}

	private String check_message;
	
	public String message;
	
	public String serial;
	
	public String name;

	public String password;

	public Boolean withoutExpirationDate;
	
	public String ExpirationDate;
		
	public String ShortMessage(){
		StringBuilder result = new StringBuilder();
		
		if (check()){
			result.append(Const.MSG_PRO_SHORT);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				Date ExpirationDate = formatter.parse(this.ExpirationDate);				
				result.append(withoutExpirationDate ? "без ограничения" : " до " + new SimpleDateFormat("dd.MM.yyyy").format(ExpirationDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}		
		}else
		{
			result.append(Const.MSG_FREE_SHORT);
		}
			
		result.append(", " + pico.get(IAuthorize.class).checkUpdates());
		
		return result.toString();	
	}

	public String FullMessage(){
		StringBuilder result = new StringBuilder();
		
		if (check()){
			result.append(Const.MSG_PRO);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			
			try {
				Date ExpirationDate = formatter.parse(this.ExpirationDate);				
				result.append("Дата окончания: " + (withoutExpirationDate ? "без ограничения" : new SimpleDateFormat("dd.MM.yyyy").format(ExpirationDate)) + "\n");
			} catch (ParseException e) {
				e.printStackTrace();
			}			
			result.append("UUID: " + serial);            
		}else{
			result.append(Const.MSG_FREE);
			result.append(check_message);
		}
			
		return result.toString();	
	}
	
	public boolean check(){
		
		IPreferenceStore store = CodeAnalyserActivator.getDefault()
				.getPreferenceStore();
		String name = store.getString(PreferenceConstants.P_LOGIN);
		String password = store.getString(PreferenceConstants.P_PASSWORD);
		
		String serial;
		try {
			serial = getComputerSerial();
		} catch (Exception e1) {
			check_message = Const.MSG_GETID;
			return false;
		}
				
		if (!(this.name.equalsIgnoreCase(name)
				&& this.password.equalsIgnoreCase(password)
				&& this.serial.equalsIgnoreCase(serial)
				))
		{
			check_message = Const.MSG_INCORRECT_SERIAL;
			return false;
		}

//		if (!this.withoutExpirationDate) {

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

			try {
				Date ExpirationDate = formatter.parse(this.ExpirationDate);
				Date currentDate = NtpMessage.getDate();
				if (!ExpirationDate.after(currentDate)){
					check_message = Const.MSG_EXPIRED;
					return false;
				}
			} catch (Exception e) {
				check_message = Const.MSG_NTP;
				return false;
			}
//		}
		
		return true;
		
	}
	
	/**
	 * UUID компьютера
	 * @return
	 * @throws Exception
	 */
	public static String getComputerSerial() throws Exception {
		return jWMI.getWMIValue("SELECT UUID FROM Win32_ComputerSystemProduct",
				"UUID");
	}
	
	public void fill(Request msg){			
		for (Field f : this.getClass().getDeclaredFields()) {
			for (Field f1 : msg.getClass().getDeclaredFields()) {
				if (f.getName().equalsIgnoreCase(f1.getName())) {
					try {
						f.set(this, f1.get(msg));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}