package cz.cvut.fel.pjv.data;

import cz.cvut.fel.pjv.map.GPSGrid;
import cz.cvut.fel.pjv.map.WorldMap;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static cz.cvut.fel.pjv.Simulation.logger;

public class Airplane extends Thread {
    private static final double EARTH_RADIUS = 6371; // Earth's radius in kilometers
    private static final double CRASH_RADIUS = 1; // Radius in kilometers to determine a crash
    public static double timeInterval;
    public static double crashRadius;
    public String icao24;
    String callsign;
    String origin_country;
    double longitude;
    double latitude;
    double baro_altitude;
    public boolean on_ground;
    public String status;
    public double velocity;
    double true_track;
    double vertical_rate;
    double geo_altitude;
    public Flight flight;
    WorldMap map;
    int numberofsteps;

    public static boolean started;
    private final GPSGrid gpsGrid;
    private boolean isInCycle;
    public double angle;
    public double time = 0;



    /**
     * Constructs an Airplane object with the specified information and associates it with a flight and a world map.
     *
     * @param icao24         the ICAO24 identifier of the airplane
     * @param callsign       the callsign of the airplane
     * @param origin_country the origin country of the airplane
     * @param longitude      the longitude of the airplane's current position
     * @param latitude       the latitude of the airplane's current position
     * @param baro_altitude  the barometric altitude of the airplane
     * @param on_ground      true if the airplane is on the ground, false otherwise
     * @param velocity       the velocity of the airplane in km/h
     * @param true_track     the true track of the airplane
     * @param vertical_rate  the vertical rate of the airplane
     * @param geo_altitude   the geodetic altitude of the airplane
     * @param flight         the associated flight
     * @param map            the world map
     */
    public Airplane(String icao24, String callsign, String origin_country, double longitude, double latitude, double baro_altitude, boolean on_ground, double velocity, double true_track, double vertical_rate, double geo_altitude, Flight flight, WorldMap map) {
        this.icao24 = icao24;
        this.callsign = callsign;
        this.origin_country = origin_country;
        this.longitude = longitude;
        this.latitude = latitude;
        this.baro_altitude = baro_altitude;
        this.on_ground = on_ground;
        this.velocity = velocity;
        this.true_track = true_track;
        this.vertical_rate = vertical_rate;
        this.geo_altitude = geo_altitude;
        this.flight = flight;
        flight.connectAirplane(this);
        this.map = map;
        started = true;
        this.gpsGrid = WorldMap.grid;
        gpsGrid.addToGrid(this,latitude,longitude);
    }

    /**
     * Runs the airplane thread and performs its behavior.
     */
    public void run() {
        while (isAlive()) {
            while (started) {
                // Fly towards the destination airport
                logger.log(Level.INFO, "Airplane {0}: Flying towards destination airport", icao24);
                flyTo(flight.ArrivalAirport);
                List<Airplane> nearbyAirplanes = getNearbyAirplanes(crashRadius);
                if (!nearbyAirplanes.isEmpty() && !on_ground) {
                    handleCrash(nearbyAirplanes);
                }

                try {
                    Thread.sleep(1000); // Sleep for 0.4 seconds
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Airplane {0}: Thread interrupted", new Object[]{icao24, e});
                    e.printStackTrace();
                }

                int activeThreadCount = Thread.activeCount();

                if (Thread.currentThread().isInterrupted()) {
                    logger.log(Level.WARNING, "Airplane {0}: Removing airplane from map, when thread was interrupted", icao24);
                    map.removeAirplane(this);
                    break;
                }
            }
        }
    }

    /**
     * Makes the airplane fly towards the specified airport.
     *
     * @param port the destination airport
     */
    protected void flyTo(Airport port) {
        logger.log(Level.INFO, "Airplane {0}: Flying to destination airport", icao24);
        status = "flying";
        double distance = calculateDistance(latitude, longitude, port.getLatitude(), port.getLongitude()); //km
        time = distance / (velocity * 3.6); // Calculate estimated time based on speed HOURS
        numberofsteps++;
        double interval = timeInterval; // Interval in hours
        if (time < interval) {
            //When time of arrival is less then simulation interval, the landing phase begins.
            port.tower.handleFlight(this);
            return;
        }
        double deltaLat = (port.getLatitude() - latitude) / (time / interval);
        double deltaLon = (port.getLongitude() - longitude) / (time / interval);

        latitude += deltaLat;
        longitude += deltaLon;

        // Wrap around the longitude if it exceeds the bounds
        if (longitude > 180) {
            longitude = -180 + (longitude - 180);
        } else if (longitude < -180) {
            longitude = 180 - (Math.abs(longitude) - 180);
        }

        // Ensure latitude stays within the bounds
        if (latitude > 90) {
            latitude = 90 - (latitude - 90);
        } else if (latitude < -90) {
            latitude = -90 + (Math.abs(latitude) - 90);
        }

        gpsGrid.updatePosition(this,latitude,longitude);
        if (calculateDistance(latitude, longitude, port.getLatitude(), port.getLongitude()) < 1) {
            land();
        }
    }

    /**
     * Calculates the distance between two points on Earth's surface using the Haversine formula.
     *
     * @param lat1 the latitude of the first point
     * @param lon1 the longitude of the first point
     * @param lat2 the latitude of the second point
     * @param lon2 the longitude of the second point
     * @return the distance between the two points in kilometers
     */
    protected static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * Retrieves the nearby airplanes within the specified radius.
     *
     * @param radius the radius in kilometers
     * @return a list of nearby airplanes
     */
    protected List<Airplane> getNearbyAirplanes(double radius) {
        List<Airplane> nearbyAirplanes = new ArrayList<>();

        nearbyAirplanes = gpsGrid.getAirplanesInNeighboringPositions(gpsGrid.getNeighboringPositions(gpsGrid.getGridPosition(latitude,longitude),radius));


        return nearbyAirplanes;
    }

    /**
     * Handles a potential crash with nearby airplanes.
     *
     * @param nearbyAirplanes the list of nearby airplanes
     */
    protected void handleCrash(List<Airplane> nearbyAirplanes) {
        logger.log(Level.WARNING, "Airplane {0}: Handling potential crash", icao24);
        if(crashRadius >0){
            for (Airplane airplane : nearbyAirplanes) {
                double distance = calculateDistance(latitude, longitude, airplane.getLatitude(), airplane.getLongitude());
                if (distance <= CRASH_RADIUS) {
                    map.addCrash(longitude, latitude);
                    logger.log(Level.WARNING, "Airplane {0}: has crashed to the airplane {1}", new String[]{icao24, airplane.icao24});
                    this.stop();
                }
            }
        }
    }

    /**
     * Checks if the airplane has reached the destination airport.
     *
     * @param airport the destination airport
     * @return true if the airplane has reached the destination airport, false otherwise
     */
    public boolean hasReachedDestination(Airport airport) {
        return gpsGrid.getGridPosition(latitude, longitude) == gpsGrid.getAirportGridPosition(airport);
    }

    /**
     * Gets the latitude of the airplane's current position.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude of the airplane's current position.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the airplane's on_ground status to false, indicating it has landed.
     */
    public void takeOff() {
        on_ground = false;
    }
    /**
     * Sets the airplane's on_ground status to true, indicating it has take off.
     */
    public void land() {
        on_ground = true;
    }


    /**
     * Calculates the estimated arrival time to the specified airport.
     *
     * @param port the destination airport
     * @return the estimated arrival time in hours
     */
    public double getArivalTime(Airport port) {
        double distance = calculateDistance(latitude, longitude, port.getLatitude(), port.getLongitude()); //km
        double time = distance / (velocity * 3.6); // Calculate estimated time based on speed HOURS
        return time;
    }

    /**
     * Performs a waiting cycle around the destination airport.
     */
    public void performWaitingCycle() {
        logger.log(Level.INFO, "Airplane {0}: Performing waiting cycle around airport", icao24);
        // Get the arrival airport of the flight
        Airport arrivalAirport = flight.getArrivalAirport();

        // Define the radius and speed for the waiting cycle
        double waitingRadius = 1.0; // Adjust the radius as needed
        double waitingSpeed = 20.0; // Adjust the speed as needed

        // Calculate the center coordinates of the waiting cycle
        double centerLatitude = arrivalAirport.getLatitude();
        double centerLongitude = arrivalAirport.getLongitude();

        // Calculate the current angle of the airplane in the waiting cycle
        double currentAngle = 0.0;

        // Perform the waiting cycle
        while (flight.getArrivalAirport().getFreeRunway() == null) {
            status = "Flying around airport";
            isInCycle = true;

            // Calculate the new GPS coordinates based on the current angle and radius
            double latitude = centerLatitude + (waitingRadius * Math.cos(Math.toRadians(currentAngle)));
            double longitude = centerLongitude + (waitingRadius * Math.sin(Math.toRadians(currentAngle)));

            // Update the airplane's GPS coordinates
            this.latitude = latitude;
            this.longitude = longitude;
            gpsGrid.updatePosition(this, latitude, longitude);

            // Calculate the tangent angle for rotation
            double tangentAngle = Math.toDegrees(Math.atan2(waitingRadius * Math.cos(Math.toRadians(currentAngle)),
                    waitingRadius * Math.sin(Math.toRadians(currentAngle))));

            // Rotate the airplane in the direction of rotation
            this.angle = tangentAngle;

            // Increment the angle by the speed (adjust based on the desired rotation speed)
            currentAngle += waitingSpeed;

            try {
                Thread.sleep(100); // Sleep for a short duration to simulate the waiting cycle
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isInCycle = false;
    }


    /**
     * Checks if the airplane is currently in a waiting cycle.
     *
     * @return true if the airplane is in a waiting cycle, false otherwise
     */
    public boolean isPerfomingCycle(){
        return isInCycle;
    }

}


