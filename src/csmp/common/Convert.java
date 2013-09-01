package csmp.common;

public class Convert {
	
	/**
	 * str2byte : String to Byte
	 * Eg. 00010203 --> 0x00 0x01 0x02 0x03
	 */
	public static byte[] str2byte(String key)
    {
        byte[] b = new byte[key.length()/2];

        for(int i=0, bStepper=0; i<key.length()+2; i+=2)
            if(i !=0)
                b[bStepper++]=((byte)Integer.parseInt((key.charAt(i-2)+""+key.charAt(i-1)), 16));

        return b;
    }
	
	/**
	 * byte2str : Byte to String
	 * Eg. 0x00 0x01 0x02 0x03 --> 00010203
	 */
	public static String byte2str (byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
    	int i;

    	for (i = 0; i < buf.length; i++) {	
    		if (((int) buf[i] & 0xff) < 0x10)
    			strbuf.append("0");
    		strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
    		strbuf.append(" ");
    	}
    	return strbuf.toString();
    }
	
}
