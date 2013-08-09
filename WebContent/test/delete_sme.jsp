<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="csmp.csm.*;"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%	
	String sme = request.getParameter("smeID");

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
	int ret = blob.deleteSME(sme);

	if ( ret == 0 )
		out.println("delete [" + sme + "] sucess"); 
	else
		out.println("delete [" + sme + "] fail"); 
%>
</body>
</html>