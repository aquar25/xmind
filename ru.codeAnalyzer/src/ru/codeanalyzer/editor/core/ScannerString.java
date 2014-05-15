package ru.codeanalyzer.editor.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WordRule;

import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IColorManager.TOKENS;
import ru.codeanalyzer.utils.Utils;

public class ScannerString extends RuleBasedScanner {

	IColorManager provider = pico.get(IColorManager.class);
	
	//TODO äîðàáîòàòü ñêàíåð òåêñòà - ðàñöâåòêà è êëþ÷åâûå ñëîâà
	
	private String[] fgKeywords = { "ÂÛÁÐÀÒÜ", "ÈÇ", "ÃÄÅ", "ëåâîå", "ïðàâîå", "âíóòðåííåå", "ïîëíîå", 
		"ñîåäèíåíèå", "ñãðóïïèðîâàòü", "èòîãè", "ïî", "èìåþùèå", "óïîðÿäî÷èòü", "äëÿ", "ðàçëè÷íûå", "ïåðâûå",
		"êàê", "ïîìåñòèòü", "îáúåäèíèòü", "âñå", "èçìåíåíèÿ", "ÇÍÀ×ÅÍÈÅ", "ÀÂÒÎÓÏÎÐßÄÎ×ÈÂÀÍÈÅ", "ÈÍÄÅÊÑÈÐÎÂÀÒÜ",
		"ÎÁÙÈÅ", "ÈÅÐÀÐÕÈß", "ÐÀÇÐÅØÅÍÍÛÅ"};

	private String[] fgKeywords1 = {
			"ÍÅÎÏÐÅÄÅËÅÍÎ", "ÅÑÒÜNULL", "ÂÛÁÎÐ", "ÊÎÃÄÀ", "ÒÎÃÄÀ", "ÈÍÀ×Å", "ÊÎÍÅÖ", "È", "Â", "ÈËÈ", "ÈÅÐÀÐÕÈÈ"
	};
	
	boolean keyChar(char c)
	{
//		return c == '&'||c == '{'||c == '}';
		return c == '&'||c == '{'||c == '}' || c == '/' || c == ',' || c == '(' || c == ')'|| c == ';'|| c == '='|| c == '>'|| c == '<'|| c == '*'|| c == '.'|| c == '+'|| c == '-' || c == '[' || c == ']';
	}
	//DONE ðàñöâåòêà çàïðîñà
	
	IWordDetector wordDetector = new IWordDetector() {
		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c);
		}
	};
	public boolean startString = false;
	
	
	public ScannerString() {
		
		setScannerRules("");

	}
	
	public void setScannerRules(String word)
	{
		List<IRule> rules = new ArrayList<IRule>();
		
		addDefaultRules(rules, word);
		
		addWordRules(rules, word);
		
		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);		
		setRules(result);		

	}
	
	private void addDefaultRules(List<IRule> rules, String word) {

		rules.add(new EndOfLineRule("//", provider.getToken(TOKENS.COMMENT)));
		
		Utils.addCompareRules(rules);
		
//		rules.add(new EndOfLineRule("#", provider.getToken(TOKENS.DIRECTIVE)));
//		rules.add(new EndOfLineRule("&", provider.getToken(TOKENS.DIRECTIVE)));
		
		WordRule wordRule = new WordRule(new IWordDetector() {			
			@Override
			public boolean isWordStart(char c) {
				return keyChar(c);
			}
			@Override
			public boolean isWordPart(char c) {
				return keyChar(c);
			}
		}, provider.getToken(TOKENS.KEYWORD_IN_STRING));
		rules.add(wordRule);
	
		
	}

	private void addWordRules(List<IRule> rules, String word)
	{
		WordRule wordRule = new WordRule(wordDetector, provider.getToken(TOKENS.STRING));

		for (int i = 0; i < fgKeywords.length; i++) {
//			wordRule.addWord(fgKeywords[i], provider.getToken(TOKENS.KEYWORD_IN_STRING));
//			wordRule.addWord(fgKeywords[i].toLowerCase(), provider.getToken(TOKENS.KEYWORD_IN_STRING));
			wordRule.addWord(fgKeywords[i].toUpperCase(), provider.getToken(TOKENS.KEYWORD_IN_STRING));
		}
		
		for (int i = 0; i < fgKeywords1.length; i++) {
//			wordRule.addWord(fgKeywords1[i], provider.getToken(TOKENS.KEYWORD_IN_STRING1));
//			wordRule.addWord(fgKeywords1[i].toLowerCase(), provider.getToken(TOKENS.KEYWORD_IN_STRING1));
			wordRule.addWord(fgKeywords1[i].toUpperCase(), provider.getToken(TOKENS.KEYWORD_IN_STRING1));
		}
		
		if(!word.isEmpty())
			wordRule.addWord(word, provider.getToken(TOKENS.COMPARE));
		
		rules.add(wordRule);
		
		
	}
	

	
}
