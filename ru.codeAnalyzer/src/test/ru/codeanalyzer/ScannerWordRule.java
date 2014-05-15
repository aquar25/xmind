package test.ru.codeanalyzer;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import ru.codeanalyzer.editor.core.ScannerCode;

public class ScannerWordRule extends WordRule {

//	private StringBuffer fBuffer= new StringBuffer();
//	private boolean fIgnoreCase= false;
//	private boolean fInsideString= false;
	
	@Override
	public IToken evaluate(ICharacterScanner _scanner) {
		ScannerCode scanner = (ScannerCode) _scanner;
		if (scanner.startString)
			return Token.UNDEFINED;
		return super.evaluate(scanner);

	}

//	protected void unreadBuffer(ICharacterScanner scanner) {
//		for (int i= fBuffer.length() - 1; i >= 0; i--)
//			scanner.unread();
//	}
	
	public ScannerWordRule(IWordDetector detector, IToken defaultToken,
			boolean ignoreCase) {
		super(detector, defaultToken, ignoreCase);
	}

	public ScannerWordRule(IWordDetector detector, IToken defaultToken) {
		super(detector, defaultToken);
	}

	public ScannerWordRule(IWordDetector detector) {
		super(detector);
	}

}
