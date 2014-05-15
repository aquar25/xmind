package ru.codeanalyzer.editor.core;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

public class ScannerStringRule extends MultiLineRule {

	public ScannerStringRule(String startSequence, String endSequence,
			IToken token) {
		super(startSequence, endSequence, token);
		fEscapeCharacter = '/';
		fEscapeContinuesLine = true;
	}

	@SuppressWarnings("rawtypes")
	private static class DecreasingCharArrayLengthComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return ((char[]) o2).length - ((char[]) o1).length;
		}
	}
	
	private char[][] fLineDelimiters;
	private char[][] fSortedLineDelimiters;
	@SuppressWarnings("rawtypes")
	private Comparator fLineDelimiterComparator= new DecreasingCharArrayLengthComparator();
	


	@SuppressWarnings("unchecked")
	@Override
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		char[][] originalDelimiters= scanner.getLegalLineDelimiters();
		int count= originalDelimiters.length;
		if (fLineDelimiters == null || fLineDelimiters.length != count) {
			fSortedLineDelimiters= new char[count][];
		} else {
			while (count > 0 && Arrays.equals(fLineDelimiters[count - 1], originalDelimiters[count - 1]))
				count--;
		}
		if (count != 0) {
			fLineDelimiters= originalDelimiters;
			System.arraycopy(fLineDelimiters, 0, fSortedLineDelimiters, 0, fLineDelimiters.length);
			Arrays.sort(fSortedLineDelimiters, fLineDelimiterComparator);
		}

		int readCount= 1;
		int c;
		while ((c= scanner.read()) != ICharacterScanner.EOF) {
			if (c == fEscapeCharacter) {
				boolean secondSymbol = false;
				// Skip escaped character(s)
				if (fEscapeContinuesLine) {
					boolean endOfLine = false;
					while (!endOfLine) {
						c = scanner.read();
						
						for (int i = 0; i < fSortedLineDelimiters.length; i++) {
							if (c == fSortedLineDelimiters[i][0]
									&& sequenceDetected(scanner,
											fSortedLineDelimiters[i], true)) {
								endOfLine = true;
								break;
							}
						}
						
						if (!secondSymbol)
							if (c == fEscapeCharacter)
								secondSymbol = true;
							else
							{
								scanner.unread();
								break;
							}
					}
				} else
					scanner.read();

			} else if (fEndSequence.length > 0 && c == fEndSequence[0]) {
				// Check if the specified end sequence has been found.
				if (sequenceDetected(scanner, fEndSequence, true))
					return true;
			} else if (fBreaksOnEOL) {
				// Check for end of line since it can be used to terminate the pattern.
				for (int i= 0; i < fSortedLineDelimiters.length; i++) {
					if (c == fSortedLineDelimiters[i][0] && sequenceDetected(scanner, fSortedLineDelimiters[i], true))
						return true;
				}
			}
			readCount++;
		}

		if (fBreaksOnEOF)
			return true;

		for (; readCount > 0; readCount--)
			scanner.unread();

		return false;
	}

	

}
