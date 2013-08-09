package csmp.common;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

public class Log {
	private static SyslogIF syslog = Syslog.getInstance("udp");
	
	public static void syslog(String str) {
		if ( System.getenv("use_cloudfoundry") == null ) {
			syslog.getConfig().setHost( "192.168.11.200" );
			syslog.getConfig().setPort(514);
		} else {
			syslog.getConfig().setHost( System.getenv("log_host") );
			syslog.getConfig().setPort(Integer.valueOf( System.getenv("log_port") ));	
		}
		syslog.getConfig().setIdent("CDM-CBM");
		syslog.getConfig().setFacility("local0");
		syslog.info(str);
	}
	
	// for test 
	public static void println(String str) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter("/var/log/csmp.log", true);
			bw = new BufferedWriter (fw);
			
			bw.newLine();
			bw.write(str);
		} catch (FileNotFoundException e) {
		} catch (IOException e1) {
		} finally {
			try {
				bw.close();
				fw.close();
			} catch (IOException e) {
				// nothing
			}
		}
	}
	
	public static void printf(String format, Object... args)  {
		System.out.printf(format, args);
	}
}
