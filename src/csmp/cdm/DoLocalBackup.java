package csmp.cdm;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import csmp.cdm.SmeInfo;
import csmp.cdm.SmeExistException;
import csmp.common.Log;


public class DoLocalBackup extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private PrintWriter out;
	private int getSmeInfo(SmeInfo smeInfo,HttpServletRequest req ) {
		
		
		for ( String arg: smeInfo.keys) {
			if ( arg.equals("reqHost") ) {
				smeInfo.setParm("reqHost", req.getRemoteAddr());
				continue;
			}
			
			String parm = req.getParameter(arg);
			
			if ( parm == null || parm.equals("") ) {
				return -1;
			} else {
				smeInfo.setParm(arg, parm);
			}
		}
		return 0;
	}
	
	private void handle(SmeInfo smeInfo) throws SmeExistException{
		try {
			new LocalBackup(smeInfo);
		} catch ( SmeExistException e ) {
			throw e;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
										throws ServletException, IOException {
		out = resp.getWriter();
		
		SmeInfo smeInfo = SmeInfo.create();
	
		if ( getSmeInfo( smeInfo , req ) < 0 ) {
			out.println("Your parameter error");
			clear(smeInfo);
		} else {
			try {
				
				handle(smeInfo);
				out.println("<code>0</code>\n<SME-ID>"+smeInfo.getParm("smeID")+"</SME-ID>");
			
			} catch ( SmeExistException e ) {
				out.println(e.getMessage());
			}
		}
		
		out.close();
	}
	
	/* for test */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
										throws ServletException, IOException {
		/*
		SmeInfo smeInfo = new SmeInfo();
		smeInfo.smeID = req.getParameter("smeID");
		smeInfo.ftpHost = req.getParameter("ftpHost");
		smeInfo.ftpPort = Integer.parseInt(req.getParameter("ftpPort"));
		smeInfo.soapHost = req.getParameter("soapHost");
		smeInfo.soapPort = Integer.parseInt(req.getParameter("soapPort"));
		smeInfo.blobHost = req.getParameter("blobHost");
		smeInfo.blobPort = Integer.parseInt(req.getParameter("blobPort"));
		smeInfo.user = req.getParameter("user");
		smeInfo.pass = req.getParameter("pass");
	
		handle(smeInfo);
		
		PrintWriter out = resp.getWriter();
		out.println("OK");
        out.close();	
        */
	}

	private void clear(Object obj) {
		obj = null;
	}
}
