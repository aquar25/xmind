package ru.codeanalyzer.editor.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.preferences.PreferenceConstants;

public class ColorManager implements IColorManager {
	protected Map<RGB, Color> fColorTable = new HashMap<RGB,Color>(10);
	
	public ColorManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see ru.codeanalyzer.editor.core.IColorManager#dispose()
	 */
	@Override
	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext())
			((Color) e.next()).dispose();
	}

	/* (non-Javadoc)
	 * @see ru.codeanalyzer.editor.core.IColorManager#getColor(org.eclipse.swt.graphics.RGB)
	 */
	@Override
	public Color getColor(RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
	
	@Override
    public String toString(RGB rgb) {
        if (rgb == null)
            return null;

        return rgb.red + ", " + rgb.green + ", " + rgb.blue;
    }
	
	@Override
    public RGB toRGB(String text) {
        if (text == null)
            return null;

        String[] t = text.split(",");
        return new RGB(
        		Integer.parseInt(t[0].trim()),
        		Integer.parseInt(t[1].trim()),
        		Integer.parseInt(t[2].trim()));
    }
	
	@Override
    public String toTopicString(RGB rgb) {
        if (rgb == null)
            return null;

        return String.format("#%06X", merge(rgb)); //$NON-NLS-1$
    }
	
	@Override
	public RGB getStandartProcedureColor(String proc_name) {

		IColorManager color = pico.get(IColorManager.class);
		IPreferenceStore store = CodeAnalyserActivator.getDefault().getPreferenceStore(); 
		
		if(!store.getBoolean(PreferenceConstants.STANDART_PROCEDURE_USING))
			return null;
		
		proc_name = proc_name.toUpperCase().replace(" ", "");
		
		String temp;
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT1);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR1));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT2);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR2));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT3);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR3));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT4);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR4));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT5);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR5));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT6);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR6));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT7);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR7));
		}
		
		temp = store.getString(PreferenceConstants.STANDART_PROCEDURE_TEXT8);
		for (String s : temp.split(",")) {
			s = s.toUpperCase().trim().replace(" ", "");
			if (proc_name.contains(s))
				return color.toRGB(store.getString(PreferenceConstants.STANDART_PROCEDURE_COLOR8));
		}
		
		return null;
	}
		
    private int merge(RGB rgb) {
        return merge(rgb.red, rgb.green, rgb.blue);
    }
    
    private int merge(int r, int g, int b) {
        return ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

	@Override
	public IToken getToken(TOKENS type) {
		
		switch (type) {
		case KEYWORD:
			return new Token(new TextAttribute(getColor(IColorManager.KEYWORD)));
		case COMMENT:
			return new Token(new TextAttribute(getColor(IColorManager.COMMENT)));		
		case DIRECTIVE:
			return new Token(new TextAttribute(
					getColor(IColorManager.DIRECTIVE)));
		case KEYWORD_IN_STRING:
			return new Token(new TextAttribute(
					getColor(IColorManager.KEYWORD_IN_STRING)));
		case OTHER:
			return new Token(new TextAttribute(getColor(IColorManager.DEFAULT)));
			
		case STRING:
			return new Token(new TextAttribute(getColor(IColorManager.STRING)));
		case QUERY_PARAMETR:
			return new Token(new TextAttribute(
					getColor(IColorManager.KEYWORD_IN_STRING)));
		case KEYWORD_IN_STRING1:
			return new Token(new TextAttribute(
					getColor(IColorManager.KEYWORD_IN_STRING1)));
		//COMPARE *************************************************************************	
		case COMPARE:
			return new Token(new TextAttribute(getColor(IColorManager.DEFAULT),
					getColor(IColorManager.TEXT_SELECTION), SWT.NORMAL));
		case COMPARE_ADDED:
			return new Token(new TextAttribute(getColor(IColorManager.COMPARE_ADDED)));
		case COMPARE_CHANGED:
			return new Token(new TextAttribute(getColor(IColorManager.COMPARE_CHANGED)));
//					getColor(IColorManager.COMPARE_SELECTION), SWT.NORMAL));
		case COMPARE_REMOVED:
			return new Token(new TextAttribute(getColor(IColorManager.COMPARE_CHANGED)));
		
		//*************************************************************************	
		default:
			return Token.UNDEFINED;
		}
	}
}
