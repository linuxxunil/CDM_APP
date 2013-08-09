package csmp.junit;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import csmp.cbm.SoapClient;


public class SoapClientTest {
	private SoapClient _soapClient;
	private String host = "192.168.11.205";
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
	
	@Test
	public void testPutFile() {
		int expected = 0;
		int result;
		if ( _soapClient.putFile(_smapleSME,file,size) >= 0 ) {
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
