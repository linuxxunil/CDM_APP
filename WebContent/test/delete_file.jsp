<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="csmp.csm.*"%>
 <%@ page import="csmp.result.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%	
	
	String remote = request.getParameter("remote");
	String smeID = request.getParameter("smeID");
	
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
	BucketOfSME result = new BucketOfSME();
	System.out.println(smeID + " " + remote );

	int ret = blob.deleteFile(smeID, remote);
	
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
	
	
%>
</body>
</html>