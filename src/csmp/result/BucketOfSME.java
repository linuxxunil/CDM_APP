package csmp.result;

import java.util.ArrayList;

public class BucketOfSME extends Result {
	public class Contents {
		public int length;
		public ArrayList<String> fileName;
		public ArrayList<String> lastModified;
		public ArrayList<Long> size;
	}
	
	/* */
	public String smeID = null;
	public int maxFiles;
	public Contents contents = null;
	public long quotaSize;
	public long usedSize;
	
	public BucketOfSME() {
		contents = new Contents();
		contents.fileName = new ArrayList<String>();
		contents.lastModified = new ArrayList<String>();
		contents.size = new ArrayList<Long>();
	}

	@Override
	public void free() {
		smeID = null;
		maxFiles = 0;
		quotaSize = 0;
		usedSize = 0;
		contents = null;
		contents.fileName = null;
		contents.lastModified = null;		
		contents.size = null;	
	}	
}
