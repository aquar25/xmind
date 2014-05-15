package ru.codeanalyzer.core.model;

import java.lang.reflect.Field;

/**
 * класс сообщения для выполнения запросов к сайту
 * @author Enikeev M.A.
 *
 */
public class Request {

	public String name = "";

	public String password = "";

	public String message = "";

	public String serial = "";

	public Boolean crypt_error = false;

	public Boolean accept = false;
	
	public Boolean pro = false;
	
	public Boolean activated = false;
	
	public Boolean withoutExpirationDate = false;
	
	public String ExpirationDate = "";
	
	public String InitialDate = "";
	
	public String version = "";

	public void clear() {

		try {
			for (Field f : getClass().getDeclaredFields()) {

				if (f.getName().equalsIgnoreCase("name")
						|| f.getName().equalsIgnoreCase("password")
						|| f.getName().equalsIgnoreCase("serial")
						|| f.getName().equalsIgnoreCase("ExpirationDate")
						|| f.getName().equalsIgnoreCase("withoutExpirationDate")						
						|| f.getName().equalsIgnoreCase("InitialDate")) {
					continue;
				}
				if (f.getType().isAssignableFrom(Boolean.class))
					f.set(this, false);
				else
					f.set(this, "");

			}
		} catch (Exception e) {
		}
	}

}
