package ru.codeanalyzer.editor.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WordRule;

import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IColorManager.TOKENS;
import ru.codeanalyzer.utils.Utils;

public class ScannerComment extends RuleBasedScanner {

	IColorManager provider = pico.get(IColorManager.class);
	
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
	
	public ScannerComment() {
		
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

		Utils.addCompareRules(rules);		
						
	}

	private void addWordRules(List<IRule> rules, String word)
	{
		WordRule wordRule = new WordRule(wordDetector, provider.getToken(TOKENS.STRING));

		if(!word.isEmpty())
			wordRule.addWord(word, provider.getToken(TOKENS.COMPARE));
		
		rules.add(wordRule);
		
		
	}
	

	
}
