<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@page import="csmp.csm.*"%>
 <%@ page import="csmp.result.*"%>

<% 
class HtmlMultiPart {
	private String boundry = null;
	public int position = 0;
	
	public void setBoundry(String boundry) {
		this.boundry = new String("--"+boundry);
	}
	
	public long getFileSize(long total) {
		return total - position - boundry.length() - "--\r\n".length() -2;
	}
	
	public void test(ServletInputStream sim) {
		byte[] buffer = new byte[1024];
		int len = 0;
		String line;
		try {
		while ( (len= sim.readLine(buffer, 0, 1024)) > 0 ) {
			line = new String(buffer,0,len);
			System.out.print(line);
		}
		} catch(Exception e ) {
			System.out.println(e.getMessage());
		}
	}
	public String getMultiPartParameter(ServletInputStream sim,String name) {
		byte[] buffer = new byte[1024];
		int len = 0;
		String line;
		boolean hasPart = false;
		String[] lineSplit;
		String data = null;
		try {
			while ( (len= sim.readLine(buffer, 0, 1024)) > 0 ) {
				position += len;
				line = new String(buffer,0,len);
				if ( hasPart == true ) {
					
					if ( line.trim().endsWith(boundry) == true ) {
						
						return data;
					} else if ( line.trim().equals("") == false) {
						data = new String(line.substring(0,line.length()-2));
					}
					
				} else {
					if ( line != null ) {
						lineSplit = null;
						lineSplit = line.split(";");
						if ( lineSplit != null 
						&&	lineSplit[0].trim().equals("Content-Disposition: form-data") == true
					 	&&  lineSplit[1].trim().equals("name=\""+name+"\"") == true ) {
							hasPart = true;
						}
					}
				}
			}
		} catch (Exception e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	public ServletInputStream getMultiPartSite(ServletInputStream sim,String name) {
		byte[] buffer = new byte[1024];
		int len = 0;
		String line;

		String[] lineSplit;
		boolean hasPart  = false;
		try {
			while ( (len= sim.readLine(buffer, 0, 1024)) > 0 ) {
				position += len;
				line = new String(buffer,0,len);
				
				if ( hasPart == true ) {
					if ( line.equals("\r\n") == true ) {
						return sim;	
					}
				} else if ( line != null ) {
					lineSplit = null;
					lineSplit = line.split(";");
					if ( lineSplit != null 
					&&	lineSplit[0].trim().equals("Content-Disposition: form-data") == true
					&&  lineSplit[1].trim().equals("name=\""+name+"\"") == true ) {
						hasPart = true;
					}
				}
				
			}
		} catch (Exception e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>Insert title here</title>
</head>
<body>
<%
	if ( request == null ) {
		return;
	} 

	String[] content = null;
	try {
		content = request.getContentType().split("=");
	} catch ( Exception e ) {
		return;
	}
			
	HtmlMultiPart htmlPart = new HtmlMultiPart();
	if ( content != null && 
		content[0].equals("multipart/form-data; boundary") ) {
		htmlPart.setBoundry(content[1]);
		
		ServletInputStream sim = request.getInputStream();

		String smeID = htmlPart.getMultiPartParameter(sim, "smeID");
		
		String remote = htmlPart.getMultiPartParameter(sim, "remote");
		
		sim =htmlPart.getMultiPartSite( sim,"lcoal");
		
		long filesize= htmlPart.getFileSize(request.getContentLength());
		System.out.println(filesize);
		
		
		String host = null;
		int port = 0;
		
		if ( System.getenv("use_cloudfoundry") == null ) {
			host = "192.168.11.200";
			port = 9981;
		} else {
			host = System.getenv("vblob_host");
			port = Integer.valueOf(System.getenv("vblob_port"));
		}
		
		BlobClient blob = new BlobClient(host,port);		
		int ret = blob.uploadFile(smeID, sim, filesize , remote);
		BucketOfSME result = new BucketOfSME();
		
		Thread.sleep(2000);
		
		blob.listBucketOfSME(smeID, result );
		
		String html =
		   	  "<h2>基本資料</h2>"
		   	+ "<table width=\"50%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">"
			+ "<tr><td>Code : </td><td>" + ret + "</td></tr>"
			+ "<tr><td>SME-ID : </td><td>" + result.smeID + "</td></tr>"
			+ "<tr><td>MaxFiles : </td><td>" + result.maxFiles + "</td></tr>"
			+ "<tr><td>SME_QuotaSize : </td><td>" + result.quotaSize + "</td></tr>"
			+ "<tr><td>SME_UsedSize : </td><td>" + result.usedSize + "</td></tr>"
			+ "<tr><td>Contents-Length : </td><td>" + result.contents.length + "</td></tr>"
			+ "</table><br/>"
			+ "<h2>雲端硬碟</h2>"
			+ "<table width=\"50%\" border=\"2\" cellpadding=\"5\" cellspacing=\"0\">"
			+ "<tr><th>檔案名稱</th><th>檔案大小</th><th>最後修改</th></tr>";
		
		out.print(html);  
		for ( int i=0; i<result.contents.length; i++) {
			out.print("<tr><th>"+ result.contents.fileName.get(i) +"</th>"
				    + "<th>"+ result.contents.size.get(i) +"</th>"
					+ "<th>"+ result.contents.lastModified.get(i)+"</th></tr>");
		}
		out.print("</table>");
	
		sim.close();
		
	} else {
		out.println("not multipart/form-data;");
	}
%>
</body>
</html>