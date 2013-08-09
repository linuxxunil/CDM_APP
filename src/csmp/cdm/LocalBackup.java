package csmp.cdm;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.LinkedList;

import csmp.cbm.LocalBackupClient;
import csmp.cbm.SoapClient;
import csmp.common.Log;
import csmp.csm.BlobClient;
import csmp.csm.HttpHandleGet;
import csmp.result.BucketOfSME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class SmeInfo {
	private Hashtable<String,String> hash = null;
	
	static final public String[] keys = { "smeID","host","ftpPort",
			"ftpUser","ftpPass","soapPort", "reqHost" };
	
	static final private String[] _keys = {"blobHost" , "blobPort"}; 

	private SmeInfo() {
		hash = new Hashtable<String,String>();
		setParm( _keys[0] , System.getenv("vblob_host"));
		setParm( _keys[1] , System.getenv("vblob_port"));
	}
	
	static public SmeInfo create() {
		return new SmeInfo();
	}
	
	private boolean _isKey(String[] keys,String k) {
		for ( String key : keys) {
			if ( key.equals(k) ) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isKey(String k) {
		if ( _isKey(keys,k) || _isKey(_keys,k)) {
			return true;
		}
		return false;
	}
 	
	public boolean setParm(String key,String value) {
		if ( isKey(key) ) {
			hash.put(key, value);
			return true;
		} 
		return false;
	}

	public String getParm(String key) {
		if ( isKey(key) ) {
			return hash.get(key); 
		} 
		return null;
	}

	public boolean equals(SmeInfo smeInfo) {
		// don't compare blob field 
		for (String key : smeInfo.keys ) {
			if ( !getParm(key).equals(smeInfo.getParm(key))) {
				return false;
			}
		}
		return true;
	}
	
 	public void listParm() {
		for ( String key : _keys ) {
			Log.printf("%s=%s\n",key,getParm(key));
		}
		
		for ( String key : keys ) {
			Log.printf("%s=%s\n",key,getParm(key));
		}
	}

}

class SmeList {	
	private String table = "SME_LIST";
	
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	
	static public enum RET {
		OK , SME_EXIST , SME_NOT_EXIST , AGAIN ,FAIL
	}
	
	private Connection getConnection() throws SQLException, ClassNotFoundException  {
		// Load the JDBC driver
		Class.forName("com.mysql.jdbc.Driver");
		String mysqlHost = null;
		int mysqlPort = 0;
		String dbName = null;

		if ( System.getenv("use_cloudfoundry") == null ) {
			mysqlHost = "192.168.11.200";
			mysqlPort = 3306;
			dbName = "cbm";
		} else {
			mysqlHost = System.getenv("mysql_host");
			mysqlPort = Integer.valueOf( System.getenv("mysql_port") );
			dbName = System.getenv("dbname");
		}

		String url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbName;
		String username = "cbm";
		String password = "1qazxcv";
		return DriverManager.getConnection(url, username, password);
	} 
	
	private void init_database() throws SQLException, ClassNotFoundException  {
		conn = getConnection();
		stmt = conn.createStatement();
	}
	
	private void set_env() throws SQLException {
		stmt.executeQuery("SET NAMES 'utf8'");
	}
	
	private void lock_table(String table) throws SQLException {
		stmt.execute("LOCK TABLE "+ table +" WRITE");
	}
	
	private void unlock_table() throws SQLException {
		stmt.execute("UNLOCK TABLES");
	}
	
	public RET add(SmeInfo smeInfo) {
		RET ret = null;
		try {
			init_database();
			
			set_env();

			lock_table(table);

			// select * form SME_LIST where smeID = 'jesse' and host = '192.168.11.1' and status = 'running'
			String sql = "select * from " + table + " where smeID='" + smeInfo.getParm("smeID") + "' and " +
						 "host='" + smeInfo.getParm("host") + "' and (status='running' or status='again')";
			
			rs = stmt.executeQuery(sql);

			if ( rs.next() ) { // SME backup
				if ( rs.getString("status").equals("running") ) {
					// update SME_LIST set status='again' where smeID='jesse' and status='running'
					sql = "update " + table + " set status='again' where smeID=\'" + smeInfo.getParm("smeID") + "\' and status='running'"; 
					stmt.executeUpdate(sql);
				}
				sql = "insert into " + table + " (smeID,host,status) values (\'"  + smeInfo.getParm("smeID") + 
						"\',\'" + smeInfo.getParm("host") + "\', \'finish\')";

				if ( stmt.executeUpdate(sql) == 1) {
					ret = RET.SME_EXIST;
				} else {
					ret = RET.FAIL;
				}
			} else {
				// insert into SME_LIST (smeID, host, status) values ('jesse0','140.116.247.32','again')
				sql = "insert into " + table + " (smeID,host,status) values (\'"  + smeInfo.getParm("smeID") + 
						"\',\'" + smeInfo.getParm("host") + "\', \'running\')"; 
				if ( stmt.executeUpdate(sql) == 1) {
					ret = RET.OK;
				} else {
					ret = RET.FAIL;
				}
			}
		} catch (ClassNotFoundException e1) {
			Log.printf("JDBC Driver Error\n");
		} catch( SQLException e2 ) {
			Log.printf("SQL Exception(add)\n");
		} finally {
			try {
				unlock_table();
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				// nothing
			}
		}
		
		return ret;
	}
	
	public RET del(SmeInfo smeInfo) {
		RET ret = null;
		try {
			init_database();
			set_env();
			lock_table(table);

			// select * form SME_LIST where smeID = 'jesse' and host = '192.168.11.1' and status = 'running'
			String sql = "select * from " + table + " where smeID='" + smeInfo.getParm("smeID") + "' and " +
									 "host='" + smeInfo.getParm("host") + "' and status='again'";
		
			rs = stmt.executeQuery(sql);
			
			if ( rs.next() ) { // if status is again
				sql = "update " + table + " set status='running' where smeID='" + smeInfo.getParm("smeID") + "' and status='again'"; 
				if ( stmt.executeUpdate(sql) == 1 ) {
					ret = RET.AGAIN;
				} else {
					ret = RET.FAIL;
				}
			} else { // if status is running
				sql = "update " + table + " set status='finish' where smeID='" + smeInfo.getParm("smeID") + "' and status='running'"; 
				if ( stmt.executeUpdate(sql) == 1 ) {
					ret = RET.OK;
				} else {
					ret = RET.FAIL;
				}
			}
			
		} catch (ClassNotFoundException e1) {
			Log.printf("JDBC Driver Error");
		} catch( SQLException e2 ) {
			Log.printf("SQL Exception(del)\n");
			ret = RET.FAIL;
		} finally {
			try {
				unlock_table();
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				// nothing
			}
		}
		return ret;
	}

}


class SmeExistException extends Exception {
	SmeExistException() {
		super("<code>1</code>\n<stats>Backup already is running</status>\n");
	}
}

public class LocalBackup implements Runnable {
	private SmeInfo smeInfo = null; 
	
	LocalBackup(SmeInfo smeInfo) throws SmeExistException {
		SmeList smeList = new SmeList();
		
		if ( smeList.add(smeInfo) == SmeList.RET.OK ) {
			this.smeInfo = smeInfo;
			start();
		} else {
			throw new SmeExistException();
		}
	}
	
	private void backupLog(String msg) {
		String log_msg = "{\"SrcIP\": \""	+ smeInfo.getParm("reqHost") + "\","
				+  "\"DstIP\": \""	+ smeInfo.getParm("host") + "\","
				+  "\"SmeID\": \""	+ smeInfo.getParm("smeID") + "\","
				+  "\"Msg\": \"" + msg	+ "\"}";
		
		Log.syslog(log_msg);
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		backupLog("Start backup to CSG");
		do {
			BlobClient blobClient = null;

			try {
				blobClient = new BlobClient(smeInfo.getParm("blobHost") ,
							Integer.valueOf(smeInfo.getParm("blobPort")));
			} catch (UnknownHostException e) {
				//Log
			}
		
			final BucketOfSME bucketOfSME = new BucketOfSME();
		
			final LocalBackupClient  localBackupClient = 
					new LocalBackupClient(smeInfo.getParm("host"),
						Integer.valueOf(smeInfo.getParm("ftpPort")),
										smeInfo.getParm("ftpUser"),
										smeInfo.getParm("ftpPass"));
		
			final SoapClient soapClient = new SoapClient(smeInfo.getParm("host"),
										Integer.valueOf(smeInfo.getParm("soapPort")));
		
		
			int ret = blobClient.listBucketOfSME( smeInfo.getParm("smeID") , bucketOfSME );
			System.out.println(ret);
		
		
			for ( int i=0; i<bucketOfSME.contents.length; i++ ) {
			
				final String fileName = bucketOfSME.contents.fileName.get(i);
				final String fileSize = String.valueOf(bucketOfSME.contents.size.get(i));
			
				// From blob send data to local 
				blobClient.readFileOfSME(
						smeInfo.getParm("smeID"),
						"/" + fileName, 			
						new HttpHandleGet() {
							int ret;
							@Override
							public void handleContent(InputStream im) {
								// PUT Soap to Soap server 
								if ((ret = soapClient.getFile(smeInfo.getParm("smeID"), fileName)) >= 0 ) {
									Log.printf("File[%s] already exsit\n",fileName);
								} else if ( ret ==  SoapClient.RET.CONN_REFUSED ) { 
									Log.printf("Connect to Soap Server fail\n");
								} else if ( !localBackupClient.putFile(fileName, im)) {
									Log.printf("Put File Command to Backup Storage fail\n");
								} else if ( soapClient.putFile(smeInfo.getParm("smeID"), fileName,fileSize) < 0) {
									Log.printf("Put Soap Commad to Soap Server fail\n");
								} else {
									Log.printf("finish\n");
								}
							}
						}
					);
			}
			
		} while (new SmeList().del(smeInfo) == SmeList.RET.AGAIN );
		backupLog("Finish backup to CSG");
	}
}





/* Memory version
public class LocalBackup implements Runnable {

	static private class SmeList extends LinkedList {
		// Override
		public int indexOf(Object obj) {
			SmeInfo dst = (SmeInfo)obj;
			Iterator itr = iterator();
			
			int i=0;
			while ( itr.hasNext() ) {
				SmeInfo src = (SmeInfo) get(i);
				if ( src.equals(dst)){
					return i;
				}
				itr.next();
				i++;
			}
			return -1;
		}
	}
	
	static private SmeList smeList = new SmeList(); 

	private SmeInfo  smeInfo = null; 
	
	static public enum RET {
		OK , SME_EXIST , SME_NOT_EXIST , 
	}
	
	static private synchronized RET add(SmeInfo smeInfo) {
		if ( smeList.indexOf(smeInfo) == -1) {
			smeList.add(smeInfo);
		} else {
			return RET.SME_EXIST;
		}
		return RET.OK;
	}
	
	static private synchronized RET del(SmeInfo smeInfo) {
		System.out.println("delete");
		int index;
		if ( (index = smeList.indexOf(smeInfo)) == -1 ) {
			return RET.SME_NOT_EXIST;
		} else {
			System.out.println("index =" + index);
			smeList.remove(index);
		}
		return RET.OK;
	}
	

	
	LocalBackup(SmeInfo smeInfo) throws SmeExistException{
		if ( add(smeInfo) == RET.OK ) {
			this.smeInfo = smeInfo;
			start();
		} else {
			throw new SmeExistException();
		}
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		BlobClient blobClient = null;

		try {
			blobClient = new BlobClient(smeInfo.getParm("blobHost") ,
								Integer.valueOf(smeInfo.getParm("blobPort")));
		} catch (UnknownHostException e) {
			//Log
		}
		
		final BucketOfSME bucketOfSME = new BucketOfSME();
		
		final LocalBackupClient  localBackupClient = 
				new LocalBackupClient(smeInfo.getParm("host"),
						Integer.valueOf(smeInfo.getParm("ftpPort")),
										smeInfo.getParm("ftpUser"),
										smeInfo.getParm("ftpPass"));
		
		final SoapClient soapClient = new SoapClient(smeInfo.getParm("host"),
										Integer.valueOf(smeInfo.getParm("soapPort")));
		
		
		int ret = blobClient.listBucketOfSME( smeInfo.getParm("smeID") , bucketOfSME );
		System.out.println(ret);
		
		
		for ( int i=0; i<bucketOfSME.contents.length; i++ ) {
			
			final String fileName = bucketOfSME.contents.fileName.get(i);
			final String fileSize = String.valueOf(bucketOfSME.contents.size.get(i));
			
			// From blob send data to local 
			blobClient.readFileOfSME(
				smeInfo.getParm("smeID"),
				"/" + fileName, 			
				new HttpHandleGet() {
					int ret;
					@Override
					public void handleContent(InputStream im) {
						// PUT Soap to Soap server 
						if ((ret = soapClient.getFile(smeInfo.getParm("smeID"), fileName)) >= 0 ) {
							Log.printf("File[%s] already exsit\n",fileName);
						} else if ( ret ==  SoapClient.RET.CONN_REFUSED ) { 
							Log.printf("Connect to Soap Server fail\n");
						} else if ( !localBackupClient.putFile(fileName, im)) {
							Log.printf("Put File Command to Backup Storage fail\n");
						} else if ( soapClient.putFile(smeInfo.getParm("smeID"), fileName,fileSize) < 0) {
							Log.printf("Put Soap Commad to Soap Server fail\n");
						} else {
							Log.printf("finish\n");
						}
					}
				}
			);
		}
		
		del(smeInfo);
	}
}
*/