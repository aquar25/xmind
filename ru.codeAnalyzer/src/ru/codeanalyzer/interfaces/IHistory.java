package ru.codeanalyzer.interfaces;

import ru.codeanalyzer.core.model.BuildInfo;

public interface IHistory {

	public abstract BuildInfo getPrev();

	public abstract BuildInfo getNext();

	public abstract void setCurrent(BuildInfo data);

	void clear();
	
}