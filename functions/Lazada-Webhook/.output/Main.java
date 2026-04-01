import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.catalyst.advanced.CatalystAdvancedIOHandler;

public class Main implements CatalystAdvancedIOHandler {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	@Override
    public void runner(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			switch(request.getRequestURI()) {
				case "/api/webhook": {
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("text/plain;charset=UTF-8");

					/* NEW  */
					response.setContentLength(2);          // tell container exactly how big the body is
					response.getWriter().write("OK");
					response.getWriter().flush();
					response.flushBuffer();                // force the bytes onto the wire ASAP
					response.setHeader("Connection", "close");   // ask client-side to drop the socket
					break;	
				}
				default: {
					response.setStatus(404);
					response.getWriter().write("You might find the page you are looking for at \"/\" path");
				}
			}
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Main",e);
			response.setStatus(500);
			response.getWriter().write("Internal server error");
		}
	}
	
}