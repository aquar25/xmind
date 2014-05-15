package ru.codeanalyzer.exceptions;

import ru.codeanalyzer.utils.Const;

public class LiscenseException extends Exception {
	
	private static final long serialVersionUID = 3044026216467123492L;
	
	public String message = Const.ERROR_PRO_ACCESS_LOAD;
}
