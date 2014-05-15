package ru.codeanalyzer.core;

import java.util.ArrayList;

import ru.codeanalyzer.core.model.BuildInfo;
import ru.codeanalyzer.interfaces.IHistory;

public class History implements IHistory {

	final int size = 10;
	
	ArrayList<BuildInfo> operations = new ArrayList<BuildInfo>();
	
	int current = -1;
	
	@Override
	public void clear()
	{
		current = -1;
		operations.clear();
	}
	
	@Override
	public BuildInfo getPrev()
	{
		if(current<0) return null;
		current = current==0 ? 0 : current-1;
		return operations.get(current);
	}
	
	@Override
	public BuildInfo getNext()
	{
		if(current<0) return null;
		current = current==(operations.size()-1) ? current : current+1;
		return operations.get(current);
	}
	
	@Override
	public void setCurrent(BuildInfo data)
	{
		int k = 0;
		if (current >= 0)
		{
			k = operations.size();
			for (int i = (current+1); i < k; i++) {
				operations.remove(operations.size()-1);
			}
						
		}
		
		if (current >= size)
			{
				operations.remove(0);
				current--;
			}
 		
		if (current >= 0) {
			BuildInfo _data = operations.get(current);
			if (_data.module != data.module || _data.name != data.name) {
				operations.add(data);
				current++;
			}
		} else {
			operations.add(data);
			current++;
		}
	}
}
