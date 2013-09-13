package csmp.cbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.*;

import csmp.common.Log;


class FtpClient {
	FTPClient client = null;
	String host = null;
	int	   port = -1;
	String user = null;
	String pass = null;
	
	FtpClient(String host ,int port, String user, String pass) {
		client = new FTPClient();
		this.host = new String(host);
		this.port = port;
		this.user = new String(user);
		this.pass = new String(pass);
	}
	
	private boolean login() {
		try {
			return client.login(user, pass);
		} catch ( IOException e ) {
			Log.printf("login fail\n");
			return false;
		}
	}
	
	private void setPassive() {
		client.enterLocalPassiveMode();
	}
	
	private void setBinaryMode() {
		try {
			client.setFileType(FTP.BINARY_FILE_TYPE);
			//client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean connect()  {
		try {
			client.connect(host, port);
			return true;
		} catch ( IOException e ) {
			Log.printf("connect fail\n");
			return false;
		}
	}
	
	private boolean logout() {
		try {
			return client.logout();
		} catch (IOException e) {
			Log.printf("logout fail\n");
			return false;
		}
	}

	private boolean disconnect() {
		try {
			client.disconnect();
			return true;
		} catch ( IOException e ) {
			Log.printf("disconnect fail\n");
			return false;
		}
	}
	
	protected boolean storeFile(String remoteFile, InputStream im) {
		boolean ret = false;
		if ( remoteFile == null || im == null ) {
			return false;
		}  else if (!connect() || !login() ) {
			return false;
		}
		
		setPassive();
		setBinaryMode();
		
		try {
			Log.printf("FTP: Put file[%s] to FTP Server[%s:%d]\n",remoteFile,host,port);
			ret = client.storeFile(remoteFile, im);
		} catch (IOException e) {
			Log.printf("disconnect fail\n");
		}

		if (!logout() || !disconnect() ) {
			return false;
		}
		return ret;
	}

	protected boolean storeFiles(String[] remoteFiles,String[] localFiles) {
		if ( remoteFiles == null || localFiles == null ) {
			return false;
		} else if ( remoteFiles.length != localFiles.length ) {
			Log.printf("remote path != local paths\n");
			return false;
		} 
		
		boolean ret = false;
		FileInputStream im = null;
		for (int i=0; i<localFiles.length; i++ ) {
			try {
				im = new FileInputStream(new File(localFiles[i]));
				ret = storeFile(remoteFiles[i], im) ;
				
			} catch (FileNotFoundException e) {
				Log.printf("copy %s to %s fail\n",localFiles[i],remoteFiles[i]);
				return false;
			}  catch (IOException e) {
				Log.printf("copy %s to %s fail\n",localFiles[i],remoteFiles[i]);
				return false;
			} finally {
				try {
					im.close();
					if ( ret == false ) return false;
				} catch (IOException e) {
					// nothing
				}
			}
		}
		
		return true;
	}

	protected boolean retrieveFile(String[] remoteFiles, String[] localFiles) {
		if ( remoteFiles.length != localFiles.length ) {
			Log.printf("remote path != local paths\n");
			return false;
		} else if (!connect() || !login() ) {
			return false;
		}
		setPassive();
		setBinaryMode();
		int i=0;
		
		for ( i=0; i<localFiles.length; i++ ) {
			try {
				File local = new File(localFiles[i]);  
				
				OutputStream is = new FileOutputStream(local);   
                client.retrieveFile(remoteFiles[i], is);  
                is.close();  
			} catch (FileNotFoundException e) {
				Log.printf("get %s to %s fail\n",localFiles[i],remoteFiles[i]);
				return false;
			}  catch (IOException e) {
				Log.printf("get %s to %s fail\n",localFiles[i],remoteFiles[i]);
				return false;
			}
		}
		
		
		if (!logout() || !disconnect() ) {
			return false;
		}
		return true;
	}
}

public class LocalBackupClient extends FtpClient{
	// for test
	public LocalBackupClient() {
		super("140.116.247.32",21,"csmp","csmp");
	}
	
	public LocalBackupClient(String host,int port,String user,String pass) {
		super(host,port,user,pass);
	}
	
	public boolean putFile(String remote, InputStream im) {
		return storeFile(remote,im);
	}
	
	public boolean putFiles(String[] remotes, String[] locals) {
		return storeFiles(remotes,locals);
	}
	
	public boolean putFile(String remote,String local) {
		String[] remotes = {remote};
		String[] locals = {local};
		return putFiles(remotes,locals);
	}
	
	public boolean getFile(String[] remotes, String[] locals) {
		return retrieveFile(remotes,locals);
	}
	
	public boolean getFile(String remote,String local) {
		String[] remotes = {remote};
		String[] locals = {local};
		return retrieveFile(remotes,locals);
	}
	
	/*
	public static void main(String[] args) {
		
		//String[] putRemotes = {"test1","test2"};
		//String[] putlocals = {"./root/test1","./root/test2"};
		//new LocalBackupClient().putFile(putRemotes, putlocals);
		
		
		String[] getRemotes = {"test1","test2"};
		String[] getLocals = {"./root/QQQQ1","./root/QQQQ2"};
		new LocalBackupClient().getFile(getRemotes, getLocals);
	}
	*/
}
