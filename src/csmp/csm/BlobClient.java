package csmp.csm;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
//import sun.net.www.protocol.http.HttpURLConnection;  
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import csmp.common.Log;
import csmp.csm.HttpHandleGet;
import csmp.csm.HttpHandlePut;
import csmp.result.BucketOfSME;

interface ReturnCode {
	static final int OK=0;
	static final int Error=-1;
	static final int IOException=-1;
	static final int FileNotFoundException=-2;
}



class Restful {
	private InetAddress _host;
	private int _port;
	private String _restURL;
	
	Restful(String host,int port) throws UnknownHostException {
		_host = InetAddress.getByName(host);
		_port = port;
	}
	
	Restful(URL url) throws UnknownHostException {
		this(url.getHost(),url.getPort());
	}
	
	
	private String getBaseURL() {
		return String.format("http://%s:%d/",_host.getHostAddress(), _port);
	}

	private void setRestURL(String path) {
		_restURL = getBaseURL() + path;
	}
	
	public String getRestURL() {
		return _restURL;
	}
	
	
	private HttpURLConnection createHTTPConnection(String httpMethod,String path) 
													throws IOException {
		setRestURL(path);
		Log.printf("BlobURL=%s\n", _restURL);

		if ( !(httpMethod.equals("GET") || 
				httpMethod.equals("PUT") || 
				httpMethod.equals("DELETE"))) {
	
			Log.printf("Only provide method has GET/PUT/DELETE ");
			return null;
		}
		
		URL url = new URL(_restURL);
		URLConnection conn = url.openConnection();
		HttpURLConnection httpConn  = (HttpURLConnection)conn;
		
		if ( httpMethod.equals("PUT") && path.split("/").length > 1) 
			httpConn.setChunkedStreamingMode(2048);
	
		httpConn.setRequestMethod(httpMethod);  //POST 的重要參數
		httpConn.setRequestProperty("User-Agent:", "curl/7.22.0") ; 
		httpConn.setRequestProperty("Accept", "*/*") ;

		httpConn.setDoOutput(true); 
		httpConn.setDoInput(true); 
		
		return httpConn;
	}
		
	protected int restPut(String path, HttpHandlePut put) throws IOException {
		HttpURLConnection httpConn = null;
		OutputStream om = null;
		
		try {
			
			httpConn = createHTTPConnection("PUT", path);
			
			if ( put != null ) {
				httpConn.setRequestProperty("Expect","100-continue") ;
			}
		
			httpConn.connect();
		
			if ( put != null ) {
				om = httpConn.getOutputStream();
				put.handleContent(om);
				om.flush();
			}
		
			int ret = httpConn.getResponseCode();
		
			if ( om != null ) {
				om.close();
			}
		
			httpConn.disconnect();
			return ret;
			
		} catch ( IOException e) {
			if ( om != null ) {
				try {
					om.close();
				} catch ( IOException e1) {
					// Log
				}
			}
		
			if ( httpConn != null ) {
				httpConn.disconnect();
			}
			throw e;
		}
	}	

	protected int restDelete(String path) throws IOException {
		HttpURLConnection httpConn = createHTTPConnection("DELETE", path);
		
		try {
			httpConn.connect();
			int ret = httpConn.getResponseCode();
			httpConn.disconnect();
			return ret;
		} catch ( IOException e ) {
			httpConn.disconnect();
			throw e;
		}
	}

	protected int restGet(String path, HttpHandleGet get) throws IOException{
		HttpURLConnection httpConn = createHTTPConnection("GET", path);
		InputStream im = null;
		try {
			httpConn.connect();

			int ret = httpConn.getResponseCode();
			if ( get != null ) {
				im = httpConn.getInputStream();
				get.handleContent(im);
				im.close();
			}
		
			httpConn.disconnect();
			return ret;
		} catch ( IOException e ) {
			if ( im != null ) {
				try {
					im.close();
				} catch ( IOException e1) {
					// Log
				}
			}
			
			if ( httpConn != null ) {
				httpConn.disconnect();
			}
			throw e;
		}
	}

}

public class BlobClient extends Restful {
	public BlobClient(String host,int port) throws UnknownHostException {
		super(host,port);
	}
	
	public BlobClient(URL url) throws UnknownHostException {
		super(url);
	}
	
	public int uploadFile(final String smeID,final String src,final String dst) {
		String urlRecord = String.format("%s/%s", smeID, dst);
		int httpCode = 0;
		final InputStream srcFile;
		InputStream srcFilePoint  = null; 
		
		try {
			srcFile = new FileInputStream(new File(src));
			 srcFilePoint = srcFile;
			
			httpCode = 
				restPut(urlRecord, new HttpHandlePut() {
					@Override
					public void handleContent(OutputStream om) throws IOException  {
						byte[] buf = new byte[512];
						int len;

						while ((len = srcFile.read(buf)) > 0){
							om.write(buf,0,len);
							om.flush();
						}
						
						srcFile.close();
					}
				});
		
			if ( httpCode == HttpURLConnection.HTTP_OK ) {
				return ReturnCode.OK;
			} else {
				return httpCode;
			}
		} catch (FileNotFoundException e1) {
			return ReturnCode.FileNotFoundException;
		}catch (IOException e) {
			if ( srcFilePoint != null ) {
				try {
					srcFilePoint.close();
				} catch ( IOException e1 ) {
					// Log
				}
			}
			
			return ReturnCode.IOException;
		}
	}
	
	public int uploadFile(final String smeID,InputStream src,final long size,final String dst) {
		String urlRecord = String.format("%s/%s", smeID, dst);
		int httpCode = 0;
		final InputStream srcFile;
		InputStream srcFilePoint  = null; 
		
		try {
			srcFile = src;
			 srcFilePoint = srcFile;
			
			httpCode = 
				restPut(urlRecord, new HttpHandlePut() {
					@Override
					public void handleContent(OutputStream om) throws IOException  {
						byte[] buf = new byte[1024];
						int len;
						long _size = size;
						
						while ((len = srcFile.read(buf)) > 0){
	
							if ( _size >= len ) {  
								om.write(buf,0,len);
							} else {
								int _size_int = (int)_size;
								om.write(buf,0,_size_int);
							}
							_size -= len;
						}
						srcFile.close();
		
					}
				});
		
			if ( httpCode == HttpURLConnection.HTTP_OK ) {
				return ReturnCode.OK;
			} else {
				return httpCode;
			}
		} catch (FileNotFoundException e1) {
			return ReturnCode.FileNotFoundException;
		}catch (IOException e) {
			if ( srcFilePoint != null ) {
				try {
					srcFilePoint.close();
				} catch ( IOException e1 ) {
					// Log
				}
			}
			
			return ReturnCode.IOException;
		}
	}
	
	public int deleteFile(final String smeID,final String file) {
		String urlRecord = String.format("%s/%s", smeID, file);
		
		try { 
			int ret = restDelete(urlRecord);
			
			if ( ret == HttpURLConnection.HTTP_OK ||
					ret == HttpURLConnection.HTTP_NO_CONTENT ) {
				return ReturnCode.OK;
			} else {
				return ret;
			}
		} catch (IOException e) {
			// Log
			return ReturnCode.IOException;
		}
	}
	
	public int createSME(final String smeID) {
		try {
			int ret = restPut( smeID, null );
			
			if ( ret == HttpURLConnection.HTTP_OK ) {
				return ReturnCode.OK;
			} else {
				return ret;
			}
		} catch (IOException e) {
			// Log
			return ReturnCode.IOException;
		}
	}
	
	public int createSME(final String smeID,final long smeQuota) {
		
		try {
			int ret = restPut( smeID + "?quota="  + smeQuota, null );
			
			if ( ret == HttpURLConnection.HTTP_OK ) {
				return ReturnCode.OK;
			} else {
				return ret;
			}
		} catch (IOException e) {
			// Log
			return ReturnCode.IOException;
		}
	}
	
	public int listBucketOfSME(final String smeID, final HttpHandleGet get) {
		try {
			
			int ret = restGet( smeID, get );
			
			if ( ret == HttpURLConnection.HTTP_OK ) {
				return ReturnCode.OK;
			} else {
				return ret;
			}
			
		} catch ( IOException e ) {
			// Log
			return ReturnCode.IOException;
		}
	}
	
	public int listBucketOfSME(final String smeID, BucketOfSME result) {
		class Content implements HttpHandleGet {
			BucketOfSME result;
			
			Content(BucketOfSME result) {
				this.result = result;
			}
			
			@Override
			public void handleContent (InputStream im) {
				int len;
				byte[] buf = new byte[1024];
				String xml = "";
				
				try {
					while((len = im.read(buf)) > 0) {
						xml += new String(buf,0,len);
					}
					BlobParser parse = new BlobParser(xml);
					parse.getListBucketResult(result);
	
				} catch (SAXException e) {
					Log.printf("%s",e.getMessage());
				} catch (ParserConfigurationException e) {
					Log.printf("%s",e.getMessage());
				} catch ( IOException e ){
					Log.printf("%s",e.getMessage());
				}
			}
		}
		
		return listBucketOfSME(smeID, new Content(result));
	}
	
	public int deleteSME(final String smeID) {
		try { 
			int ret = restDelete(smeID);
			
			if ( ret == HttpURLConnection.HTTP_OK ||
					ret == HttpURLConnection.HTTP_NO_CONTENT ) {
				return ReturnCode.OK;
			} else {
				return ret;
			}
		} catch (IOException e) {
			// Log
			return ReturnCode.IOException;
		}
	}
	
	public int readFileOfSME(String smeID,String filePath, HttpHandleGet get) {
		try {		
			int ret = restGet( smeID + filePath, get );
			
			if ( ret == HttpURLConnection.HTTP_OK ) {
				return ReturnCode.OK;
			} else {
				return ret;
			}
			
		} catch ( IOException e ) {
			// Log
			return ReturnCode.IOException;
		}
	}
	

	/*
	public static void main(String args[]) {
		
		BlobClient blob = null;
		try {
			blob = new BlobClient("192.168.11.200",9981);
			
		} catch (UnknownHostException e) {
			Log.printf("UnknownHostException");
		}

		// create SME 
		int ret = blob.createSME("jesse");
		System.out.println(ret);
		
		// upload a file to blob
		ret = blob.uploadFile("jesse", "README.txt", "README.txt");
		System.out.println(ret);

		// Read Information of SME
		
		blob.listBucketOfSME("jesse", new HttpHandleGet() {
			@Override
			public void handleContent(InputStream im) throws IOException {
				int len;
				byte[] buf = new byte[1024];
				String str = "";
				while((len = im.read(buf)) > 0) {
					str += new String(buf,0,len);
				}
				System.out.println(str);
			}
		});
		
		
		BucketOfSME result = new BucketOfSME();
		blob.listBucketOfSME("jesse", result );
		
		System.out.println("Code : " + result.code);
		System.out.println("SME-ID : " + result.smeID);
		System.out.println("MaxFiles : " + result.maxFiles);
		System.out.println("Contents-Length :" + result.contents.length);
		for ( int i=0; i<result.contents.length; i++) {
			System.out.println("fileName[" + i + "] :" + result.contents.fileName.get(i));
			System.out.println("lastModified[" + i + "] :" + result.contents.lastModified.get(i));
			System.out.println("lastModified[" + i + "] :" + result.contents.size.get(i));
		}
		
		//blob.readFileOfSME("jesse", "/xxx.txt", new Content() );
		
		// delete a file from blob
		// blob.delete("jesse", "aaa.mp3");
		// blob.deleteSME("jesse"); 
		
	}
	*/
	
}