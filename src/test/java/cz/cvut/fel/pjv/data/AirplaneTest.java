package cz.cvut.fel.pjv.data;

import cz.cvut.fel.pjv.Simulation;
import cz.cvut.fel.pjv.control.Tower;
import cz.cvut.fel.pjv.data.Airplane;

import cz.cvut.fel.pjv.map.GPSGrid;
import cz.cvut.fel.pjv.map.WorldMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AirplaneTest {
    @Mock
    private Airport destinationAirport;

    @Mock
    private Tower tower;

    @Mock
    private WorldMap worldMap;

    @Mock
    private GPSGrid grid;

    @Mock
    private Flight flight;
    private Airplane airplane;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        destinationAirport.tower = tower;
        WorldMap.grid = grid;
        airplane = new Airplane("icao24", "callsign", "origin_country",
                0.0, 0.0, 0.0, false, 100.0, 0.0, 0.0, 0.0, flight, worldMap);
        grid.addToGrid(airplane,0,0);
    }

    @Test
    public void testFlyTo() {
        // Create an instance of the Airplane class
        airplane = new Airplane("icao24", "callsign", "origin_country",
                0.0, 0.0, 0.0, false, 100.0, 0.0, 0.0, 0.0, flight, worldMap);

        // Set up the mock behavior
        when(destinationAirport.getLatitude()).thenReturn(10.0);
        when(destinationAirport.getLongitude()).thenReturn(10.0);
        when(grid.getAirportGridPosition(destinationAirport)).thenReturn(1);
        when(grid.getGridPosition(anyDouble(), anyDouble())).thenReturn(0);

        // Call the flyTo function
        airplane.flyTo(destinationAirport);

        // Verify that the expected methods were called
        verify(grid).addToGrid(airplane, 0, 0);
        verify(grid).updatePosition(airplane,airplane.latitude,airplane.longitude);


        // Create an instance of the Airplane class
        Airplane airplane_landing = new Airplane("icao24", "callsign", "origin_country",
                10.0334069, 9.9685706, 0.0, false, 100.0, 0.0, 0.0, 0.0, flight, worldMap);

        // Set up the mock behavior
        Airplane.timeInterval = 0.3;

        airplane_landing.flyTo(destinationAirport);

        verify(tower).handleFlight(airplane_landing);
        verify(grid).addToGrid(airplane, airplane.latitude,airplane.longitude);
        verify(grid).updatePosition(airplane,airplane.latitude,airplane.longitude);

    }


    @Test
    public void testCalculateDistance() {


    }

    @Test
    public void testGetNearbyAirplanes() {
        Airplane nearbyAirplane = new Airplane("ICAO456", "Nearby", "Origin", 0.1, 0.1, 0, false, 0, 0, 0, 0, flight, worldMap);
        worldMap.addAirplane(nearbyAirplane);
        int radius = 10;
        airplane.getNearbyAirplanes(radius);

        verify(grid).getGridPosition(airplane.latitude,airplane.longitude);
        when(grid.getGridPosition(airplane.latitude,airplane.longitude)).thenReturn(Integer.valueOf("0"));

        int gridPosition = grid.getGridPosition(airplane.latitude,airplane.longitude);
        verify(grid).getNeighboringPositions(gridPosition,radius);

        List<Integer> neighboringPositions = new ArrayList<>();
        neighboringPositions.add(0);
        neighboringPositions.add(1);
        when(grid.getNeighboringPositions(gridPosition,radius)).thenReturn(neighboringPositions);
        verify(grid).getAirplanesInNeighboringPositions(grid.getNeighboringPositions(gridPosition,radius));
    }

    @Test
    public void testHandleCrash() {
        Airplane nearbyAirplane = new Airplane("ICAO456", "Nearby", "Origin", 0.01, 0.01, 0, false, 0, 0, 0, 0, flight, worldMap);
        worldMap.addAirplane(nearbyAirplane);

    }

    @Test
    public void testTakeOff() {
        airplane.takeOff();
        assertFalse(airplane.on_ground);
    }

    @Test
    public void testLand() {
        airplane.land();
        assertTrue(airplane.on_ground);
    }

    @Test
    public void testPerformWaitingCycle() {
        Airport.numOfRunways= 5;
        Airport.numOfTerminals = 11;
        Airport airport = new Airport("Letiste","PES",0,0);
        for (Runway runway: airport.runways
             ) {
            runway.setAvailabitity(false);
        }

        when(flight.getArrivalAirport()).thenReturn(airport);




    }
}
