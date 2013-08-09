<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="csmp.csm.*"%>
<%@ page import="java.net.HttpURLConnection"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.net.URLConnection"%>
<%@ page import="java.net.InetAddress"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="java.io.InputStream"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
	try {
		
		String getParameter = 
			"smeID=" 		+ request.getParameter("smeID")
			+ "&host=" 		+ request.getParameter("host")
			+ "&ftpPort="  	+ request.getParameter("ftpPort")
			+ "&ftpUser="   + request.getParameter("ftpUser")
			+ "&ftpPass="   + request.getParameter("ftpPass")
			+ "&soapPort=" 	+ request.getParameter("soapPort")
			+ "&blobHost=" 	+ request.getParameter("blobHost")
			+ "&blobPort=" 	+ request.getParameter("blobPort");

		URL url = null;
		if ( System.getenv("use_cloudfoundry") == null ) {
			url = new URL("http://localhost:8080/CDM_APP/DoLocalBackup?"+getParameter);
		} else {
			url = new URL("http://cdm.vcap.me/DoLocalBackup?"+getParameter);
		}
		
		
		URLConnection conn = url.openConnection();
		HttpURLConnection urc  = (HttpURLConnection)conn;

		urc.setRequestMethod("GET");  //POST 的重要參數
		urc.setRequestProperty("User-Agent:", "curl/7.22.0") ; 
		urc.setRequestProperty("Accept", "*/*") ;
		urc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") ; 
		urc.setDoOutput(false); 
		urc.setDoInput(true); 
				
		//OutputStream om = urc.getOutputStream();
		//om.write(getParameter.getBytes());
		//om.flush();
	
		int ret = urc.getResponseCode();

		InputStream im = urc.getInputStream();
		
		byte[] buf = new byte[1024];
		int len;
		len = im.read(buf);
		String retHtml = new String(buf);
		
		String[] content = new String(buf).split("\n");
		out.println("&lt;code&gt;"+content[0]+"&lt;/code&gt;<br/>");
		out.println("&lt;status&gt;"+content[1]+"&lt;/status&gt;");
		//out.println();
		
		urc.disconnect();
		//om.close();
	}catch ( Exception e ){
		out.print(e.getMessage());
		System.out.println(e.getMessage());
	}
%>
</body>
</html>