package csmp.csm;

import java.io.IOException;
import java.io.InputStream;

public interface HttpHandleGet  {
	void handleContent(InputStream im) throws IOException ;
}