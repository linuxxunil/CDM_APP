package csmp.csm;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpHandlePut {
	void handleContent(OutputStream om) throws IOException ;
}
