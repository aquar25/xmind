package ru.codeanalyzer.interfaces;

import ru.codeanalyzer.interfaces.ILoaderService.operationType;



/**
 * существует в единственном экземпл€ре
 * хранит активную базу
 * 
 * @author Enikeev M.A.
 *
 */
public interface IDbManager {

	public void init();
	
	public IDb get(String ID);
	
	public IDb getActive();
	
	public IDb getNonActive();
	
	public void setActive(String ID);

	public String getOperationName(operationType key);
	
	void execute(final IDb db);
	
}
