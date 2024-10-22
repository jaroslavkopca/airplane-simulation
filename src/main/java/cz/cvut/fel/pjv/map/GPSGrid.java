package cz.cvut.fel.pjv.map;


import cz.cvut.fel.pjv.data.Airplane;
import cz.cvut.fel.pjv.data.Airport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a grid for managing GPS coordinates and their associated objects.
 */
public class GPSGrid {

    private final double gridCellSize;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;
    private final int numLatitudeLines;
    private final int numLongitudeLines;
    private final Map<Integer, List<Airplane>> grid;
    private final Map<Integer, List<Airport>> airportGrid;
    private List<Airport> airports;

    /**
     * Constructs a GPSGrid object with the specified grid parameters.
     *
     * @param minLatitude
     * @param maxLatitude
     * @param minLongitude
     * @param maxLongitude
     * @param numLatitudeLines
     * @param numLongitudeLines
     */
    public GPSGrid(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude,
                   int numLatitudeLines, int numLongitudeLines) {
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.numLatitudeLines = numLatitudeLines;
        this.numLongitudeLines = numLongitudeLines;
        this.gridCellSize = calculateGridCellSize();
        this.grid = new HashMap<>();
        this.airportGrid = new HashMap<>();
    }

    /**
     * Calculates the size of each grid cell based on the latitude and longitude ranges.
     *
     * @return the size of each grid cell
     */
    private double calculateGridCellSize() {
        double latitudeRange = maxLatitude - minLatitude;
        double longitudeRange = maxLongitude - minLongitude;

        double latitudeGridSize = latitudeRange / numLatitudeLines;
        double longitudeGridSize = longitudeRange / numLongitudeLines;

        // Calculate the grid cell size as the average of latitude and longitude grid sizes
        return (latitudeGridSize + longitudeGridSize) / 2.0;
    }

    /**
     * Adds an airplane to the grid at the specified latitude and longitude.
     *
     * @param airplane  the airplane to add to the grid
     * @param latitude  the latitude of the airplane's position
     * @param longitude the longitude of the airplane's position
     */
    public void addToGrid(Airplane airplane, double latitude, double longitude) {
        // Convert latitude and longitude to grid coordinates
        int x = (int) ((longitude - minLongitude) / gridCellSize);
        int y = (int) ((latitude - minLatitude) / gridCellSize);

        // Get the list of airplanes at the grid position
        int position = x * numLongitudeLines + y;
        List<Airplane> airplanes = grid.getOrDefault(position, new ArrayList<>());

        // Add the airplane to the list
        airplanes.add(airplane);
        grid.put(position, airplanes);
    }

    /**
     * Adds an airport to the grid at its latitude and longitude.
     *
     * @param airport the airport to add to the grid
     */
    public void addToGridAirport(Airport airport) {
        // Convert latitude and longitude to grid coordinates
        int x = (int) ((airport.getLongitude() - minLongitude) / gridCellSize);
        int y = (int) ((airport.getLatitude() - minLatitude) / gridCellSize);

        // Get the list of airplanes at the grid position
        int position = x * numLongitudeLines + y;
        List<Airport> airports = airportGrid.getOrDefault(position, new ArrayList<>());

        // Add the airplane to the list
        airports.add(airport);
        airportGrid.put(position, airports);
    }


    /**
     * Gets the grid position for the specified latitude and longitude.
     *
     * @param latitude  the latitude value
     * @param longitude the longitude value
     * @return the grid position
     */
    public int getGridPosition(double latitude, double longitude) {
        int latitudeIndex = (int) ((latitude - minLatitude) / ((maxLatitude - minLatitude) / numLatitudeLines));
        int longitudeIndex = (int) ((longitude - minLongitude) / ((maxLongitude - minLongitude) / numLongitudeLines));

        return latitudeIndex * numLongitudeLines + longitudeIndex;
    }

    /**
     * Gets the grid position for the specified airport.
     *
     * @param airport the airport
     * @return the grid position
     */
    public int getAirportGridPosition(Airport airport) {
        double latitude = airport.getLatitude();
        double longitude = airport.getLongitude();

        int latitudeIndex = (int) ((latitude - minLatitude) / ((maxLatitude - minLatitude) / numLatitudeLines));
        int longitudeIndex = (int) ((longitude - minLongitude) / ((maxLongitude - minLongitude) / numLongitudeLines));

        return latitudeIndex * numLongitudeLines + longitudeIndex;
    }


    /**
     * Gets the latitude and longitude coordinates for the specified grid position.
     *
     * @param gridPosition the grid position
     * @return an array containing the latitude and longitude coordinates
     */
    public double[] getCoordinates(int gridPosition) {
        int latitudeIndex = gridPosition / numLongitudeLines;
        int longitudeIndex = gridPosition % numLongitudeLines;

        double latitude = minLatitude + latitudeIndex * ((maxLatitude - minLatitude) / numLatitudeLines);
        double longitude = minLongitude + longitudeIndex * ((maxLongitude - minLongitude) / numLongitudeLines);

        return new double[]{latitude, longitude};
    }


    /**
     * Updates the position of an airplane in the grid.
     *
     * @param airplane     the airplane to update
     * @param newLatitude  the new latitude of the airplane's position
     * @param newLongitude the new longitude of the airplane's position
     */
    public void updatePosition(Airplane airplane, double newLatitude, double newLongitude) {
        int oldPosition = getGridPosition(airplane.getLatitude(), airplane.getLongitude());
        int newPosition = getGridPosition(newLatitude, newLongitude);

        // Check if the airplane has moved to a different grid position
        if (oldPosition != newPosition) {
            // Remove the airplane from the old grid position
            List<Airplane> airplanes = grid.get(oldPosition);
            if (airplanes != null) {
                airplanes.remove(airplane);
                if (airplanes.isEmpty()) {
                    grid.remove(oldPosition);
                }
            }

            // Add the airplane to the new grid position
            addToGrid(airplane, newLatitude, newLongitude);
        }
    }


    /**
     * Gets the neighboring grid positions for the specified grid position.
     *
     * @param gridPosition the grid position
     * @return a list of neighboring grid positions
     */
    public List<Integer> getNeighboringPositions(int gridPosition, double radius) {
        List<Integer> neighboringPositions = new ArrayList<>();

        int latitudeIndex = gridPosition / numLongitudeLines;
        int longitudeIndex = gridPosition % numLongitudeLines;

        int radiusInCells = (int) Math.ceil(radius / gridCellSize);

        for (int i = -radiusInCells; i <= radiusInCells; i++) {
            for (int j = -radiusInCells; j <= radiusInCells; j++) {
                int neighborLatitudeIndex = latitudeIndex + i;
                int neighborLongitudeIndex = longitudeIndex + j;

                if (neighborLatitudeIndex >= 0 && neighborLatitudeIndex < numLatitudeLines &&
                        neighborLongitudeIndex >= 0 && neighborLongitudeIndex < numLongitudeLines) {
                    int neighborPosition = neighborLatitudeIndex * numLongitudeLines + neighborLongitudeIndex;
                    neighboringPositions.add(neighborPosition);
                }
            }
        }
        neighboringPositions.add(gridPosition);

        return neighboringPositions;
    }

    /**
     * Gets the airplanes in the neighboring grid positions.
     *
     * @param neighboringPositions the neighboring grid positions
     * @return a list of airplanes in the neighboring grid positions
     */
    public List<Airplane> getAirplanesInNeighboringPositions(List<Integer> neighboringPositions) {
        List<Airplane> airplanesInNeighboringPositions = new ArrayList<>();

        for (int position : neighboringPositions) {
            List<Airplane> airplanes = grid.getOrDefault(position, new ArrayList<>());
            airplanesInNeighboringPositions.addAll(airplanes);
        }

        return airplanesInNeighboringPositions;
    }
}
