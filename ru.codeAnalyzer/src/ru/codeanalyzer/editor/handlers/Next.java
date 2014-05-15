package ru.codeanalyzer.editor.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.pico;

public class Next extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		pico.get(IEvents.class).next();
		
		return null;
	}

}
