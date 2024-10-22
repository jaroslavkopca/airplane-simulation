package cz.cvut.fel.pjv.OpenSky;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;


import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A class that interacts with the OpenSky API to retrieve information about aircraft and flights.
 */
public class OpenSky {

    /**
     * Returns all states of aircraft at the time of calling.
     * @return array of all state vector
     */
    public static JSONArray getAllStates(){
        JSONArray arr = new JSONArray();
        try {
            String url = "https://opensky-network.org/api/states/all";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObj = new JSONObject(response.toString());
            arr = jsonObj.getJSONArray("states");
            return arr;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return arr;
    }

    /**
     * Returns all flights within the specified time range.
     *
     * @param begin the start time in seconds since epoch
     * @param end   the end time in seconds since epoch
     * @return JSONArray containing flight state vectors
     */
    public static JSONArray getAllFlights(int begin, int end) {
        String username = "Cavror";
        String password = "aB67LKjj7UVpAVX";
        JSONArray arr = new JSONArray();
        try {
            String url = "https://opensky-network.org/api/flights/all?begin=" + begin + "&end=" + end;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            // Encode username and password in Base64
            String credentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            // Set the Authorization header with the encoded credentials
            con.setRequestProperty("Authorization", "Basic " + encodedCredentials);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObj = new JSONObject(response.toString());
            arr = jsonObj.getJSONArray("states");
            return arr;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return arr;
    }


    /**
     * Return only states vector that are in the area of square define in GPS atributes
     * @param lamin
     * @param lomin
     * @param lamax
     * @param lomax
     * @return
     */
    public static JSONArray getAllStatesGPS(float lamin,float lomin, float lamax, float lomax){
        JSONArray arr = new JSONArray();
        try {
            String url = "https://opensky-network.org/api/states/all?"+"lamin="+lamin+"&"+"lomin="+lomin+"&"+"lamax="+lamax+"&"+"lamin="+lomax;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObj = new JSONObject(response.toString());
            arr = jsonObj.getJSONArray("states");
            return arr;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return arr;
    }


    /**
     * Return information about arriving flights for an airport by its icao24
     * @param airport
     * @return
     */
    public static JSONArray getFlightsAirportArrival(String airport){
        JSONArray arr = new JSONArray();
        try {
            String url = "https://opensky-network.org/api/flights/arrival?"+"airport="+airport;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObj = new JSONObject(response.toString());
            arr = jsonObj.getJSONArray("states");
            return arr;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return arr;
    }

    /**
     * Return information about departing flights for an airport by its icao24
     * @param airport
     * @return
     */
    public static JSONArray getFlightsAirportDepart(String airport){
            JSONArray arr = new JSONArray();
        try {
            String url = "https://opensky-network.org/api/flights/departure?"+"airport="+airport;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObj = new JSONObject(response.toString());
            arr = jsonObj.getJSONArray("states");
            return arr;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return arr;
    }


    /**
     * Returns airport information indentified by its icao24
     *
     * @param icao24 Airport icao24
     * @return JSONArray with information about the airport
     */
    public static JSONArray getAirportInfoByIcao24(String icao24){
        JSONArray arr = new JSONArray();
        try {
            String url = "https://opensky-network.org/api/airports?icao="+icao24;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObj = new JSONObject(response.toString());
            Iterator<String> keys = jsonObj.keys();
            arr.put(jsonObj);
            return arr;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return arr;
    }


}
