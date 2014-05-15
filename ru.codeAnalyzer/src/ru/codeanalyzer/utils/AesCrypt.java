package ru.codeanalyzer.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ru.codeanalyzer.exceptions.CryptException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class AesCrypt {

	//CRYPT *******************************************************

	//DONE ключ и пароль шифрования
	// длина ключа: 16 байт	                                             
	//public static final String CRYPT_PASSWORD = "YdxVmg||LJ8d#*1D";
	//public static final String CRYPT_PASSWORD = "sJ|MLVVcID3Qc6|~";
	public static final String CRYPT_PASSWORD = "sJ|MLVVcID3Qc6|~";
	public static final String CRYPT_PREFIX = "TnhTI5Az983Akms~AyHtHctE$|F546DLAUuN?RpXm4fLutW*G61Bj#fFw#DDDAnn";
	public static final String characterEncoding = "UTF-8";
	
	String key;

	public AesCrypt() {
		key = AesCrypt.CRYPT_PASSWORD;
	}
	
	private final String cipherTransformation = "AES/CBC/PKCS5Padding";
	private final String aesEncryptionAlgorithm = "AES";

	private  byte[] decrypt(byte[] cipherText, byte[] key, byte [] initialVector) throws Exception
	{
		Cipher cipher = Cipher.getInstance(cipherTransformation);
		SecretKeySpec secretKeySpecy = new SecretKeySpec(key, aesEncryptionAlgorithm);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
		cipherText = cipher.doFinal(cipherText);
		return cipherText;
	}

	private byte[] encrypt(byte[] plainText, byte[] key, byte [] initialVector) throws Exception
	{
		Cipher cipher = Cipher.getInstance(cipherTransformation);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, aesEncryptionAlgorithm);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		plainText = cipher.doFinal(plainText);
		return plainText;
	}

	private byte[] getKeyBytes(String key) throws Exception{
		byte[] keyBytes= new byte[16];
		byte[] parameterKeyBytes= key.getBytes(characterEncoding);
		System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.length, keyBytes.length));
		return keyBytes;
	}

	public byte[] Encrypt(String plainText) throws CryptException{
		try {
			byte[] plainTextbytes = plainText.getBytes(characterEncoding);
			byte[] keyBytes = getKeyBytes(key);
			return encrypt(plainTextbytes,keyBytes, keyBytes);	
		} catch (Exception e) {
			throw new CryptException();
		}	
	}

	public String Decrypt(byte[] cipheredBytes) throws CryptException{
		try {
			byte[] keyBytes = getKeyBytes(key);
			return new String(decrypt(cipheredBytes, keyBytes, keyBytes), characterEncoding);	
		} catch (Exception e) {
			throw new CryptException();
		}
	}
	
	public byte[] toByteArray(InputStream input) throws IOException
	{
	    byte[] buffer = new byte[8192];
	    int bytesRead;
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    while ((bytesRead = input.read(buffer)) != -1)
	    {
	        output.write(buffer, 0, bytesRead);
	    }
	    return output.toByteArray();
	}
	
	public String toString(byte[] cipheredBytes) throws UnsupportedEncodingException{		
	    BASE64Encoder encoder = new BASE64Encoder();    
		return encoder.encodeBuffer(cipheredBytes);	    
	}
	
	public byte[] toByteArray(String input) throws IOException
	{
		BASE64Decoder decoder = new BASE64Decoder();		    
		return decoder.decodeBuffer(input);	   
	}
	
	public static String getHash(byte[] convertme) {
	    MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } 
	    BASE64Encoder encoder = new BASE64Encoder();    
	  	return encoder.encodeBuffer(md.digest(convertme));	  
	}
}
