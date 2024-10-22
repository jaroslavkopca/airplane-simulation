package cz.cvut.fel.pjv.data;

import cz.cvut.fel.pjv.Simulation;
import cz.cvut.fel.pjv.control.Terminal;
import cz.cvut.fel.pjv.control.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents airport on the worldmap.
 */
public class Airport {
    public static int numOfRunways;
    public static int numOfTerminals;
    String name;
    double latitude;
    double longitude;
    public Tower tower;
    List<Runway> runways;
    List<Terminal> terminals;


    /**
     * Constructs an Airport object with specified name, IATA code, longitude, and latitude.
     * Randomly generates runways and terminals for the airport.
     *
     * @param name    the name of the airport
     * @param iata    the IATA code of the airport
     * @param longt   the longitude of the airport
     * @param lat     the latitude of the airport
     */
    public Airport(String name, String iata, double longt, double lat) {
        this.name = name;
        this.latitude = lat;
        this.longitude = longt;
        this.tower = new Tower(iata);
        runways = new ArrayList<>();
        terminals = new ArrayList<>();
        for (int i = 0; i < Simulation.randomNumber(4, numOfRunways, "int"); i++) {
            // Code to be executed in each iteration
            this.runways.add(new Runway());
        }
        for (int i = 0; i < Simulation.randomNumber(10, numOfTerminals, "int"); i++) {
            // Code to be executed in each iteration
            this.terminals.add(new Terminal(i, 60, this));
        }
    }


    /**
     * Returns a free runway at the airport, if available.
     *
     * @return a free runway at the airport, or null if no runway is available
     */
    public Runway getFreeRunway() {
        Runway result = null;
        for (Runway runway : runways) {
            if (runway.isAvaible) {
                result = runway;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a free terminal at the airport. Loops until a free terminal becomes available.
     *
     * @return a free terminal at the airport
     */
    public Terminal getFreeTerminal() {
        Terminal terminalIni = null;
        while (terminalIni == null) {
            for (Terminal terminal : terminals) {
                if (terminal.isAvailable()) {
                    terminalIni = terminal;
                    terminalIni.setAvailable(false);
                }
            }
        }
        return terminalIni;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Terminal> getTerminals() {
        return terminals;
    }
}


