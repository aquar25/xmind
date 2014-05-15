package ru.codeanalyzer.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.codeanalyzer.editor.core.ScannerCode;
import ru.codeanalyzer.exceptions.ProcNotFoundException;
import ru.codeanalyzer.interfaces.ITextParser;
import ru.codeanalyzer.utils.Const;
import difflib.DiffRow;
import difflib.DiffRowGenerator;

public class TextParser implements ITextParser {
	
	
	public void parseObject(File f, IEntity line) throws Exception {
		String name = f.getName();
		String s[] = name.split("\\.");
		if (s.length < 3)
			throw new Exception();

		boolean isConf = s[0].equalsIgnoreCase("Конфигурация");
		if (isConf) {
			//DONE загрузка модулей конфигурации в group1
			line.group1 = "Конфигурация";
			line.group2 = "МодулиКонфигурации";
			line.module = s[1];
		} else {
			line.group1 = s[0];
			line.group2 = s[1];

			if (s[2].equalsIgnoreCase("Форма")||s[2].equalsIgnoreCase("Команда"))
				line.module = s.length > 3 ? s[3] : "";
			else
				line.module = s[2];
		}
	}

	public boolean findProcStart(String file_line, String proc_name, Entity line, ProcStartReader procStart) {

		//DONE загрузка представления процедур в виде Имя(...)
		if(procStart!=null)
			procStart.temp = new StringBuilder();
		
		String _line = file_line.toUpperCase();

		String pattern;
		if(proc_name==null)
			pattern = Const.PATTERN_PROCEDURE;
		else
			pattern = "(ФУНКЦИЯ|ПРОЦЕДУРА)\\s+" + proc_name.toUpperCase() +"\\(";
		
		Pattern r = Pattern.compile(pattern);

		String param_list = "";
		
		String _p, __p;
		Pattern _r, __r;
		Matcher _m, __m;

		Matcher m = r.matcher(_line);
		if (m.find()) {

			String v = m.group().replace("(", "");
			
			// комментарий
			_p = "/{2,}.*".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				return false;

			// имя
			line.proc_name = v.replace("ФУНКЦИЯ", "").replace("ПРОЦЕДУРА", "")
					.trim();
			// представление
			int i = file_line.indexOf(" ");
			i = i < 0 ? file_line.indexOf("\t") : i;
			int j = file_line.indexOf("(");
			try {
				line.proc_title = file_line.substring(i, j).concat("(...)")
						.trim();
			} catch (Exception e) {
				line.proc_title = file_line;
			}
			//DONE загрузка признака экспортной процедуры
			//признак экспорта
			line.export = (_line.indexOf("ЭКСПОРТ")>=0);
			
			//если в текущей строке нет ')', то читаем до него
			_p = pattern.concat(".*\\)");
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			
			//DONE обработка списка параметров
			
			param_list = file_line;
			while (!_m.find())
			{
				if(procStart!=null)
				{
					try {
						file_line = procStart.bufferedReader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
						if(file_line==null)
							return false;
				
					procStart.temp.append(file_line + "\n");
				}
				// комментарий
				__p = ",.*/{2,}";
				__r = Pattern.compile(__p);
				__m = __r.matcher(file_line);
				if (__m.find())
				{
					int k = file_line.indexOf(__m.group()); 
					file_line = file_line.substring(0, k).trim().concat(",");
					//file_line = __m.group().replace("/", "").trim();
				}else file_line = file_line.trim();

				param_list = param_list.concat(file_line);
				
				_line = file_line.toUpperCase();
				_p = ".*\\)";
				_r = Pattern.compile(_p);
				_m = _r.matcher(_line);					
			}
			
			line.export = (_line.indexOf("ЭКСПОРТ")>=0);
			
			i = param_list.indexOf("(");
			j = param_list.indexOf(")");
			if(i>=0 && j>=0) 
				param_list = param_list.substring(i+1,j);
			
			line.params = param_list.split(",");
			
			return true;
		}

		return false;

	}

	public boolean findProcEnd(String file_line) {

		String _line = file_line.toUpperCase();

		String pattern = "(КОНЕЦПРОЦЕДУРЫ|КОНЕЦФУНКЦИИ)";
		Pattern r = Pattern.compile(pattern);

		String _p;
		Pattern _r;
		Matcher _m;

		Matcher m = r.matcher(_line);
		if (m.find()) {
			String v = m.group();

			// комментарий
			_p = "/{2,}.*".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				return false;

			// строка
			_p = "\".*".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				return false;

			return true;
		}

		return false;
	}

	public boolean isCommentOrDirective(String file_line) {

		if(file_line.isEmpty())
			return true;
		file_line = file_line.replace(Character.toChars(65279)[0],' ');
		String _line = file_line.toUpperCase();

		String pattern = "^\\s*(/{2,}.*|#|&)";
		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(_line);
		if (m.find()) {			
			return true;
		}
		return false;
	}

	public List<String> findProcsInString(String _line,
			String exclude_name) {

		_line = _line.toUpperCase();
		
		List<String> result = new ArrayList<String>();

		// String pattern = "/{2,}.*СТРУКТУРАОБЯЗАТЕЛЬНЫХПОЛЕЙРАСЧЕТЫУПР";
		String pattern = Const.PATTERN_PROCEDURE_IN_STRING; 
		Pattern r = Pattern.compile(pattern);

		String _p;
		Pattern _r;
		Matcher _m;

		// FUTURE оптимизация поиска имени процедуры в строке

		Matcher m = r.matcher(_line);
		while (m.find()) {
			String v = m.group().replace("(", "");
			if (v.equalsIgnoreCase(exclude_name))
				continue;

			// комментарии
			_p = "/{2,}.*".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				continue;
			
			// строка
			_p = "\\|[^\"]*".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				continue;
			
			// новый
			_p = "НОВЫЙ\\s+".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				continue;
			
			//ищем модуль
			if(v.contains("."))
			{
				_p = Const.PATTERN_MODULE.concat(v);
				_r = Pattern.compile(_p);
				_m = _r.matcher(_line);
				if (_m.find())
					v=_m.group();
			}else{
				if (ScannerCode.keyword(v.trim()))
					continue;
			}
				
			
			result.add(v.trim());
			// System.out.println("Found value: " + v);
		}

		return result;
	}
	
	@Override
	public boolean findCallee(String _line, String _calleeName) {

		if(_line.isEmpty()||_calleeName.isEmpty())
			return false;
		
		if(isCommentOrDirective(_line))
			return false;
//		_line = _line.replace(Character.toChars(65279)[0],' ');
		String line = _line.toUpperCase();
		String calleeName = _calleeName.toUpperCase();

		String pattern = calleeName + "\\(";
		Pattern r = Pattern.compile(pattern);

		Matcher m = r.matcher(line);
		if (m.find()) {			
			return true;
		}
		return false;
	}

	@Override
	public boolean findProcStart(String line, String name) {
		
		if(name==null) return false;
		
		String _line = line.toUpperCase();

		String pattern = "(ФУНКЦИЯ|ПРОЦЕДУРА)\\s+" + name.toUpperCase() +"\\s*\\(";
	
		Pattern r = Pattern.compile(pattern);

		String _p;
		Pattern _r;
		Matcher _m;

		Matcher m = r.matcher(_line);
		if (m.find()) {
			String v = m.group();
			v = v.replace("(", "\\(");
			// комментарий
			_p = "/{2,}.*".concat(v);
			_r = Pattern.compile(_p);
			_m = _r.matcher(_line);
			if (_m.find())
				return false;

			return true;
		}

		return false;
	}

	@Override
	public int getProcInfo(Entity proc, ArrayList<String> buffer, ArrayList<String> vars) throws ProcNotFoundException {

		boolean founded = false;
		
		vars.clear();
		String pattern = Const.PATTERN_PROCEDURE;
//		String pattern = "(ФУНКЦИЯ|ПРОЦЕДУРА)\\s+\\w+(\\w|\\d)*\\s*\\(";
		
		Pattern r = Pattern.compile(pattern);
		String param_list = "";
		String _p, __p;
		Pattern _r, __r;
		Matcher _m, __m;

		for (int line = 0; line < buffer.size(); line++) {

			String file_line = buffer.get(line);

			String _line = file_line.toUpperCase();

			Matcher m = r.matcher(_line);
			if (!m.find()) {

				vars.add(file_line);

			} else {

				founded = true;
				
				String v = m.group().replace("(", "");

				// комментарий
				_p = "/{2,}.*".concat(v);
				_r = Pattern.compile(_p);
				_m = _r.matcher(_line);
				if (_m.find())
					continue;

				// имя
				proc.proc_name = v.replace("ФУНКЦИЯ", "")
						.replace("ПРОЦЕДУРА", "").trim();
				// представление
				int i = file_line.indexOf(" ");
				i = i < 0 ? file_line.indexOf("\t") : i;
				int j = file_line.indexOf("(");
				try {
					proc.proc_title = file_line.substring(i, j).concat("(...)")
							.trim();
				} catch (Exception e) {
					proc.proc_title = file_line;
				}

				// если в текущей строке нет ')', то читаем до него
				_p = pattern.concat(".*\\)");
				_r = Pattern.compile(_p);
				_m = _r.matcher(_line);

				// DONE обработка списка параметров

				param_list = file_line;
				if (!_m.find()) {

					for (int _i = line+1; _i < buffer.size(); _i++) {

						file_line = buffer.get(_i);

						// комментарий
						__p = ",.*/{2,}";
						__r = Pattern.compile(__p);
						__m = __r.matcher(file_line);
						if (__m.find()) {
							int k = file_line.indexOf(__m.group());
							file_line = file_line.substring(0, k).trim()
									.concat(",");
							// file_line = __m.group().replace("/", "").trim();
						} else
							file_line = file_line.trim();

						param_list = param_list.concat(file_line);

						_line = file_line.toUpperCase();
						_p = ".*\\)";
						_r = Pattern.compile(_p);
						_m = _r.matcher(_line);
						if (_m.find())
							break;
					}
				}

				proc.export = (_line.indexOf("ЭКСПОРТ") >= 0);

				i = param_list.indexOf("(");
				j = param_list.indexOf(")");
				if (i >= 0 && j >= 0)
					param_list = param_list.substring(i + 1, j);

				proc.params = param_list.split(",");

				break;

			}
		}
		
		
		if(!founded)
			throw new ProcNotFoundException();
		
		for (int k = 0; k < vars.size(); k++) {
			buffer.remove(0);
		}
		
		int lineOffset = vars.size();
		
		int _k = 0;
		for (int k = vars.size()-1; k >=0 ; k--) {
			String s = vars.get(k);
			if(isCommentOrDirective(vars.get(k)))
			{	
				buffer.add(0, s);
				_k++;
			} else break;
		}
		for (int k = 0; k < _k; k++) {
			vars.remove(vars.size()-1);
		}

		return lineOffset;
	}

	@Override
	public String compare(String text, String text1) {
		String[] t1 = text.split("\n");
		String[] t2 = text1.split("\n");

		List<String> original = Arrays.asList(t1);
		List<String> revised = Arrays.asList(t2);

		StringBuilder sb = new StringBuilder();

		DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
		DiffRowGenerator dfg = builder.build();
		List<DiffRow> rows = dfg.generateDiffRows(original, revised);
		for (DiffRow diffRow : rows) {
			DiffRow.Tag tag = diffRow.getTag();
			
			switch (tag) {
			case CHANGE:

				sb.append(Const.COMPARE_CHANGED_MARKER + diffRow.getOldLine() + "\n");

				break;
			case DELETE:

				sb.append(Const.COMPARE_REMOVED_MARKER + diffRow.getOldLine() + "\n");
				break;

			case INSERT:

				sb.append(Const.COMPARE_ADDED_MARKER + diffRow.getNewLine() + "\n");
				break;
				
			default:
				sb.append(diffRow.getOldLine() + "\n");
				break;
			}

		}

		return sb.toString()
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("<br>", "");

	}

	@Override
	public boolean findCompareMarker(String line) {
		String _line = line.toUpperCase();

		return _line.contains(Const.COMPARE_TEXT_MARKER) 
				|| _line.contains(Const.COMPARE_CHANGED_MARKER)
				|| _line.contains(Const.COMPARE_REMOVED_MARKER)
				|| _line.contains(Const.COMPARE_ADDED_MARKER);
		
//		String pattern = ".*(" + 
//				Const.COMPARE_TEXT_MARKER + "|" +
//				Const.COMPARE_CHANGED_MARKER + "|" +
//				Const.COMPARE_REMOVED_MARKER + "|" +
//				Const.COMPARE_ADDED_MARKER +
//				").*";
//	
//		Pattern r = Pattern.compile(pattern);
//		Matcher m = r.matcher(_line);
//		return m.find();
	}
	
	@Override
	public boolean findTextInLine(String line, String text) {
		if(text.trim().isEmpty()) return false;
		
//		String _line = line.toUpperCase();
//
//		String pattern = ".*" + text.toUpperCase() +".*";
//	
//		Pattern r = Pattern.compile(pattern);
//		Matcher m = r.matcher(_line);
//		return m.find();
		return line.toUpperCase().contains(text.toUpperCase());
	}
	
	// *****************************************************
	
	

}
