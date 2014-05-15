package ru.codeanalyzer.interfaces;

public interface ICData {

	public enum Type{
		Config, Document, Catalog, Enum, Report, CommonModules, DataProcessor, NotDefined, NotObject;
		
		public String getCaption(){
			switch (this) {
			case Catalog:
				return "Справочник";
			case Document:
				return "Документ";
			case Enum:
				return "Перечисление";
			case Report:
				return "Отчет";
			case CommonModules:
				return "Общий модуль";
			case DataProcessor:
				return "Обработка";
			default:
				return "";
			}	
		}
	}
	
	public abstract void setText(String text);
	
	public abstract Type getType();

	public abstract boolean needLoading();

	public abstract String getName();

}