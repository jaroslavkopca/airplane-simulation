package cz.cvut.fel.pjv.data;

/**
 * Represents a runway.
 */
public class Runway {
    boolean isAvaible;

    public Runway() {
        this.isAvaible = true;
    }

    /**
     * Sets the availability of the runway.
     *
     * @param available true if the runway is available, false otherwise
     */
    public void setAvailabitity(boolean available) {
        isAvaible = available;
    }
}
