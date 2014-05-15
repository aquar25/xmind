package test.ru.codeanalyzer;

import java.util.HashMap;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

import ru.codeanalyzer.editor.core.ScannerCode;

public class ScannerStringRule implements IRule {

	private final IToken token;

	private IWordDetector fDetector;
	
	private StringBuffer fBuffer = new StringBuffer();

	private HashMap<String, IToken> fWords = new HashMap<String, IToken>();
	
	protected boolean wordDetected = false;
	
	public ScannerStringRule(IToken token, IWordDetector fDetector) {
		this.token = token;
		this.fDetector = fDetector;
		
	}

	public void addWord(String word, IToken token)
	{
		fWords.put(word, token);
	}
	
	@Override
	public IToken evaluate(ICharacterScanner _scanner) {
		ScannerCode scanner = (ScannerCode)_scanner;
		
		int c = ICharacterScanner.EOF;
		if(!scanner.startString)
			c = scanner.read();
		if (c == '\"' || c == '|' || scanner.startString) {
			scanner.startString = true;
			do {
				c = scanner.read();
//				if(wordDetected)
//					scanner.unread();	
				if(fDetector.isWordStart((char) c))
				{
					
					fBuffer.setLength(0);
					do {
						fBuffer.append((char) c);
						c= scanner.read();
					} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
										
					String buffer= fBuffer.toString();
					
					IToken _token= (IToken)fWords.get(buffer);
					
					if (_token != null)
						if(!wordDetected)
						{
							wordDetected = true;
							scanner.unread();
							unreadBuffer(scanner);
							return token;
						}else
						{
							wordDetected = false;
							return _token;
						}
					wordDetected = false;
				}				
				
				if (c == '/')
					c = scanner.read();				
			} while (c != ICharacterScanner.EOF && (c != '\"') && (c != '/'));
			if (c == '/')
			{
				scanner.unread();
				scanner.unread();
			}
			scanner.startString = false;
			return token;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
	
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i= fBuffer.length()-1; i >= 0; i--)
			scanner.unread();
	}

}
