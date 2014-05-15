package ru.codeanalyzer.views.core;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.views.OutlineView;

public class OutlineContentProvider implements ITreeContentProvider  {

	IEvents events = pico.get(IEvents.class);
	
	private static final Object[] EMPTY_ARRAY = new Object[0];
//	private OutlineView view;
	 
	public OutlineContentProvider(OutlineView outlineView) {
//		view = outlineView;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection)
			return ((Collection<LineInfo>) inputElement).toArray();
		else
			return EMPTY_ARRAY;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
//		if (parentElement instanceof LineInfo)
//		{
//			if (view.showCalls)
//				return BuildInfo.toLineInfo(events.getCalls(BuildInfo.fromLineInfo((LineInfo)parentElement)));
//			else
//				return BuildInfo.toLineInfo(events.getCalled(BuildInfo.fromLineInfo((LineInfo)parentElement)));
//				
//		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
//		if (element instanceof LineInfo)
//			return !((LineInfo)element).items.isEmpty();
		
		return false;
	}

	

	

}
