package ru.codeanalyzer.core.model;

public class ConnectionTo1C {
	public String exe;
	public String directory;
	public String server;
	public String ref;
	public String login;
	public String password;
	
	public String getConnectionString()
	{
		StringBuilder res = new StringBuilder();
		if(directory.isEmpty())
		{
			res.append("Srvr=\"" + server + "\";");
			res.append("Ref=\"" + ref + "\";");
			
		}else
		{
			res.append("File=\"" + directory + "\";");
		}
		
		res.append("usr=\"" + login + "\";");
		res.append("pwd=\"" + password + "\";");
	
		return res.toString();
	}
}
