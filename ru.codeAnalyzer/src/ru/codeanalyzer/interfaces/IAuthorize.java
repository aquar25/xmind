package ru.codeanalyzer.interfaces;

import ru.codeanalyzer.core.model.ActivationInfo;

public interface IAuthorize {

		
	/**
	 * получение ключа через запрос к сайту
	 * @param name
	 * @param password
	 * @return - информация о активации
	 */
	public ActivationInfo Activate(String name, String password);

	/**
	 * проверка доступности про
	 * @param name
	 * @param password
	 * @return 
	 */
	public boolean check();
	
	/**
	 * проверка доступности про
	 * @param name
	 * @param password
	 * @return 
	 */
	public ActivationInfo getInfo();
	
	/**
	 * проверка обновлений
	 * @return строка сообщения о наличии обновлений
	 */
	public String checkUpdates();
		
}