package csmp.cbm;

import csmp.common.Log;

public class SoapMsgRequest {
	private static String soapMsg = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+	"<SOAP-ENV:Envelope "
		+	"xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
		+ 	"xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" "
		+ 	"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
		+ 	"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
		+ 	"xmlns:ns=\"urn:csgws\">"
		+ 	"<SOAP-ENV:Body>"
		+		"%s"	
		+	"</SOAP-ENV:Body>"
		+ "</SOAP-ENV:Envelope>";
	
	private static String getSoapMsg(String body) {
		return String.format(soapMsg, body);
	}
	
	public static String getLog(String smeID) {
		String body =
		  "<ns:getLog>"					
		+	"<getLog>" 					
		+		"<SME-ID>%s</SME-ID>"	
		+	"</getLog>"					
		+ "</ns:getLog>";
			
		String msg = String.format(getSoapMsg(body), body, smeID);
		//Log.printf("%s\n",msg);
		return msg;
	}
	
	public static String putFile(String smeID, String fileName, String date, String fileSize) {
		String body = 
		  "<ns:putFile>"					
		+	"<putFile>" 					
		+		"<SME-ID>%s</SME-ID>"		
		+		"<FileName>%s</FileName>"	
		+		"<FileDate>%s</FileDate>"
		+		"<FileSize>%s</FileSize>"	
		+	"</putFile>"					
		+ "</ns:putFile>"					;
						
		String msg = String.format(getSoapMsg(body), smeID, fileName, date, fileSize);
		//Log.printf("%s\n",msg);
		return msg;
	}
	
	public static String getFile(String smeID, String fileName) {
		String body = 
		  "<ns:getFile>"					
		+	"<getFile>" 					
		+		"<SME-ID>%s</SME-ID>"			
		+	   	"<FileName>%s</FileName>"		
		+	"</getFile>"					
		+ "</ns:getFile>"					;
		
		String msg = String.format(getSoapMsg(body), smeID, fileName);
		//Log.printf("%s\n",msg);
		return msg;
	}
	
	public static String deleteFile(String smeID, String fileName) {
		String body = 
		  "<ns:deleteFile>"					
		+	"<deleteFile>" 				
		+		"<SME-ID>%s</SME-ID>"			
		+		"<FileName>%s</FileName>"		
		+	"</deleteFile>"					
		+ "</ns:deleteFile>"					;
		
		String msg = String.format(getSoapMsg(body), smeID, fileName);
		//Log.printf("%s\n",msg);
		return msg;
	}
}
