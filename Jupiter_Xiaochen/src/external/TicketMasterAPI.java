package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "VQVT1NmY4fF0akh40jfQI1Mx69H3AhiV";
	
	public JSONArray search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		//Encode the keyword in url since it may contain special characters
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Convert the lat/lon into geo hash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		//Generate the url query by concatenating segments
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);
		try {
			//open a HTTP connection between the java application and ticketMaster based on URL
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			
			//set request method t0 "GET"
			connection.setRequestMethod("GET");
			//send request to ticketMaster and get response, response code should be returned directly
			//response code is saved in inputStream of connection
			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);
			System.out.println("Response code: " + responseCode);
			
			//read response body to get events data
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONObject obj = new JSONObject(response.toString());
			if(obj.isNull("_embedded")) {
				return new JSONArray();
			}
			JSONObject embedded = obj.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return events;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void queryAPI(double lat, double lon) {
		JSONArray events = search(lat, lon, null);
		try {
			for (int i = 0; i < events.length(); i++) {
				JSONObject event = events.getJSONObject(i);
				System.out.println(event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		tmAPI.queryAPI(37.38, -122.08);
		tmAPI.queryAPI(51.503364, -0.12);
		tmAPI.queryAPI(29.682684, -95.295410);
	}
}
