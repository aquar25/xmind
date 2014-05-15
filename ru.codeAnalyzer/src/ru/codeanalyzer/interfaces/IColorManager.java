package ru.codeanalyzer.interfaces;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public interface IColorManager {

//	http://www.colorschemer.com/online.html
	public enum TOKENS{
		KEYWORD, STRING, KEYWORD_IN_STRING, COMMENT, DIRECTIVE, OTHER, QUERY_PARAMETR, KEYWORD_IN_STRING1,
		COMPARE, COMPARE_ADDED, COMPARE_REMOVED, COMPARE_CHANGED
	}

	 
	//TODO отрегулировать цвета стандартных процедур
	public final RGB STANDART_PROCEDURE_1 = new RGB(153, 51, 0);
	public final RGB STANDART_PROCEDURE_2 = new RGB(255, 153, 0);
	public final RGB STANDART_PROCEDURE_3 = new RGB(255, 102, 0);
	public final RGB STANDART_PROCEDURE_4 = new RGB(255, 26, 0);
	public final RGB STANDART_PROCEDURE_5 = new RGB(122, 175, 255);
	public final RGB STANDART_PROCEDURE_6 = new RGB(0, 102, 0);
	public final RGB STANDART_PROCEDURE_7 = new RGB(51, 153, 255);
	public final RGB STANDART_PROCEDURE_8 = new RGB(230, 172, 51);


	public final RGB DIRECTIVE = new RGB(153, 76, 0);
	public final RGB COMMENT = new RGB(0, 102, 0);
	public final RGB KEYWORD = new RGB(255, 0, 0);
	public final RGB TYPE = new RGB(0, 0, 128);
	public final RGB STRING = new RGB(0, 0, 0);
	public final RGB DEFAULT = new RGB(0, 0, 153);
	
	public final RGB KEYWORD_IN_STRING = new RGB(99, 99, 99);
	public final RGB KEYWORD_IN_STRING1 = new RGB(153, 77, 0);
	
	public final RGB TEXT_SELECTION = new RGB(255, 204, 0);
	
	public final RGB COMPARE_ADDED = new RGB(145, 145, 145);
	public final RGB COMPARE_CHANGED = new RGB(214, 107, 10);
	

	public abstract void dispose();

	public abstract Color getColor(RGB rgb);
	
	public String toString(RGB rgb);

	String toTopicString(RGB rgb);

	RGB toRGB(String text);

	RGB getStandartProcedureColor(String proc_name);
	
	IToken getToken(TOKENS type);

}