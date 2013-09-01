package csmp.crypt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.productivity.java.syslog4j.util.Base64;
import csmp.common.Convert;


class AES {
	private SecretKeySpec skeySpec;
	private Cipher cipher;
	private Cipher noPadding;
	private Cipher padding;
	private byte[] key;
	
	public AES() throws NoSuchAlgorithmException, NoSuchPaddingException{
		KeyGenerator keyG = KeyGenerator.getInstance("AES");
		keyG.init(128);
		SecretKey secuK = keyG.generateKey();
		this.key = secuK.getEncoded();
		init();
	}
	
	public AES(String key) throws NoSuchAlgorithmException, NoSuchPaddingException{
		this.key = Convert.str2byte(key);
		init();
	}
	
	public AES(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException{
		this.key = key;
		init();
	}
	
	private void init() throws NoSuchAlgorithmException, NoSuchPaddingException{
		this.skeySpec = new SecretKeySpec(this.key, "AES");
		this.noPadding = Cipher.getInstance("AES/ECB/NoPadding");  
		this.padding = Cipher.getInstance("AES/ECB/PKCS5Padding");
		this.cipher = noPadding;
	}
	
	public void setNoPadding() {
		this.cipher = noPadding;
	}
	
	public void setPKCS5Padding() {
		this.cipher = padding;
	}
	
	public  byte[] encrypt( byte[] byteArray, int offset, int len ) 
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		return cipher.doFinal(byteArray, offset, len);
	}
	
	public  byte[] encrypt( byte[] byteArray ) 
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encrypt( byteArray, 0, byteArray.length);
	}
	
	public  byte[] decrypt( byte[] byteArray, int offset, int len )
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		return cipher.doFinal(byteArray, offset, len);
	}	
	
	public  byte[] decrypt( byte[] byteArray ) throws Exception{
		return decrypt( byteArray, 0, byteArray.length);
	}
	
	/*public void encryptFile(FileInputStream fim, FileOutputStream fom) throws Exception {
		 byte[] buffer = new byte[16];
    	 int length = -1;
    	 
         while((length = fim.read(buffer, 0, buffer.length)) != -1) { 
        	 if ( length < buffer.length ) setPKCS5Padding();
        	 else setNoPadding();
        	 
        	 byte[] decoder = encrypt(buffer, 0, length);
        	 fom.write(decoder, 0, decoder.length);
         } 
	}*/
	
	public void encryptFile(InputStream fim, OutputStream fom) 
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		 byte[] buffer = new byte[16];
		 int length = -1;
		 
        while((length = fim.read(buffer, 0, buffer.length)) != -1) { 
        	if ( length < buffer.length ) {
        		setPKCS5Padding();
        	}else {
        		setNoPadding();
        	}
       	 
       	 	byte[] decoder = encrypt(buffer, 0, length);
       	 	
       	 	fom.write(decoder, 0, decoder.length);
        } 
	}
	
	public void decryptFile(InputStream fim, OutputStream fom)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] buffer = new byte[8192];
		int length = -1;
   	 
		while((length = fim.read(buffer,0,buffer.length)) != -1) { 
			if ( length < buffer.length ) setPKCS5Padding();
			else setNoPadding();
			
			byte[] decoder = decrypt(buffer, 0, length);
			fom.write(decoder, 0, decoder.length);
		} 
	}
	

	

	
	public static void demoRandomKey() throws Exception {
		//CDM_AES aes = new CDM_AES();
		//for ( int i=0; i<10 ; i++)
		//System.out.println(Convert.byte2str(aes.produceRandomKey(128)));
		
	}
}

class Demo {
	public static void demoStringCrypt() throws Exception {
		System.out.println("### Demo String Encrypt and Decrypt");
		
		String plainText = "HelloWorld";
		
		// 128 bit
		AES aes = new AES("000102030405060708090A0B0C0D0E0F");
		
		// encode
		if ( (plainText.getBytes().length % 16) == 0) aes.setNoPadding();
		else aes.setPKCS5Padding();
		byte[] encoder = aes.encrypt(plainText.getBytes());
    	
		System.out.println("Encoder: " + Convert.byte2str(encoder));
    	
    	// decode
    	aes.setPKCS5Padding();
    	String decoder = new String(aes.decrypt(encoder));
    	System.out.println("Decoder: " + decoder);
	}
	
	public static void demoFileCrypt()  throws Exception {
		System.out.println("### Demo File Encrypt and Decrypt");
		
		String plainFile = "plainText.txt";
		String encodeFile = "encode.txt";
		String decodeFile = "decode.txt";
		
		FileInputStream plainText = 
				new FileInputStream(new File(plainFile)); 
         
		FileOutputStream encode = 
				new FileOutputStream(new File(encodeFile)); 
	
		AESEncrypt aes = new AESEncrypt();
		
		// encrypt file
		System.out.println(plainFile + " encrypt to " + encodeFile);
		aes.encryptFile(plainText, encode);
		
		FileInputStream cipherText = 
				new FileInputStream(new File(encodeFile)); 
		
		FileOutputStream decode = 
				new FileOutputStream(new File(decodeFile)); 
		
		// decrypt file
		System.out.println(encodeFile + " decrypt to " + decodeFile);
		aes.decryptFile(cipherText, decode);
		

		plainText.close();
		encode.close();
		cipherText.close();
		decode.close();	
	}

	public static void main(String[] args) throws Exception {
		
		Demo.demoStringCrypt();

		Demo.demoFileCrypt();
     }
}

/**
 * 1. Use default key encrypt private key
 * 2. deliver  
 * @author jesse
 *
 */

public class AESEncrypt  {
	private final byte[][] defaultKey = {
			// key1
			{(byte)0x81,(byte)0xae,(byte)0x15,(byte)0x7a,(byte)0x41,(byte)0x5c,(byte)0x3d,(byte)0xfe,
			 (byte)0x67,(byte)0xbb,(byte)0xdb,(byte)0x8a,(byte)0xaa,(byte)0x37,(byte)0x1a,(byte)0x69 
			},
			// key2
			{(byte)0x55,(byte)0xa5,(byte)0x94,(byte)0x64,(byte)0x92,(byte)0x07,(byte)0xcb,(byte)0x0e,
			 (byte)0xb7,(byte)0x03,(byte)0xb6,(byte)0xd1,(byte)0xbd,(byte)0xca,(byte)0x4f,(byte)0x10 
			},
			// key3
			{(byte)0x22,(byte)0xc4,(byte)0xd3,(byte)0x83,(byte)0x86,(byte)0x6c,(byte)0x78,(byte)0xdf,
			 (byte)0x42,(byte)0xcf,(byte)0x08,(byte)0xba,(byte)0x28,(byte)0x5e,(byte)0x7c,(byte)0x12 
			},
			// key4
			{(byte)0x3e,(byte)0x2b,(byte)0x74,(byte)0xf2,(byte)0x26,(byte)0x4e,(byte)0xa8,(byte)0x73,
			 (byte)0x39,(byte)0xb0,(byte)0x28,(byte)0xd1,(byte)0x23,(byte)0xa5,(byte)0x41,(byte)0x38 
			},
			// key5
			{(byte)0xe3,(byte)0x77,(byte)0x53,(byte)0xeb,(byte)0xb2,(byte)0x81,(byte)0xf2,(byte)0x77,
			 (byte)0x3c,(byte)0x79,(byte)0x9d,(byte)0x07,(byte)0xef,(byte)0x66,(byte)0x72,(byte)0x4a 
			},
			// key6
			{(byte)0x05,(byte)0x12,(byte)0xff,(byte)0x08,(byte)0xed,(byte)0xd6,(byte)0x22,(byte)0x45,
			 (byte)0x80,(byte)0x66,(byte)0x84,(byte)0xa9,(byte)0x42,(byte)0x89,(byte)0xe3,(byte)0x1d 
			},
			// key7
			{(byte)0x6e,(byte)0xd0,(byte)0xbc,(byte)0x98,(byte)0x4a,(byte)0xf3,(byte)0x65,(byte)0xaa,
			 (byte)0x81,(byte)0xd8,(byte)0xd5,(byte)0xb8,(byte)0xd9,(byte)0x57,(byte)0x9d,(byte)0xa9 
			},			
			// key8
			{(byte)0x9e,(byte)0x63,(byte)0xa2,(byte)0x8e,(byte)0x84,(byte)0x96,(byte)0x35,(byte)0xe2,
			 (byte)0xb0,(byte)0x14,(byte)0xfe,(byte)0x17,(byte)0xb0,(byte)0x66,(byte)0x16,(byte)0x94 
			},
			// key9
			{(byte)0xea,(byte)0x0f,(byte)0x54,(byte)0x98,(byte)0x13,(byte)0xc4,(byte)0x70,(byte)0x54,
			 (byte)0xab,(byte)0x95,(byte)0x49,(byte)0x1e,(byte)0x5b,(byte)0xab,(byte)0xd1,(byte)0xbb 
			},
			// key10
			{(byte)0x6d,(byte)0x32,(byte)0xbd,(byte)0xc7,(byte)0x12,(byte)0x6e,(byte)0x2c,(byte)0x8d,
			 (byte)0x78,(byte)0x69,(byte)0xa4,(byte)0x03,(byte)0x0e,(byte)0x48,(byte)0xf9,(byte)0x3d
			},
	};
	
	private AES aes;
	private byte[] privateKey;
	private byte[] publicKey;
	private int keyLen;
	private int fileKey; 
	
	public AESEncrypt() 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
			IllegalBlockSizeException, BadPaddingException  {
		this(128);
	}
	
	public AESEncrypt(int bit) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
					IllegalBlockSizeException, BadPaddingException  {
		keyLen = bit / 8;
		privateKey = produceRandomKey(bit);
		
		fileKey = (int)(Math.random()*10);
		// private key is encrypted by default key.
		aes = new AES(defaultKey[fileKey]);
		aes.setNoPadding();
		publicKey = aes.encrypt(privateKey);
		
		aes = new AES(privateKey);	
	}

	public byte[] getPublicKey() {
		return publicKey;
	}
	
	public String getBase64PublicKey() {
		return Base64.encodeBytes(publicKey);
	}
	
	public int getFileKey() {
		return fileKey;
	}
	
	private byte[] produceRandomKey(int bit) {
		int length = bit / 8 ;
		byte[] key = new byte[length];
		
		for ( int i=0; i<length; i++ ) {
			key[i] = (byte)(Math.random()*256);
		}
		
		return key;
	}


	public void encryptFile(InputStream fim, OutputStream fom) 
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException  {
		aes.encryptFile(fim,fom);
	}
	public void decryptFile(InputStream fim, OutputStream fom)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException  {
		aes.encryptFile(fim,fom);
	}
}
