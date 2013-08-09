<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="csmp.csm.*"%>
<%@page import="csmp.result.*;"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%	
	String smeID = request.getParameter("smeID");
	if ( smeID.equals("")) 
		return ;
	
	String quotaParm =  request.getParameter("quotaSize");
	long quotaSize = 0;
	if ( quotaParm.equals("") ) quotaSize = Long.valueOf(quotaParm);

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
	
	int ret ;
	if ( quotaSize == 0 ) {
		ret = blob.createSME(smeID);
	} else {
		ret = blob.createSME(smeID,quotaSize);
	}
	
	out.print("<table width=\"50%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">");
	if ( ret == 0 ) {
		BucketOfSME result = new BucketOfSME();
		blob.listBucketOfSME( smeID , result );
		
		out.print(
				"<tr><td>Code : </td><td>" + ret + "</td></tr>"
				+ "<tr><td>SME-ID : </td><td>" + smeID + "</td></tr>"
				+ "<tr><td>MaxFiles : </td><td>" + result.maxFiles + "</td></tr>"
				+ "<tr><td>SME_QuotaSize : </td><td>" + result.quotaSize + "</td></tr>"
				+ "<tr><td>SME_UsedSize : </td><td>" + result.usedSize + "</td></tr>");
	} else {
		out.print(
				"<tr><td>Code : </td><td>" + ret + "</td></tr>"
				+ "<tr><td>SME-ID : </td><td>" + smeID + "</td></tr>"
				+ "<tr><td>MaxFiles : </td><td>" + 0  + "</td></tr>"
				+ "<tr><td>SME_QuotaSize : </td><td>" + 0 + "</td></tr>"
				+ "<tr><td>SME_UsedSize : </td><td>" + 0 + "</td></tr>");
	}
	out.print("</table>");

%>
</body>
</html>