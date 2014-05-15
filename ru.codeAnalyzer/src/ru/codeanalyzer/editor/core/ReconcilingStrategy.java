package ru.codeanalyzer.editor.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.editor.Editor;
import ru.codeanalyzer.interfaces.ITextParser;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.interfaces.ITextParser.Entity;
import ru.codeanalyzer.utils.Const;
import ru.codeanalyzer.views.OutlineView;
import ru.codeanalyzer.views.core.LineInfo;

public class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension{

	final LinkedHashMap<String, LineInfo> model = new LinkedHashMap<String, LineInfo>();

	ITextParser parser = pico.get(ITextParser.class);
	
	Editor editor;
	IDocument fDocument;
	
	final LinkedHashMap<String, Position> fPositions = new LinkedHashMap<String, Position>();
	final ArrayList<Position> fMarkers = new ArrayList<Position>();

	private OutlineView outlineView;
	 
	public ReconcilingStrategy(Editor editor)
	{
		this.editor = editor;		
		this.editor.setReconcilerStrategy(this);
	}
	
	@Override
	public void initialReconcile() {

		fillFoldingStructure();
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(fPositions);
//				LineInfo line = getLineInfo();
//				if (line!=null)
//					editor.highlightLine(line.line);
				if(outlineView!=null)
					outlineView.update(editor, model);
			}
		});
		
		updateCurrentLine();
	}
	
	LineInfo getLineInfo()
	{
		BuildInfo data = ((EditorInput) editor.getEditorInput()).getData();
		return model.get(data.name);
	}
	
	public void updateCurrentLine() {
	
		BuildInfo data = ((EditorInput) editor.getEditorInput()).getData();
		
		fMarkers.clear();
		if((!data.getCalleeName().isEmpty()) 
				|| data.compare 
				|| (!data.getSearch().isEmpty()))
		{
			fillMarkers();
		}
			
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateMarkers(fMarkers);
				LineInfo line = getLineInfo();
				if (line!=null)
					editor.highlightLine(line.line);
			}
		});
	}
	
	private void fillFoldingStructure()
	{
		try {

			BuildInfo data = ((EditorInput) editor.getEditorInput()).getData();

			fPositions.clear();
			model.clear();
			
			int startLine = 0;
			ArrayList<String> buffer = new ArrayList<String>();
			ArrayList<String> vars =new ArrayList<String>();
			Boolean procWasFound = false;
			
			for (int index = 0; index < fDocument.getNumberOfLines(); index++) {

				IRegion r = fDocument.getLineInformation(index);
				String line = fDocument.get(r.getOffset(), r.getLength());

				buffer.add(line + "\n");

				if (parser.findProcEnd(line)) {

					Entity proc = new Entity();
					int lineOffset = parser.getProcInfo(proc, buffer, vars);
					if (!procWasFound && !vars.isEmpty()) {

						LineInfo lineInfo = new LineInfo();
						lineInfo.line = 0;
						lineInfo.offset = 0;
						lineInfo.title = Const.STRING_VARS_TITLE;
						lineInfo.name = Const.STRING_VARS;
						lineInfo.export = false;
						lineInfo.data = data;
						model.put(lineInfo.name, lineInfo);
					}

					buffer.clear();
					procWasFound = true;
					
					startLine = startLine + lineOffset;
					
					IRegion reg = fDocument.getLineInformation(startLine);
					
					LineInfo lineInfo = new LineInfo();
					lineInfo.line = startLine;
					lineInfo.offset = reg.getOffset();
					lineInfo.title = proc.proc_title;
					lineInfo.name = proc.proc_name;
					lineInfo.export = proc.export;
					lineInfo.data = data;
					model.put(proc.proc_name, lineInfo);
					
					fPositions.put(lineInfo.name + ":" + reg.getLength(), new Position(reg.getOffset(), r.getOffset()- reg.getOffset() + r.getLength() + 1));
					
					startLine = index + 1; 
				}
			}

			if (!buffer.isEmpty()) {
				IRegion reg = fDocument.getLineInformation(startLine);
				LineInfo lineInfo = new LineInfo();
				lineInfo.line = startLine;
				lineInfo.offset = reg.getOffset();
				lineInfo.title = Const.STRING_INIT_TITLE;
				lineInfo.name = Const.STRING_INIT;
				lineInfo.export = false;
				lineInfo.data = data;
				model.put(lineInfo.name, lineInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	private void fillMarkers()
	{
		try {

			BuildInfo data = ((EditorInput) editor.getEditorInput()).getData();

			fMarkers.clear();
			boolean mProcStart = false;
			
			for (int index = 0; index < fDocument.getNumberOfLines(); index++) {

				IRegion r = fDocument.getLineInformation(index);
				String line = fDocument.get(r.getOffset(), r.getLength());
				
				if (parser.findTextInLine(line, data.getSearch())) {
					
					fMarkers.add(new Position(r.getOffset(), line.length()));								
				}

				if (parser.findCompareMarker(line)) {
					
					fMarkers.add(new Position(r.getOffset(), line.length()));								
				}
				
				if (parser.findProcStart(line, data.name))
				{
					mProcStart = true;	
				}
				
				if (parser.findProcEnd(line)) {
				
					mProcStart = false;										
				}
				
				if (mProcStart)
					for (String callee : data.getCalleeName()) {
						if (parser.findCallee(line, callee)) {
							fMarkers.add(new Position(r.getOffset(), line.length()));
						}
					}
				
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	@Override
	public void setDocument(IDocument document) {
		this.fDocument = document;		
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		 initialReconcile();
		
	}

	@Override
	public void reconcile(IRegion partition) {
		 initialReconcile();
		
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		
	}

	public void updateOutineView(OutlineView view) {
		if(outlineView==null)
		{
			outlineView = view;
			outlineView.update(editor, model);
		}else
		{
			if(!outlineView.openWithEditor(editor))
			{
				outlineView.update(editor, model);
			}
		}
	}
	
	public void setCurrentInOutline(String name) {
		if (outlineView != null) {
			
			LineInfo line = model.get(name);
			if(line!=null)
				outlineView.setCurrentLine(line);

		}
	}
}
