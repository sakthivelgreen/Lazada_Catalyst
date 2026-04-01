
import com.catalyst.Context;
import com.catalyst.event.EVENT_STATUS;
import com.catalyst.event.EventRequest;
import com.catalyst.event.CatalystEventHandler;
import com.zc.common.ZCProject;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements CatalystEventHandler {

	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	// ── Target: your Advanced I/O function endpoint ──
	private static final String ADVANCED_IO_URL = "https://ecommerceintegration-903799318.development.catalystserverless.com/lazada/webhook";

	@Override
	public EVENT_STATUS handleEvent(EventRequest paramEventRequest, Context paramContext) throws Exception {

		try {
			ZCProject.initProject();

			// 1. Grab the raw signal payload
			Object rawData = paramEventRequest.getRawData();
			String payload = (rawData != null) ? rawData.toString() : "{}";
			LOGGER.log(Level.INFO, "Signal received – raw data: " + payload);

			// 2. Forward the payload to the Advanced I/O function via HTTP POST
			int statusCode = forwardToAdvancedIO(payload);
			LOGGER.log(Level.INFO, "Advanced I/O responded with HTTP " + statusCode);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in signalToIOLazada", e);
		}

		return EVENT_STATUS.SUCCESS;
	}

	/**
	 * Posts the signal payload to the Advanced I/O endpoint and returns the HTTP status.
	 */
	private int forwardToAdvancedIO(String jsonPayload) throws Exception {

		URL url = new URL(ADVANCED_IO_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		try {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "text/plain");
			conn.setConnectTimeout(10_000);  // 10 s connect timeout
			conn.setReadTimeout(30_000);     // 30 s read timeout
			conn.setDoOutput(true);

			JSONObject payLoad = new JSONObject(jsonPayload);

			JSONArray event = payLoad.getJSONArray("event");

			JSONObject data = new JSONObject();
			if(event.length() > 0){
				data = event.getJSONObject(0).getJSONObject("data");
			}
			LOGGER.log(Level.INFO, "Signal received – data: " + data.toString());
			// Write body
			byte[] body = data.getBytes(StandardCharsets.UTF_8);
			conn.setFixedLengthStreamingMode(body.length);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(body);
				os.flush();
			}

			// Read response
			int status = conn.getResponseCode();
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(
							(status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream(),
							StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}
			LOGGER.log(Level.INFO, "Advanced I/O body: " + sb.toString());
			return status;

		} finally {
			conn.disconnect();
		}
	}
}
