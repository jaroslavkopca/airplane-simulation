package cz.cvut.fel.pjv.control;

import cz.cvut.fel.pjv.data.Airplane;
import cz.cvut.fel.pjv.data.Flight;
import cz.cvut.fel.pjv.data.Runway;


import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static cz.cvut.fel.pjv.Simulation.logger;

public class Tower {
    String iata;
    PriorityQueue<Flight> landingQueue;
    PriorityQueue<Flight> takeoffQueue;

    public Tower(String iata) {
        this.iata = iata;
    }

    /**
     * @param plane Calls inner method in order to handle the whole process of landing/taking off.
     */
    public void handleFlight(Airplane plane) {
        requestLanding(plane.flight);
        requestTakeOff(plane.flight);
    }

    protected void requestLanding(Flight flight) {
        boolean cantLand = true;
        // Gets in the loop until there is free runway.
        while (cantLand) {
            flight.getAirplane().status = "Requesting landing permission";
            Runway runway = flight.getArrivalAirport().getFreeRunway();
            if (runway != null) {
                runway.setAvailabitity(false);
                grantLanding(flight, runway);
                cantLand = false;
            } else {
                //Goes into loop to cycle aroundairplane until its free to land.
                flight.getAirplane().performWaitingCycle();
            }
        }
    }

    protected void requestTakeOff(Flight flight) {
        boolean cantTakeOff = true;
        while (cantTakeOff) {
            flight.getAirplane().status = "Request take off permission";
            Runway runway = flight.getArrivalAirport().getFreeRunway();
            if (runway != null) {
                grantTakeoff(flight, runway);
                cantTakeOff = false;
            }
        }
    }

    protected void grantLanding(Flight flight, Runway runway) {
        flight.getAirplane().status = "Landing";
        //Time simulaiton of the landing.
        try {
            TimeUnit.MILLISECONDS.sleep((long) ((17 * 20) / (60 * Airplane.timeInterval)));
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Landing delay interrupted", e);
            throw new RuntimeException(e);
        }
        runway.setAvailabitity(true);
        //Sets airplane to on ground, handle Terminal behavior.
        flight.landFlight();
    }

    protected void grantTakeoff(Flight flight, Runway runway) {
        flight.getAirplane().status = "Taking off";
        try {
            TimeUnit.MILLISECONDS.sleep((long) ((17 * 15) / (60 * Airplane.timeInterval)));
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Takeoff delay interrupted", e);
            throw new RuntimeException(e);
        }
        flight.getAirplane().takeOff();
        runway.setAvailabitity(true);
    }


}
