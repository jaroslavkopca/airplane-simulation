package cz.cvut.fel.pjv.data;

import cz.cvut.fel.pjv.control.Terminal;

import static cz.cvut.fel.pjv.Simulation.logger;

/**
 * Represents a flight with an airplane and its associated information.
 */
public class Flight {
    Airplane airplane;
    String icao24;
    Airport DepartureAirport;
    Airport ArrivalAirport;
    double departureTime;


    /**
     * Constructs a Flight object with the specified departure and arrival airports.
     *
     * @param depAirport the departure airport
     * @param arrAirport the arrival airport
     */
    public Flight(Airport depAirport, Airport arrAirport) {
        this.DepartureAirport = depAirport;
        this.ArrivalAirport = arrAirport;
    }

    /**
     * Connects the airplane to the flight.
     *
     * @param airplane the airplane to connect
     */
    public void connectAirplane(Airplane airplane) {
        this.airplane = airplane;
        this.icao24 = airplane.icao24;
    }

    /**
     * Handles the landing process of the flight.
     * The airplane lands and is handled at an available terminal at the arrival airport.
     */
    public void landFlight() {
        airplane.land();
        Terminal terminal = ArrivalAirport.getFreeTerminal();
        if (terminal != null) {
            terminal.HandleAircraft(airplane, airplane.flight);
        } else {
            logger.warning("No available terminal at the arrival airport: " + ArrivalAirport.getName());
        }
    }

    /**
     * Changes the departure and arrival airports of the flight.
     *
     * @param depAirport the new departure airport
     * @param arrAirport the new arrival airport
     */
    public void changeAirports(Airport depAirport, Airport arrAirport) {
        this.DepartureAirport = depAirport;
        this.ArrivalAirport = arrAirport;
    }

    /**
     * Gets the airplane associated with the flight.
     *
     * @return the airplane
     */
    public Airplane getAirplane() {
        return airplane;
    }

    /**
     * Gets the arrival airport of the flight.
     *
     * @return the arrival airport
     */
    public Airport getArrivalAirport() {
        return ArrivalAirport;
    }

    /**
     * Gets the departure time of the flight.
     *
     * @return the departure time
     */
    public double getDepartureTime() {
        return departureTime;
    }

    /**
     * Sets the departure time of the flight.
     *
     * @param departureTime the departure time to set
     */
    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Gets the departure airport of the flight.
     *
     * @return the departure airport
     */
    public Airport getDepartureAirport() {
        return DepartureAirport;
    }
}

