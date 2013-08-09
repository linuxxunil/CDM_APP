package csmp.csm;


import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import csmp.common.Log;
import csmp.common.XmlParser;
import csmp.result.*;


public class BlobParser extends XmlParser{
	protected BlobParser(String xml) throws ParserConfigurationException, SAXException, IOException {
		super(xml);
	}
	
	private String getElemntVal(Element element, String key) {
		return element.getElementsByTagName(key).item(0).getFirstChild().getNodeValue();
	}
	
	public void getListBucketResult(BucketOfSME result)  {	
		NodeList nl;
		Node ret;
		
		if ((nl = getElementsByTagName("ListBucketResult")) 
											== null ) {
				result.code = -1;
				return;
		}
			
		ret = getElementsByTagName("Name").item(0).getFirstChild();
		result.smeID = ret.getNodeValue();
			
		ret = getElementsByTagName("MaxKeys").item(0).getFirstChild();
		result.maxFiles = Integer.valueOf(ret.getNodeValue());
		
		ret = getElementsByTagName("SME_QuotaSize").item(0).getFirstChild();
		result.quotaSize = Long.valueOf(ret.getNodeValue());
		
		ret = getElementsByTagName("SME_UsedSize").item(0).getFirstChild();
		result.usedSize = Long.valueOf(ret.getNodeValue());

		if ((nl = getElementsByTagName("Contents")) 
											== null ) {
			result.contents.length = 0;
			return ;
		}
		
		result.contents.length = nl.getLength();
		
		for ( int i=0; i<result.contents.length; i++) {
			Node nd = nl.item(i);
			if (nd.getNodeType() == Node.ELEMENT_NODE) {
				result.contents.fileName.add( 
						getElemntVal((Element) nd,"Key")) ;
				
				result.contents.lastModified.add( 
						 getElemntVal((Element) nd,"LastModified")) ;
				
				result.contents.size.add( 
						Long.valueOf( getElemntVal((Element) nd,"Size")));	
				
			}
		}
	}
	
	public static void main(String[] args) {
		String xml =
			  "<ListBucketResult>"
			+	"<Name>container1</Name>"
			+	"<Prefix/>"
			+	"<MaxKeys>1000</MaxKeys>"
			+	"<Contents>"
			+		"<Key>file1.xml</Key>"
		    +		"<LastModified>Thu Aug 18 2011 10:28:54 GMT-0700 (PDT)</LastModified>"
		    +		"<ETag>\"9fff58b7a9575dea85e7ca6ddbe31125\"</ETag>"
		    +		"<Size>60421</Size>"
		    +		"<StorageClass>STANDARD</StorageClass>"
		    +	"</Contents>"
			+	"<Contents>"
			+		"<Key>file2.xml</Key>"
		    +		"<LastModified>Thu Aug 18 2011 10:28:54 GMT-0700 (PDT)</LastModified>"
		    +		"<ETag>\"9fff58b7a9575dea85e7ca6ddbe31125\"</ETag>"
		    +		"<Size>60421</Size>"
		    +		"<StorageClass>STANDARD</StorageClass>"
		    +	"</Contents>"
		    +  "</ListBucketResult>";
		String xml2 =
			 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
			+ 	"<Name>jesse</Name>"
			+	"<Prefix></Prefix>"
			+	"<Marker></Marker>"
			+	"<MaxKeys>1000</MaxKeys>"
			+	"<IsTruncated>false</IsTruncated>"
			+	"<Contents>" 
			+		"<Key>test</Key>"
			+		"<LastModified>2013-03-07T06:57:28.000Z</LastModified>"
			+		"<ETag>&quot;d8fa59a0ca75294a60e58f48de7a2c13&quot;</ETag>"
			+		"<Size>217544</Size>"
			+		"<Owner>" 
			+			"<ID>1a2b3c4d5e6f7</ID>" 
			+			"<DisplayName>blob</DisplayName>" 
			+		"</Owner>"
			+		"<StorageClass>STANDARD</StorageClass>" 
			+	"</Contents>"
			+ "</ListBucketResult>";
		
		
		try {
			/* Parser XML1 */
			
			BlobParser ttt = new BlobParser(xml2);
			BucketOfSME tt = new BucketOfSME();
			ttt.getListBucketResult(tt);
			
			System.out.println("Code : " + tt.code);
			System.out.println("SME-ID : " + tt.smeID);
			System.out.println("MaxFiles : " + tt.maxFiles);
			System.out.println("Contents-Length :" + tt.contents.length);
			for ( int i=0; i<tt.contents.length; i++) {
				System.out.println("fileName[" + i + "] :" + tt.contents.fileName.get(i));
				System.out.println("lastModified[" + i + "] :" + tt.contents.lastModified.get(i));
				System.out.println("lastModified[" + i + "] :" + tt.contents.size.get(i));
			}
			//tt.free();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
