package csmp.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

 public class XmlParser {
	private String xml;
	private File xmlFile;
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	protected Document doc;
	private boolean enableNS = false;
	
	protected XmlParser(String xml) throws ParserConfigurationException, SAXException, IOException {
		this.xml = new String(xml);
		
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		
		doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		doc.getDocumentElement().normalize();
	}
	
	protected XmlParser(File xmlFile) throws ParserConfigurationException, SAXException, IOException {	
		this.xmlFile = xmlFile;
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		doc = builder.parse(xmlFile);
	}
	
	protected void enableNamesapce() {
		enableNS = true;
		factory.setNamespaceAware(true);
	}
	
	protected void disableNamespace() {
		enableNS = false;
		factory.setNamespaceAware(false);
	}
		
	protected NodeList getElementsByTagName(String tagName) {
		return doc.getElementsByTagName(tagName);
	}
	
	protected NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		if ( enableNS == false ) {
			Log.printf("please enable namespace\n");
			return null;
		}
		return doc.getElementsByTagNameNS(namespaceURI, localName);
	}

 }