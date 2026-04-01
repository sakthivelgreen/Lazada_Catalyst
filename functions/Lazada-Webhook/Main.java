import java.io.BufferedReader;
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
			String uri = request.getRequestURI();
			String method = request.getMethod();
			LOGGER.log(Level.INFO, "Incoming  " + method + " " + uri);

			switch (uri) {
				/* ── Lazada callback / Signal-forwarded webhook ── */
				case "/api/webhook": {

					// 1. Read the request body (works for both Lazada GET/POST & signal POST)
					String body = readBody(request);
					LOGGER.log(Level.INFO, "Webhook body: " + body);

					// ────────────────────────────────────────────
					// TODO: Add your Lazada business logic here
					//       e.g. parse order events, update DB, etc.
					// ────────────────────────────────────────────

					// 2. Respond immediately so Lazada (or Signal caller) doesn't time-out
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("text/plain;charset=UTF-8");
					response.setContentLength(2);
					response.getWriter().write("OK");
					response.getWriter().flush();
					response.flushBuffer();
					response.setHeader("Connection", "close");
					break;
				}

				default: {
					response.setStatus(404);
					response.getWriter().write("Not found. Try /api/webhook");
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in Main", e);
			response.setStatus(500);
			response.getWriter().write("Internal server error");
		}
	}

	/** Reads the full request body into a String. Returns "" for GET / empty bodies. */
	private String readBody(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception ignored) {
			// GET requests or missing body – perfectly fine
		}
		return sb.toString();
	}
}