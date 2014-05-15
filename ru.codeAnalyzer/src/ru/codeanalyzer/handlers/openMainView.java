package ru.codeanalyzer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.codeanalyzer.views.MainView;

public class openMainView extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			HandlerUtil.getActiveWorkbenchWindowChecked(event).getActivePage().showView(MainView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
