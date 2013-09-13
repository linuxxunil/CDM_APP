package csmp.junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import csmp.cbm.LocalBackupClient;

public class LocalBackupTest {
	private LocalBackupClient _localBackupClient ;
	private String host = "140.116.247.32";
	private int port = 21;
	private String user = "csmp";
	private String pass = "csmp";
	private String putSrc = "plainText.ppt";
	private String putDst = "plainText.ppt";
	private String getSrc = "README.txt";
	private String getDst = "README.txt.bak";
	
	@Before
	public void setUp() throws UnknownHostException {
		_localBackupClient = new LocalBackupClient(host,port,user,pass);
	}
	
	@After
	public void tearDown() throws InterruptedException {
		_localBackupClient = null;
	}
	
	@Test
	public void testPutFile() {
		boolean result = _localBackupClient.putFile(putDst, putSrc);
			System.out.println(result);
			assertTrue(result);
	}
	@Test
	public void testGetFile() {
		boolean result = _localBackupClient.getFile(getSrc,getDst);
		assertTrue(result);
	}
}
