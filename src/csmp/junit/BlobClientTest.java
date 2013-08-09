package csmp.junit;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import csmp.csm.BlobClient;;

public class BlobClientTest {
	private BlobClient _blobClient;
	String _smapleSME = "SME-1" ;
	String _srcPath = "README.txt";
	String _dstPath = "README.txt";
	
	@Before
	public void setUp() throws UnknownHostException {
		_blobClient = new BlobClient("192.168.11.200",9981);
	}
	
	@After
	public void tearDown() throws InterruptedException {
		_blobClient = null;
	}
	
	@Test
	public void testCreateSME() {
		int expected = 0;
		int result = _blobClient.createSME(_smapleSME);
		assertEquals(expected, result);
	}
	
	@Test
	public void testUploadFile() {
		int expected = 0;
		int result = _blobClient.uploadFile(_smapleSME, _srcPath, _dstPath);
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeleteFile() {
		int expected = 0;
		int result = _blobClient.deleteFile(_smapleSME, _dstPath);
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeleteSME() {
		int expected = 0;
		int result = _blobClient.deleteSME("SME-1");
		assertEquals(expected, result);
	}

}