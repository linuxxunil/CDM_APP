package csmp.junit;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import csmp.cbm.SoapClient;
import csmp.crypt.AESEncrypt;


public class SoapClientTest {
	private SoapClient _soapClient;
	private String host = "140.116.247.32";
	private int port = 8888;
	private String _smapleSME = "SME-1" ;
	private String file = "README.txt";
	private String size = "1024";
	

	@Before
	public void setUp() throws UnknownHostException {
		_soapClient = new SoapClient(host,port);
	}
	
	@After
	public void tearDown() throws InterruptedException {
		_soapClient = null;
	}
	
	private String getFileKey() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		AESEncrypt aes = new AESEncrypt();
		String key = "";
		int keySite = aes.getFileKey();
		
		if ( keySite < 10) {
			key = "0" +  keySite + "," + aes.getBase64PublicKey();
		}  else {
			key = keySite + "," + aes.getBase64PublicKey();
		}
		return key;
	}
	
	@Test
	public void testPutFile() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		int expected = 0;
		int result;
		if ( _soapClient.putFile(_smapleSME,file,size,getFileKey() ) >= 0 ) {
			result = 0;
		} else {
			result = -1;
		}
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetFile() {
		int expected = 0;
		int result ;
		if ( _soapClient.getFile(_smapleSME,file) >= 0 ) {
			result = 0;
		} else {
			result = -1;
		}
		assertEquals(expected, result);
	}
	
	@Test
	public void testDelFile() {
		int expected = 0;
		int result ;
		if ( _soapClient.delFile(_smapleSME,file) >= 0 ) {
			result = 0;
		} else {
			result = -1;
		}
		assertEquals(expected, result);
	}
	
}
