package ru.codeanalyzer.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import ru.codeanalyzer.interfaces.ICData;
import ru.codeanalyzer.interfaces.pico;
import ru.codeanalyzer.utils.AesCrypt;

public class SQLService {

	public int getCount(Connection con) throws SQLException {
		
		String SQL = 
				"SELECT COUNT(filename) FROM [dbo].[Config] " +
				"WHERE " +
				"filename NOT LIKE ? AND " +
				"filename NOT LIKE ? AND " +
				"filename NOT LIKE ? ";
		PreparedStatement prep = con.prepareStatement(SQL);
		prep.setString(1, "%.%");
		prep.setString(2, "root%");
		prep.setString(3, "version%");
		ResultSet rs = prep.executeQuery();
		
		try {
			if (rs.next())
				return rs.getInt(1);
			else
				throw new SQLException();
		} finally {
			rs.close();
		}
	}

	public List<String> getMetaNames(Connection sql) throws InvocationTargetException {
		List<String> list = new ArrayList<String>();
		
		try {
			String SQL = "SELECT [filename] FROM [dbo].[Config] " +
					"WHERE " +
					"filename NOT LIKE ? AND " +
					"filename NOT LIKE ? AND " +
					"filename NOT LIKE ? ";
			PreparedStatement prep;

			prep = sql.prepareStatement(SQL);

			prep.setString(1, "%.%");
			prep.setString(2, "root%");
			prep.setString(3, "version%");
			ResultSet rs = prep.executeQuery();

			while (rs.next()) {

				String item = rs.getString(1);
				list.add(item);

			}
		} catch (SQLException e) {
			throw new InvocationTargetException(e, e.getMessage()); 
		}
		return list;
	}

	public ICData getConfigData(Connection sql, String name) throws InvocationTargetException {
		
		String text = "";
		
		InputStream inflInstream = null;
		ByteArrayOutputStream out = null;
		
		try {


			String SQL = "SELECT [BinaryData] FROM [dbo].[Config] where [filename]=?";
			PreparedStatement prep = sql.prepareStatement(SQL);
			prep.setString(1, name);
			ResultSet rs = prep.executeQuery();
			
			while (rs.next()) {
				
				out = new ByteArrayOutputStream();
				
				inflInstream = new InflaterInputStream(
						rs.getBinaryStream(1),
						new Inflater(true));

				byte bytes[] = new byte[1024];
				while (true) {
					int length = inflInstream.read(bytes, 0, 1024);
					if (length == -1)
						break;
					
					out.write(bytes, 0, length);
				}

				text = out.toString(AesCrypt.characterEncoding);
				
			}
			    
		} catch (Exception e) {
			throw new InvocationTargetException(e, e.getMessage()); 
		
		}
		ICData result = pico.get(ICData.class);
		result.setText(text);
		
		return result;
	}


}
