package ru.codeanalyzer.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;

import ru.codeanalyzer.interfaces.IDbManager;
import ru.codeanalyzer.interfaces.pico;

public class IsConfigurationLoaded extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

//		Command command = Utils.getCommand(CommandConstants.loadConfiguration);
//		State state = command.getState("org.eclipse.ui.commands.toggleState");
//		boolean isToggled = (Boolean) state.getValue();
		IDbManager dbManager = pico.get(IDbManager.class);
//		IDb db = dbManager.getActive();
				
		return dbManager.getActive()!=null;
	}

}
