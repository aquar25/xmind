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

public class ScannerCode extends RuleBasedScanner {

	IColorManager provider = pico.get(IColorManager.class);
	
	public static boolean keyword(String v) {
		for(String s:fgKeywords)
		{
			if(s.equalsIgnoreCase(v))
				return true;
		}
		return false;
	}

	//TODO доработать сканер кода - расцветка и ключевые слова
	
	private static String[] fgKeywords = { "СокрЛ", "СокрП", "СокрЛП", "Число",
			"ЗначениеЗаполнено", "Окр", "Цел", "Формат", "Не",
			"Ложь", "Истина", "Или", "И", "ПолучитьМакет", "ОткрытьФорму",
			"ОткрытьФормуМодульно", "ЗаполнитьЗначенияСвойство", "Оповестить",
			"Закрыть", "НСТР", "Пустая", "Вопрос", "СтрЗаменить",
			"Прав", "Лев", "Предупреждение", "Сообщить", "ЭтоНовый",
			"КонецДня", "НачалоДня", "Перем", "Новый", "Неопределено",
			"Процедура", "КонецПроцедуры", "Функция", "КонецФункции",
			"Возврат", "Экспорт", "Если", "Тогда", "Иначе", "КонецЕсли", "Конецесли", "конецЕсли",
			"ИначеЕсли", "Попытка", "Исключение", "КонецПопытки",
			"НачатьТранзакцию", "ЗафиксироватьТранзакцию",
			"ОтменитьТранзакцию", "Для", "Каждого", "Из", "Пока", "Цикл",
			"КонецЦикла", "Прервать", "Продолжить" };

	boolean keyChar(char c)
	{
		return c == '/' || c == '?' || c == '(' || c == ')'|| c == ';'|| c == '='|| c == '>'|| c == '<'|| c == '*'|| c == '.'|| c == '+'|| c == '-' || c == ',' || c == '[' || c == ']';
	}
	
	IWordDetector wordDetector = new IWordDetector() {
		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c);//!(Character.isWhitespace(c) || keyChar(c));
		}
	};
	public boolean startString = false;
	
	public ScannerCode() {
		
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
		
		rules.add(new EndOfLineRule("#", provider.getToken(TOKENS.DIRECTIVE)));
		rules.add(new EndOfLineRule("&", provider.getToken(TOKENS.DIRECTIVE)));
		
		WordRule wordRule = new WordRule(new IWordDetector() {			
			@Override
			public boolean isWordStart(char c) {
				return keyChar(c);
			}
			@Override
			public boolean isWordPart(char c) {
				return keyChar(c);
			}
		}, provider.getToken(TOKENS.KEYWORD));
		rules.add(wordRule);
	
		
	}

	private void addWordRules(List<IRule> rules, String word)
	{
		WordRule wordRule = new WordRule(wordDetector, provider.getToken(TOKENS.OTHER));

		for (int i = 0; i < fgKeywords.length; i++) {
			wordRule.addWord(fgKeywords[i], provider.getToken(TOKENS.KEYWORD));
			wordRule.addWord(fgKeywords[i].toLowerCase(), provider.getToken(TOKENS.KEYWORD));
			wordRule.addWord(fgKeywords[i].toUpperCase(), provider.getToken(TOKENS.KEYWORD));
		}
		
		if(!word.isEmpty())
			wordRule.addWord(word, provider.getToken(TOKENS.COMPARE));
		
		rules.add(wordRule);		
		
	}
	

	
}
