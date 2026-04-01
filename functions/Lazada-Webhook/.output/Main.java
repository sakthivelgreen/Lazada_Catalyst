import java.io.BufferedReader;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.catalyst.advanced.CatalystAdvancedIOHandler;
import com.zc.common.ZCProject;
import com.zc.component.zcql.ZCQL;

public class Main implements CatalystAdvancedIOHandler {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	/** Catalyst Data Store table name */
	private static final String TABLE_NAME = "webhook_logs";

	@Override
	public void runner(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// Initialize Catalyst SDK context
			ZCProject.initProject();

			String uri = request.getRequestURI();
			String method = request.getMethod();
			LOGGER.log(Level.INFO, "Incoming  " + method + " " + uri);

			switch (uri) {
				/* -- Lazada callback / Signal-forwarded webhook -- */
				case "/api/webhook": {

					// 1. Read the request body (works for both Lazada GET/POST & signal POST)
					String body = readBody(request);
					LOGGER.log(Level.INFO, "Webhook body: " + body);

					// 2. Store the payload in the webhook_logs Data Store table
					storeWebhookLog(method, uri, body);

					// 3. Respond immediately so Lazada (or Signal caller) doesn't time-out
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

	/**
	 * Inserts a row into the webhook_logs table with the received payload.
	 *
	 * Expected columns in the "webhook_logs" table:
	 *   - method      (Text)   - HTTP method (GET / POST)
	 *   - uri         (Text)   - Request URI
	 *   - payload     (Text)   - Raw request body / payload
	 *   - received_at (Text)   - ISO-8601 timestamp of when the webhook was received
	 */
	private void storeWebhookLog(String method, String uri, String payload) {
		try {
			// Escape single quotes in the payload to prevent ZCQL injection
			String safePayload = payload.replace("'", "''");
			String safeUri = uri.replace("'", "''");
			String safeMethod = method.replace("'", "''");
			String receivedAt = Instant.now().toString();

			String insertQuery = "INSERT INTO " + TABLE_NAME
					+ " (method, uri, payload, received_at) "
					+ "VALUES ('"
					+ safeMethod + "', '"
					+ safeUri + "', '"
					+ safePayload + "', '"
					+ receivedAt + "')";

			LOGGER.log(Level.INFO, "Inserting webhook log into Data Store...");
			ZCQL zcql = ZCQL.getInstance();
			zcql.executeQuery(insertQuery);
			LOGGER.log(Level.INFO, "Webhook log stored successfully.");

		} catch (Exception e) {
			// Log the error but don't fail the webhook response
			LOGGER.log(Level.SEVERE, "Failed to store webhook log in Data Store", e);
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
			// GET requests or missing body - perfectly fine
		}
		return sb.toString();
	}
}