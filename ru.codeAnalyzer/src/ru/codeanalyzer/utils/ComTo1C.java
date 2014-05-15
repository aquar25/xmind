package ru.codeanalyzer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.codeanalyzer.core.model.ConnectionTo1C;


public class ComTo1C {

	private static ComTo1C instance;
	private ComTo1C()
	{
		
	}
	public static ComTo1C getInstance()
	{
		if(instance==null)
			instance = new ComTo1C();
		return instance;
	}
	
	public void checkConnection(ConnectionTo1C con)
	{
		try {
			String name = "checkConnectionTo1C";
			InputStream src = (InputStream) getClass().getResource("/lib/" + name + ".exe").openStream();
			File exeTempFile = File.createTempFile(name, ".exe");
			FileOutputStream out = new FileOutputStream(exeTempFile);
			byte[] temp = new byte[1024];
			int rc;
			while((rc = src.read(temp)) > 0)
				out.write(temp, 0, rc);
			src.close();
			out.close();
			exeTempFile.deleteOnExit();
			
			String line;
			String[] cmd = {exeTempFile.toString(), con.getConnectionString()};
			ProcessBuilder builder = new ProcessBuilder(cmd);
			//builder.redirectErrorStream(true);
			Process process = builder.start();
			
			InputStream stdout = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stdout));

			StringBuilder result = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
				//System.out.println("Stdout: " + line);
			}
			process.waitFor();

			if(result.toString().trim().equalsIgnoreCase("ok"))
			{
				Utils.message(Const.MESSAGE_CONFIG_CONNECTION_CHECK);
			}
			else
			{
				Utils.message(result.toString());
			}
			
			process.destroy();
		} catch (Exception e) {
			Utils.message(Const.ERROR_CONFIG_CONNECTION_CHECK);
		}
	}
	
}
