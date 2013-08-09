package csmp.cbm;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import csmp.common.Log;
import csmp.common.XmlParser;



public class SoapMsgResponse extends XmlParser{

	SoapMsgResponse(String xml) throws ParserConfigurationException, SAXException, IOException {
		super(xml);
	}
	
	SoapMsgResponse(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
		super(xmlFile);
	}
	
	public void getLog() {
		
	}
	
	public int putFile() {
		
		if ( getElementsByTagName("putFileResponse") == null ) {
			return -1;
		}
	
		Node result = getElementsByTagName("result").item(0).getFirstChild();
		Node fileID = getElementsByTagName("FileID").item(0).getFirstChild();
		
		if ( result == null || 
			 fileID == null ||
			 Integer.valueOf(result.getNodeValue()) < 0 ) {
			return -2;
		} else {
			return Integer.valueOf(fileID.getNodeValue());
		}
	}
	
	public int getFile() {
		if ( getElementsByTagName("getFileResponse") == null ) {
			return -1;
		}
		
		Node result = getElementsByTagName("result").item(0).getFirstChild();
		Node fileID = getElementsByTagName("FileID").item(0).getFirstChild();

		if ( result == null || 
			 Integer.valueOf(result.getNodeValue()) < 0 ||
			 fileID == null ) {
			return -2;
		} else {
			return Integer.valueOf(fileID.getNodeValue());
		}
	}
	
	public int deleteFile() {
		if ( getElementsByTagName("deleteFileResponse") == null ) {
			return -1;
		}
		
		Node result = getElementsByTagName("result").item(0).getFirstChild();
		
		if ( result == null || 
			 Integer.valueOf(result.getNodeValue()) < 0 
			) {
			return -2;
		} else {
			return Integer.valueOf(result.getNodeValue());
		}
	}
	/*
	public static void main(String[] args) {
		File f = new File("./root/csgws.putFile.res.xml");
		
		try {
			CDM_SoapMsgResponse response = new CDM_SoapMsgResponse(f);
			//NodeList nl = 
			//NodeList nl2 = nl.item(0).getChildNodes();
			//System.out.println(nl2.item(0).get);
			//System.out.println(nl.item(0).getChildNodes().item(0).getNodeName());
			//Node node = (Node) nl.item(0).;
			//System.out.println(node.get);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	*/
}
