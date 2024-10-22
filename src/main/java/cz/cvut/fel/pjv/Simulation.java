

package cz.cvut.fel.pjv;


import cz.cvut.fel.pjv.data.Airplane;
import cz.cvut.fel.pjv.data.Airport;
import cz.cvut.fel.pjv.data.Flight;
import cz.cvut.fel.pjv.map.WorldMap;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.logging.*;


public class Simulation {
    public static final Logger logger = Logger.getLogger(Simulation.class.getName());
    public static int numOfAirplanes;
    public static int numOfAirports;
    public static boolean running;
    public static String level;
    static List<Airport> airports;

    public static void main(String[] args) {
        WorldMap worldMap = new WorldMap();
        running = true;

        SwingUtilities.invokeLater(() -> {
            // Create the GUI and set up the frame
            worldMap.setUpFrame();
            Thread simulationThread = new Thread(() -> {
                while (true) {
                    if (running) {
                        try {
                            Thread.sleep(50); // Sleep for 0.4 seconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SwingUtilities.invokeLater(() -> {
                            worldMap.repaint(); // Refresh the map
                        });
                    }
                }
            });
            simulationThread.start();
        });
    }

    /*
    * TODO
    * Clickable Airports/Airplanes - showing info
    * Better landing/takingoff - status cheching
    * Error
    * Document code
    * Make GPS as Regular GRid :]]]]]]]]]]]]
    * */


    public static void createAirports(WorldMap worldMap) {
        try {
            FileReader fileReader = new FileReader("src/main/java/cz/cvut/fel/pjv/airport_info.json");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < numOfAirports; i++) {
                JSONArray innerArray = jsonArray.getJSONArray(i);
                if (innerArray.length() > 0) {
                    JSONObject jsonObject = innerArray.getJSONObject(0);
                    if (!jsonObject.isNull("name") && !jsonObject.isNull("position")) {
                        String name = jsonObject.getString("name");
                        String iata = jsonObject.getString("iata");
                        JSONObject position = jsonObject.getJSONObject("position");
                        if (!position.isNull("latitude") && !position.isNull("longitude")) {
                            double latitude = position.getDouble("latitude");
                            double longitude = position.getDouble("longitude");
                            worldMap.addAirport(new Airport(name, iata, longitude, latitude));
                            airports = worldMap.getAirportList();
                        }
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createAirplanes(WorldMap worldMap) {
        try (FileReader fileReader = new FileReader("src/main/java/cz/cvut/fel/pjv/data.json");
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String jsonString = stringBuilder.toString();
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < numOfAirplanes; i++) {
                JSONArray jsonObject = jsonArray.getJSONArray(i);
                if (!jsonObject.isNull(0) && !jsonObject.isNull(1) && !jsonObject.isNull(5)
                        && !jsonObject.isNull(6) && !jsonObject.isNull(7) && !jsonObject.isNull(8)
                        && !jsonObject.isNull(9) && !jsonObject.isNull(10) && !jsonObject.isNull(11)
                        && !jsonObject.isNull(13)) {
                    String icao24 = jsonObject.getString(0);
                    String callsign = jsonObject.getString(1);
                    double longitude = jsonObject.getDouble(5);
                    double latitude = jsonObject.getDouble(6);
                    double baro_altitude = jsonObject.getDouble(7);
                    boolean on_ground = jsonObject.getBoolean(8);
                    double velocity = jsonObject.getDouble(9);
                    double true_track = jsonObject.getDouble(10);
                    double vertical_rate = jsonObject.getDouble(11);
                    double geo_altitude = jsonObject.getDouble(13);
                    List<Airport> airports = worldMap.getAirportList();

                    if (airports.size() >= 2) {
                        Random random = new Random();

                        int index1 = random.nextInt(airports.size());
                        int index2 = random.nextInt(airports.size() - 1);

                        // Adjust the index2 if it is greater than or equal to index1
                        if (index2 >= index1) {
                            index2++;
                        }

                        Airport airport1 = airports.get(index1);
                        Airport airport2 = airports.get(index2);

                        Flight flight = new Flight(airport1, airport2);
                        Airplane plane = new Airplane(icao24, callsign, "", longitude, latitude, baro_altitude, on_ground, velocity, true_track, vertical_rate, geo_altitude, flight, worldMap);
                        worldMap.addAirplane(plane);
                    } else {
                        System.out.println("Not enough airports in the list.");
                    }
                } else {
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Airport randomAirport(Airport port) {
        if (airports.size() >= 2) {
            Random random = new Random();

            // Calculate the total weight based on the number of terminals
            int totalWeight = 0;
            for (Airport airport : airports) {
                totalWeight += airport.getTerminals().size();
            }

            // Generate a random number within the total weight range
            int randomNumber = random.nextInt(totalWeight);

            // Select the airport based on the random number and weighted distribution
            int cumulativeWeight = 0;
            for (Airport airport : airports) {
                cumulativeWeight += airport.getTerminals().size();
                if (randomNumber < cumulativeWeight) {
                    if (!airport.equals(port)) {
                        return airport;
                    }
                }
            }

            return randomAirport(port);
        } else {
            System.out.println("Not enough airports in the list.");
            return null; // or throw an exception indicating the error
        }
    }

    public static double randomNumber(double startingRange, double endingRange, String type) {
        Random random = new Random();

        if (type.equalsIgnoreCase("int")) {
            int range = (int) (endingRange - startingRange + 1);
            return random.nextInt(range) + (int) startingRange;
        } else if (type.equalsIgnoreCase("double")) {
            return random.nextDouble() * (endingRange - startingRange) + startingRange;
        } else {
            throw new IllegalArgumentException("Invalid type. Supported types are 'int' and 'double'.");
        }
    }

    public static double calculateInterval(double behavourTime, double v) {
        if(behavourTime==v){
            return 0;
        }
        if(Math.abs(behavourTime-v)>Airplane.timeInterval){
            return calculateInterval(Math.abs(behavourTime-v),Airplane.timeInterval);
        }else{
            return Math.abs(behavourTime-v);
        }
    }


}


