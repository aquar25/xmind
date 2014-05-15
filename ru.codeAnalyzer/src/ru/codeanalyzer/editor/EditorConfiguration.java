package ru.codeanalyzer.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import ru.codeanalyzer.CodeAnalyserActivator;
import ru.codeanalyzer.editor.core.DocumentPartitionScanner;
import ru.codeanalyzer.editor.core.ReconcilingStrategy;
import ru.codeanalyzer.editor.core.ScannerCode;
import ru.codeanalyzer.editor.core.ScannerComment;
import ru.codeanalyzer.editor.core.ScannerString;
import ru.codeanalyzer.editor.core.TextDoubleClickStrategy;
import ru.codeanalyzer.interfaces.IColorManager;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.IColorManager.TOKENS;

public class EditorConfiguration extends SourceViewerConfiguration {

	private Editor editor;
	private RuleBasedScanner scanner;
	private RuleBasedScanner string_scanner;
	private RuleBasedScanner comment_scanner;
	IColorManager provider = pico.get(IColorManager.class);
	
	
	public EditorConfiguration(Editor editor) {
		this.editor = editor;
	}
	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return CodeAnalyserActivator.PARTITIONING;
	}
	
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		
		return new String[] { 
				IDocument.DEFAULT_CONTENT_TYPE, 
				DocumentPartitionScanner.STRING,
				DocumentPartitionScanner.COMMENT};
	}
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		DefaultDamagerRepairer dr1 = new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr1, DocumentPartitionScanner.STRING);
		reconciler.setRepairer(dr1, DocumentPartitionScanner.STRING);
		
		DefaultDamagerRepairer dr2 = new DefaultDamagerRepairer(getCommentScanner());
		reconciler.setDamager(dr2, DocumentPartitionScanner.COMMENT);
		reconciler.setRepairer(dr2, DocumentPartitionScanner.COMMENT);
	
		return reconciler;
	}
	
	private ITokenScanner getCommentScanner() {
		if (comment_scanner == null) {
			comment_scanner = new ScannerComment();
			comment_scanner.setDefaultReturnToken(provider.getToken(TOKENS.COMMENT));
		}
		return comment_scanner;
	}
	private ITokenScanner getStringScanner() {
    	if (string_scanner == null) {
    		string_scanner = new ScannerString();
    		string_scanner.setDefaultReturnToken(provider.getToken(TOKENS.STRING));
		}
		return string_scanner;
	}

	protected ITokenScanner getCodeScanner() {
		if (scanner == null) {
			scanner = new ScannerCode();
			//scanner.setDefaultReturnToken(provider.getToken(TOKENS.OTHER));
		}
		return scanner;
	}
	
	public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        ReconcilingStrategy strategy = new ReconcilingStrategy(editor);
                
        MonoReconciler reconciler = new MonoReconciler(strategy, false);
        
        return reconciler;
    }
    
    @Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		
		return new TextDoubleClickStrategy();
		// super.getDoubleClickStrategy(sourceViewer, contentType);
	}

    public void lightWord(String text)
    {
    	((ScannerCode)getCodeScanner()).setScannerRules(text);
    	((ScannerString)getStringScanner()).setScannerRules(text);
    	((ScannerComment)getCommentScanner()).setScannerRules(text);
    	
    	getPresentationReconciler(null).install(editor.getViewer());
    }
}
