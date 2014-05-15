package ru.codeanalyzer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.codeanalyzer.interfaces.IEvents;
import ru.codeanalyzer.interfaces.pico;

public class Compare extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		pico.get(IEvents.class).compare();
		
		return null;
	}

}
