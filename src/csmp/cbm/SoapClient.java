package csmp.cbm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import csmp.cbm.SoapMsgRequest;
import csmp.cbm.SoapMsgResponse;
import csmp.common.Log;

interface SoapHandle {
	void request(OutputStream om) throws IOException;
	void response(InputStream im) throws IOException;;
}

class Soap {
	private String host;
	private int port;
	private String baseURL;

	Soap(String host,int port) {
		
		this.host = host;
		this.port = port;
		baseURL = String.format("http://%s:%d",this.host, this.port);
		Log.printf("Soap host = %s\n", baseURL);
	}
	
	
	private HttpURLConnection newConnection(String httpMethod) 
													throws IOException {
		URL url = new URL(this.baseURL);
		URLConnection conn = url.openConnection();
		HttpURLConnection httpConn  = (HttpURLConnection)conn;
		
		httpConn.setRequestMethod(httpMethod);  //POST 的重要參數
		httpConn.setRequestProperty("Connection", "Keep-Alive" ) ;
		httpConn.setRequestProperty("Cache-Control", "no-cache" ) ;   
		httpConn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", "http://www.w3.org/2003/05/soap-envelope");
		httpConn.setReadTimeout(15000);
		httpConn.setDoOutput(true); 
		httpConn.setDoInput(true); 
		
		return httpConn;
	}

	protected int soapPost(SoapHandle soap) throws IOException{
		
		int httpCode;
		HttpURLConnection httpConn = newConnection("POST");		
		OutputStream om = null;
		InputStream im = null;
		
		httpConn.connect();	
		om = httpConn.getOutputStream();
		soap.request(om);

		httpCode = httpConn.getResponseCode();

		im = httpConn.getInputStream();
		soap.response(im);
		httpConn.disconnect();
		om.close();
		im.close();
		return httpCode;
	}
	
}

public class SoapClient extends Soap {	
	
	static public interface RET {
		static final int OK=0;
		static final int CONN_REFUSED=-1;
		static final int XML_PARSER_ERROR=-2;
	}
	
	public SoapClient(String host,int port){
		super(host,port);
	}
	
	public int putFile(final String smeID,final String fileName,final String fileSize,final String fileKey) {
		class Content implements SoapHandle {
			int result;
			
			public void request(OutputStream om)  throws IOException{
				SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd-hh-mm");
				String msg = SoapMsgRequest.putFile(smeID, fileName, 
							ft.format(new Date()), fileSize, fileKey);
				om.write(msg.getBytes());
			}
			
			public void response(InputStream im) throws IOException {
				int len;
				byte[] buf = new byte[1024];
				String xml = "";
				
				while((len = im.read(buf)) > 0) {
					xml += new String(buf,0,len);
				}	
				
				try {
					SoapMsgResponse response = new SoapMsgResponse(xml);
					result = response.putFile();
				//} catch (ParserConfigurationException | SAXException e) {
				} catch (Exception e) {
					result = RET.XML_PARSER_ERROR;
				}
			}
		}
		
		try {
			Content content = new Content();
			soapPost(content);
			return content.result;
		} catch ( IOException e ) {
			Log.printf("IOException(putFile), %s\n",e.getMessage());
			return RET.CONN_REFUSED;
		}
	}

	public int delFile(String smeID, String fileName) {
		class Content implements SoapHandle {
			final String smeID,fileName;
			int result;
			
			Content(String smeID,String fileName) {
				this.smeID = smeID;
				this.fileName = fileName;
			}
			
			public void request(OutputStream om)  throws IOException{
				String msg = SoapMsgRequest.deleteFile(smeID, fileName); 
				om.write(msg.getBytes());
			}
			
			public void response(InputStream im) throws IOException {
				int len;
				byte[] buf = new byte[1024];
				String xml = "";
				
				while((len = im.read(buf)) > 0) {
					xml += new String(buf,0,len);
				}	
				
				try {
					SoapMsgResponse response = new SoapMsgResponse(xml);
					result = response.deleteFile();
				//} catch (ParserConfigurationException | SAXException e) {
				} catch (Exception e) {
					//Log.printf("%s\n", e.getMessage());
					result = RET.XML_PARSER_ERROR;
				}
			}
		}
		
		try {
			Content content = new Content(smeID, fileName);
			soapPost(content);
			return content.result;
		} catch ( IOException e ) {
			Log.printf("IOException(delFile), %s\n",e.getMessage());
			return RET.CONN_REFUSED;
		}
	}
	
	public int getFile(final String smeID, final String fileName) {
		class Content implements SoapHandle {
			int result;
			
			public void request(OutputStream om)  throws IOException{
				String msg = SoapMsgRequest.getFile(smeID, fileName);
				om.write(msg.getBytes());
			}
			
			public void response(InputStream im) throws IOException {
				int len;
				byte[] buf = new byte[1024];
				String xml = "";
	
				while((len = im.read(buf)) > 0) {
					xml += new String(buf,0,len);
				}	
		
				try {
					SoapMsgResponse response = new SoapMsgResponse(xml);
					result = response.getFile();
				} catch (Exception e) {
					//Log.printf("%s\n", e.getMessage());
					result =  RET.XML_PARSER_ERROR;
				}
			}
		}
		
		try {
			Content content = new Content();
			soapPost(content);
			return content.result;
		} catch ( IOException e ) {
			Log.printf("IOException(getFile), %s\n",e.getMessage());
			return RET.CONN_REFUSED;
		}
	}
	/*
	public static void main (String[] args) {
		
		SoapClient client = new SoapClient("140.116.247.32",8888);
		
		Log.printf("getFile %d\n",client.getFile("0", "test"));
		Log.printf("putFile %d\n", client.putFile("0", "test"));
		Log.printf("delFile %d\n",client.delFile("0", "test"));
		//Log.printf("putFile %d\n", client.putFile("0", "test"));
	}
	*/
}
	
	