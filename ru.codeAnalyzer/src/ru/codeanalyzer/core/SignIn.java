package ru.codeanalyzer.core;

import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.core.model.ActivationInfo;
import ru.codeanalyzer.core.model.Request;
import ru.codeanalyzer.exceptions.CryptException;
import ru.codeanalyzer.exceptions.RequestParseException;
import ru.codeanalyzer.exceptions.SiteAccessException;
import ru.codeanalyzer.exceptions.SiteCryptException;
import ru.codeanalyzer.interfaces.IAuthorize;
import ru.codeanalyzer.preferences.PreferenceConstants;
import ru.codeanalyzer.utils.AesCrypt;
import ru.codeanalyzer.utils.Const;

public class SignIn implements IAuthorize {

	@Override
	public ActivationInfo Activate(String name, String password) {
		
		final class Error
		{
			public Error() {
				super();
				items = new ArrayList<item>();
			}
			final class item{
				boolean condition;
				String message;
				public item(boolean condition, String message) {
					this.condition = condition;
					this.message = message;		
				}								
			}
			ArrayList<item> items;	
			
			public void add(boolean condition, String message)
			{
				items.add(new item(condition, message));				
			}		
			public boolean check(ActivationInfo info) {
				
				for (item element : items) {
					if(element.condition)
					{
						info.message = element.message;
						return true;
					}				
				}
				return false;
			}
		}
		
		ActivationInfo info = new ActivationInfo();

		Request msg = new Request();
		msg.name = name;
		msg.password = password;

		try {
			msg.serial = ActivationInfo.getComputerSerial();
		} catch (Exception e1) {
			info.message = Const.MSG_ACTIVATE_FAIL + Const.MSG_GETID; 
			return info;
		}

		// ******************************************

		siteResponce res = askSite(msg, Const.URL_ACTIVATE);
		
		 
		Error er = new Error();
		er.add(!res.error.isEmpty(), 		Const.MSG_ACTIVATE_FAIL + Const.MSG_SEND_EMAIL_TO);
		er.add(!res.response.accept, 		Const.MSG_ACTIVATE_FAIL + Const.MSG_LOGIN);
		er.add(!res.response.pro, 			Const.MSG_ACTIVATE_FAIL + res.response.message);
		er.add(!res.response.activated, 	Const.MSG_ACTIVATE_FAIL + res.response.message);
		
		if (er.check(info))
			return info;
		
		// ******************************************
		String siteMessage = res.response.message;
		String activationString = "";
		try {
			res.response.clear();
			activationString = getMessageString(res.response);
			AesCrypt crypt = new AesCrypt();
			info.serial = crypt.toString(crypt.Encrypt(activationString));
		} catch (Exception e) {
			info.message = Const.MSG_ACTIVATE_FAIL + e.getMessage();
			return info;
		}
		
		info.message =  Const.MSG_ACTIVATE_OK + siteMessage;
		
		return info;
	}

	@Override
	public boolean check(){
		
		ActivationInfo info = getInfo();
		
		return info.check();				
		
	}
	
	@Override
	public String checkUpdates() {
	
		IPreferenceStore store = CodeAnalyserActivator.getDefault()
				.getPreferenceStore();
		
		Request msg = new Request();
	
		msg.name = store.getString(PreferenceConstants.P_LOGIN);
		// DONE сообщение о доступности новой версии при загрузке
		// FUTURE список изменений в ответе из файла в Content/plugin/version.txt

		String result = "версия от " + Const.GetVersion();
		msg.version = Const.GetVersion();
		// ******************************************

		siteResponce res = askSite(msg, Const.URL_CHECK_UPDATE);
		if (!res.error.isEmpty())
			return result;
		
		return res.response.message.isEmpty() ? result : res.response.message;
	}
	
	@Override
	public ActivationInfo getInfo() {
		
		ActivationInfo info = new ActivationInfo();
		
		IPreferenceStore store = CodeAnalyserActivator.getDefault()
				.getPreferenceStore();
		String activationString = store.getString(PreferenceConstants.P_SERIAL);
		if (activationString.isEmpty())
			info.message = Const.MSG_EMPTY_SERIAL;

		if (!info.message.isEmpty()) return info;
		
		// ******************************************

		try {
			AesCrypt crypt = new AesCrypt();
			Request msg = getMessageFromString(crypt.Decrypt(crypt.toByteArray(activationString)));
			info.fill(msg);
		} catch (Exception e) {
			info.message = Const.MSG_INCORRECT_SERIAL;
		}
		// ******************************************

		return info;
	}
	
	//****************************************************************************
	
	/**
	 * класс результата запроса к сайту String error, Request response
	 * @author Enikeev M.A.
	 *
	 */
	private class siteResponce {
		public String error = "";
		public Request response;
	}
	
	/**
	 * посылает сообщение сайту и обрабатывает ошибки
	 * @param msg
	 * @param url
	 * @return
	 */
	private siteResponce askSite(Request msg, String url) {

		siteResponce responce = new siteResponce();
		responce.response = new Request();
		try {
			responce.response = sendRequest(msg, url);
		} catch (MalformedURLException e2) {
			responce.error = Const.ERROR_NO_ADRESS;
		} catch (CryptException e) {
			responce.error = e.message;
		} catch (SiteAccessException e) {
			responce.error = e.message;
		} catch (SiteCryptException e) {
			responce.error = e.message;
		} catch (RequestParseException e) {
			responce.error = e.message;
		}
		return responce;
	}

	/**
	 * из строки вида "[свойство]=[значение]&" получает класс сообщения 
	 * @param decript - исходная строка
	 * @return класс сообщения Request
	 * @throws RequestParseException
	 */
	private Request getMessageFromString(String decript) throws RequestParseException {

		Request result = new Request();

		String msg = decript.substring(AesCrypt.CRYPT_PREFIX.length());
		String[] lines = msg.split("&");
		if (lines.length == 0)
			throw new RequestParseException();

		for (Field f : result.getClass().getDeclaredFields()) {
			for (String line : lines) {
				String[] v = line.split("=");
				if (v.length != 2)
					continue;
				if (f.getName().equalsIgnoreCase(v[0])) {
					try {
						if (f.getType().isAssignableFrom(Boolean.class))
							f.set(result, Boolean.parseBoolean(v[1]));
						else
							f.set(result, v[1]);
					} catch (Exception e) {
						throw new RequestParseException();
					}
					break;
				}
			}
		}

		return result;
	}

	/**
	 * формирует строку из класса сообщения
	 * @param msg - класс сообщения Request 
	 * @return строку из свойств сообщения, разделенных &
	 * @throws RequestParseException
	 */
	private String getMessageString(Request msg) throws RequestParseException {

		String result = AesCrypt.CRYPT_PREFIX;

		try {
			for (Field f : msg.getClass().getDeclaredFields()) {
				result += f.getName() + '=' + f.get(msg).toString() + '&';
			}
			result = result.substring(0, result.length() - 1);
		} catch (Exception e) {

			throw new RequestParseException();
		}

		return result;
	}
	
	/**
	 * посылает сообщение сайту
	 * @param msg - посылаемое сообщение
	 * @param _url - url сайта
	 * @return
	 * @throws CryptException
	 * @throws MalformedURLException
	 * @throws SiteAccessException
	 * @throws SiteCryptException
	 * @throws RequestParseException
	 */
	private Request sendRequest(Request msg, String _url) throws CryptException,
			MalformedURLException, SiteAccessException, SiteCryptException,
			RequestParseException {

		String body = getMessageString(msg);

		AesCrypt crypt = new AesCrypt();
		byte[] urlData = crypt.Encrypt(body);

		URL url = new URL(_url);

		HttpURLConnection connection;
		DataOutputStream wr;
		try {
			connection = (HttpURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(urlData.length));
			connection.setUseCaches(false);

			wr = new DataOutputStream(connection.getOutputStream());
			wr.write(urlData);
			wr.flush();
			wr.close();

			byte[] cipheredBytes = crypt.toByteArray(connection
					.getInputStream());

			String responceString;
			try {
				responceString = crypt.Decrypt(cipheredBytes);
			} catch (Exception e) {
				throw new CryptException();
			}

			connection.disconnect();

			Request responce = getMessageFromString(responceString);
			if (responce.crypt_error)
				throw new SiteCryptException();

			return responce;

		} catch (SiteCryptException e) {
			throw new SiteCryptException();
		} catch (CryptException e) {
			throw new CryptException();
		} catch (RequestParseException e) {
			throw new RequestParseException();
		} catch (Exception e) {
			throw new SiteAccessException();
		}
	}


}
