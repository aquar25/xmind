package ru.codeanalyzer.exceptions;

import ru.codeanalyzer.utils.Const;

public class SiteAccessException extends Exception {

	public String message = Const.ERROR_SITE_ACCESS;
	
	private static final long serialVersionUID = -6766112056705569937L;

}
