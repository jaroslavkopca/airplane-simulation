package cz.cvut.fel.pjv.control;

import cz.cvut.fel.pjv.Simulation;
import cz.cvut.fel.pjv.data.Airplane;
import cz.cvut.fel.pjv.data.Airport;
import cz.cvut.fel.pjv.data.Flight;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static cz.cvut.fel.pjv.Simulation.logger;

/**
 * Represents airport terminal with handling behaviour.
 */
public class Terminal {
    private final Airport airport;
    int id;
    double handleTime;
    boolean isAvailable;

    public Terminal(int id, double handleTime, Airport airport) {
        this.id = id;
        this.handleTime = handleTime;
        this.airport = airport;
        this.isAvailable = true;
    }

    /**
     * @param airplane
     * @param flight   This function simulates airplane at the airport Terminal.
     *                 Will simulate time frame of the handletime depending on the simulation interval.
     *                 Changes Arrival/Departure aiport of the flight.
     *                 Set random departure time and waits until it is time to depart.
     */
    public void HandleAircraft(Airplane airplane, Flight flight) {
        flight.getAirplane().status = "Terminal handling";
        try {
            TimeUnit.MILLISECONDS.sleep((long) ((long) (handleTime * 17) / (60 * Airplane.timeInterval)));
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Terminal handling delay interrupted", e);
            throw new RuntimeException(e);
        }
        flight.changeAirports(flight.getArrivalAirport(), Simulation.randomAirport(flight.getArrivalAirport()));
        flight.setDepartureTime(Simulation.randomNumber(0.1, 1.5, "double"));
        flight.getAirplane().status = "Waiting for time of departure";
        try {
            TimeUnit.MILLISECONDS.sleep((long) ((flight.getDepartureTime() * 17) / (60 * Airplane.timeInterval)));
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Waiting for departure time delay interrupted", e);
            throw new RuntimeException(e);
        }
        this.isAvailable = true;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
