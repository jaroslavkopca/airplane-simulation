package cz.cvut.fel.pjv.map;

import cz.cvut.fel.pjv.Simulation;
import cz.cvut.fel.pjv.data.Airplane;
import cz.cvut.fel.pjv.data.Airport;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class represents a world map panel that displays airplanes and airports on a map.
 */
public class WorldMap extends JPanel {
    private final ImageIcon airplaneImage;
    private final ImageIcon worldMapImage;
    public double mapWidth;   // Width of the map in pixels
    public double mapHeight;  // Height of the map in pixels

    // GPS coordinates for reference
    public double minLongitude;  // Minimum longitude value
    public double maxLongitude;  // Maximum longitude value
    public double minLatitude;   // Minimum latitude value
    public double maxLatitude;   // Maximum latitude value

    private double scale;  // Current scale factor
    private int offsetX;    // X offset for panning
    private int offsetY;    // Y offset for panning

    // Reference point coordinates (zero longitude and latitude) on the image
    double referenceX = 1845;
    double referenceY = 1100;
    boolean started = false;


    private final List<Airplane> AirplaneList;  // List of Airplane objects
    private final List<Airport> airportList;    // List of airport objects
    List<double[]> crashList = new ArrayList<>();
    public static GPSGrid grid;
    private AffineTransform transform;
    private JScrollPane Table;
    private Airplane clickedPlane;

    /**
     * Constructs a new WorldMap object.
     */
    public WorldMap() {
        // Load the world map image
        worldMapImage = new ImageIcon("src/main/java/cz/cvut/fel/pjv/map/worldmapv2.png");
        airplaneImage = new ImageIcon("src/main/java/cz/cvut/fel/pjv/map/airplane.png");

        // Set the map dimensions
        mapWidth = worldMapImage.getIconWidth();
        mapHeight = worldMapImage.getIconHeight();

        // Set the GPS coordinates (adjust these based on your image)
        minLongitude = -180.0;
        maxLongitude = 180.0;
        minLatitude = -90.0;
        maxLatitude = 90.0;

        // Set initial scale and offsets
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;

        //Creates new GPS grid
        grid = new GPSGrid(minLatitude, maxLatitude, minLongitude, maxLongitude, 360, 360);


        AirplaneList = new ArrayList<>();
        airportList = new ArrayList<>();

        // Add the startButton to the panel
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setPreferredSize(new Dimension((int) mapWidth, (int) mapHeight));

        addMouseListener(new MouseAdapter() {
            /**
             * Called when the mouse is clicked.
             * @param e the MouseEvent object
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                int imageWidth = 4500;  // Image width in pixels
                int imageHeight = 2234; // Image height in pixels
                int windowWidth = getWidth();  // Window width in pixels
                int windowHeight = getHeight();  // Window height in pixels

                double scaleFactorX = mapWidth / windowWidth;
                double scaleFactorY = mapHeight / windowHeight;

                double clickX = e.getX() * scaleFactorX;
                double clickY = e.getY() * scaleFactorY;


                for (Airplane airplane : AirplaneList) {
                    double airplaneX = referenceX + (airplane.getLongitude() * (mapWidth / (maxLongitude - minLongitude)));
                    double airplaneY = referenceY - (airplane.getLatitude() * (mapHeight / (maxLatitude - minLatitude)));

//                  Check if the click coordinates are within the bounds of the airplane image
                    if (clickX >= airplaneX && clickX <= (airplaneX + airplaneImage.getIconWidth()) &&
                            clickY >= airplaneY && clickY <= (airplaneY + airplaneImage.getIconHeight())) {
                        showAirplaneInfo(airplane);
                        break;
                    }
                }
            }
        });


        // Add mouse wheel listener for zooming
        addMouseWheelListener(new MouseAdapter() {
            /**
             * Called when mouse wheel moved.
             * @param e the event to be processed
             */
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                double previousScale = scale; // Store the previous scale for comparison

                if (rotation < 0) {
                    // Zoom in
                    double mouseX = e.getX();
                    double mouseY = e.getY();

                    // Calculate the mouse position in the world map coordinates
                    double worldMouseX = (mouseX - offsetX) / scale;
                    double worldMouseY = (mouseY - offsetY) / scale;

                    scale *= 1.1;
                    if (scale > 1.0) {
                        scale = 1.0;
                    }

                    // Adjust the offset based on the new scale and mouse position
                    offsetX = (int) (mouseX - (worldMouseX * scale));
                    offsetY = (int) (mouseY - (worldMouseY * scale));
                } else if (rotation > 0) {
                    // Zoom out
                    double mouseX = e.getX();
                    double mouseY = e.getY();

                    // Calculate the mouse position in the world map coordinates
                    double worldMouseX = (mouseX - offsetX) / scale;
                    double worldMouseY = (mouseY - offsetY) / scale;

                    scale /= 1.1;

                    // Ensure the scale does not go below the default scale
                    double defaultScale = 1;
                    if (scale > 1.0 / defaultScale) {
                        scale = 1.0 / defaultScale;
                    }

                    // Adjust the offset based on the new scale and mouse position
                    offsetX = (int) (mouseX - (worldMouseX * scale));
                    offsetY = (int) (mouseY - (worldMouseY * scale));

                    // Clamp the offset to keep the image within the window bounds
                    clampOffset();
                }

                repaint();
            }

            private void clampOffset() {
                int maxOffsetX = (int) ((mapWidth * scale) - getWidth());
                int maxOffsetY = (int) ((mapHeight * scale) - getHeight());
                offsetX = Math.min(Math.max(offsetX, -maxOffsetX), 0);
                offsetY = Math.min(Math.max(offsetY, -maxOffsetY), 0);
            }
        });


        addMouseMotionListener(new MouseAdapter() {
            private int prevX;
            private int prevY;

            /**
             * Called when mouse dragged
             * @param e event
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = e.getX();
                int currentY = e.getY();

                int deltaX = currentX - prevX;
                int deltaY = currentY - prevY;

                // Adjust the offsets based on the scale
                offsetX += deltaX / scale;
                offsetY += deltaY / scale;

                // Clamp the offset to keep the image within the window bounds
                clampOffset();

                prevX = currentX;
                prevY = currentY;

                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }

            private void clampOffset() {
                int maxOffsetX = (int) ((mapWidth * scale) - getWidth());
                int maxOffsetY = (int) ((mapHeight * scale) - getHeight());
                offsetX = Math.min(Math.max(offsetX, -maxOffsetX), 0);
                offsetY = Math.min(Math.max(offsetY, -maxOffsetY), 0);
            }
        });

    }

    /**
     * Makes Table showing airplane info.
     * @param airplane
     */
    private void showAirplaneInfo(Airplane airplane) {
        String[] columnNames = {"Property", "Value"};
        Object[][] data = {
                {"icao24", airplane.icao24},
                {"Arrival Airport", airplane.flight.getArrivalAirport().getName()},
                {"Departure Airport", airplane.flight.getDepartureAirport().getName()},
                {"Longitude", airplane.getLongitude()},
                {"Latitude", airplane.getLatitude()},
                {"Status", airplane.status},
                {"Time to destination", airplane.velocity}
        };
        JTable table = new JTable(data, columnNames);  // Specify the table data and column names
        table.setRowHeight(60);  // Set row height
        table.setIntercellSpacing(new Dimension(10, 10));  // Set cell spacing

        // Set font for table headers
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 100));

        // Set font for table cells
        table.setFont(new Font("Arial", Font.PLAIN, 60));

        // Set cell borders
        table.setBorder(BorderFactory.createLineBorder(Color.black));
        table.setGridColor(Color.gray);

        int tableWidth = 2000;
        int tableHeight = 600;
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds((int) (mapWidth - tableWidth), (int) (mapHeight - tableHeight), tableWidth, tableHeight);
        clickedPlane = airplane;
        if (Table == null) {
            Table = scrollPane;
            add(Table);
        }
    }

    /**
     * Update table on clicked airplane.
     * @param clickedAirplane
     */
    private void updateTable(Airplane clickedAirplane) {
        JTable table = (JTable) Table.getViewport().getView();
        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Object property = table.getValueAt(i, 0);
            if (property != null) {
                String propertyName = property.toString();
                switch (propertyName) {
                    case "icao24":
                        table.setValueAt(clickedAirplane.icao24, i, 1);
                        break;
                    case "Arrival Airport":
                        table.setValueAt(clickedAirplane.flight.getArrivalAirport().getName(), i, 1);
                        break;
                    case "Departure Airport":
                        table.setValueAt(clickedAirplane.flight.getDepartureAirport().getName(), i, 1);
                        break;
                    case "Longitude":
                        table.setValueAt(clickedAirplane.getLongitude(), i, 1);
                        break;
                    case "Latitude":
                        table.setValueAt(clickedAirplane.getLatitude(), i, 1);
                        break;
                    case "Status":
                        table.setValueAt(clickedAirplane.status, i, 1);
                        break;
                    case "Time to destination":
                        table.setValueAt(clickedAirplane.time, i, 1);
                        break;
                    default:
                        // Handle unknown property
                        break;
                }
            }
        }

        int tableWidth = 2000;
        int tableHeight = 600;
        Table.setBounds((int) (mapWidth - tableWidth), (int) (mapHeight - tableHeight), tableWidth, tableHeight);
        table.repaint();
    }


    /**
     * Sets upt frame window with field to be filled in.
     * Simulation sets depending on the sets parameters.
     */
    public void setUpFrame() {
        JFrame iniframe = new JFrame();
        iniframe.setTitle("Simulation Initialization");
        iniframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        iniframe.setPreferredSize(new Dimension(800, 800));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Number of airplanes field
        JLabel airplanesLabel = new JLabel("Number of Airplanes:");
        JTextField airplanesField = new JTextField(10);
        airplanesField.setText("500");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(airplanesLabel, constraints);
        constraints.gridx = 1;
        panel.add(airplanesField, constraints);

        // Number of airports field
        JLabel airportsLabel = new JLabel("Number of Airports:");
        JTextField airportsField = new JTextField(10);
        airportsField.setText("25");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(airportsLabel, constraints);
        constraints.gridx = 1;
        panel.add(airportsField, constraints);

        // Number of runways field
        JLabel runwaysLabel = new JLabel("Number of Runways: (range 1-x)");
        JTextField runwaysField = new JTextField(10);
        runwaysField.setText("4");
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(runwaysLabel, constraints);
        constraints.gridx = 1;
        panel.add(runwaysField, constraints);


        // Number of terminals field
        JLabel terminalsLabel = new JLabel("Number of Terminals: (range 10-x)");
        JTextField terminalsField = new JTextField(10);
        terminalsField.setText("30");
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(terminalsLabel, constraints);
        constraints.gridx = 1;
        panel.add(terminalsField, constraints);

        // time interval of simulation
        JLabel timeIntervalLabel = new JLabel("Time interval (in hours)");
        JTextField timeIntervalField = new JTextField(10);
        timeIntervalField.setText("0.3");
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(timeIntervalLabel, constraints);
        constraints.gridx = 1;
        panel.add(timeIntervalField, constraints);


        // Loggers
        JLabel loggersLabel = new JLabel("Loggers level");
        JTextField loggersField = new JTextField(10);
        loggersField.setText("SEVERE");
        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(loggersLabel, constraints);
        constraints.gridx = 1;
        panel.add(loggersField, constraints);

        // Checkbox
        JCheckBox checkbox = new JCheckBox("Enable Crashes (Lags a lot)");
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        panel.add(checkbox, constraints);

        // Register an ItemListener to the checkbox
        JTextField crashRadiusField = new JTextField("1", 10);
        JLabel crashRadiusLabel = new JLabel("Crash Radius: (in km)");
        checkbox.addItemListener(e -> {
            if (checkbox.isSelected()) {
                // Checkbox is selected, add the additional panel
                constraints.gridx = 0;
                constraints.gridy = 7;
                panel.add(crashRadiusLabel, constraints);
                constraints.gridy = 8;
                constraints.gridx = 0;
                panel.add(crashRadiusField, constraints);
            } else {
                // Checkbox is deselected, remove the additional panel
                panel.remove(crashRadiusLabel);
                panel.remove(crashRadiusField);
            }
            panel.revalidate();
            panel.repaint();
        });

        // Start button
        JButton startButton = new JButton("Start");
        constraints.gridx = 0;
        constraints.gridy = 9;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(startButton, constraints);
        // Action listener for the start button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform action when the button is clicked
                boolean validInput = true;

                // Validate the number of airplanes
                int numOfAirplanes = 0;
                try {
                    numOfAirplanes = Integer.parseInt(airplanesField.getText());
                    if (numOfAirplanes < 0 || numOfAirplanes > 1000) {
                        validInput = false;
                        airplanesField.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(iniframe,
                                "Number of airplanes should be between 0 and 1000",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    validInput = false;
                    airplanesField.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(iniframe,
                            "Number of airplanes should be an integer",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }

                int numOfAirports = 0;
                try {
                    numOfAirports = Integer.parseInt(airportsField.getText());
                    if (numOfAirports < 0 || numOfAirports > 29) {
                        validInput = false;
                        airportsField.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(iniframe,
                                "Number of airports should be between 0 and 28",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    validInput = false;
                    airportsField.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(iniframe,
                            "Number of airplanes should be an integer",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }

                int numOfRunways = 0;
                try {
                    numOfRunways = Integer.parseInt(runwaysField.getText());
                    if (numOfRunways < 0 || numOfRunways > 5) {
                        validInput = false;
                        runwaysField.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(iniframe,
                                "Number of runways should be between 0 and 4",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    validInput = false;
                    runwaysField.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(iniframe,
                            "Number of runways should be an integer",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }

                int numOfTerminals = 0;
                try {
                    numOfTerminals = Integer.parseInt(terminalsField.getText());
                    if (numOfTerminals < 10 || numOfTerminals > 51) {
                        validInput = false;
                        terminalsField.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(iniframe,
                                "Number of terminals should be between 10 and 50",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    validInput = false;
                    terminalsField.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(iniframe,
                            "Number of terminals should be an integer",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }

                // Validate the logger level
                String loggerLevel = loggersField.getText().trim();
                List<String> validLoggerLevels = Arrays.asList("INFO", "WARNING", "SEVERE", "CONFIG", "FINE", "FINER", "FINEST");

                if (!validLoggerLevels.contains(loggerLevel.toUpperCase())) {
                    validInput = false;
                    loggersField.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(iniframe,
                            "Invalid logger level. Valid options are: INFO, WARNING, SEVERE",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }


                // Validate the number of airplanes
                double timeInterval = 0;
                try {
                    timeInterval = Double.parseDouble(timeIntervalField.getText());
                    if (timeInterval < 0 || timeInterval > 3) {
                        validInput = false;
                        timeIntervalField.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(iniframe,
                                "Time interval should be between 0 and 3",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    validInput = false;
                    timeIntervalField.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(iniframe,
                            "Time interval should be an integer",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }

                double crashRadius = 0;
                if (checkbox.isSelected()) {
                    try {
                        crashRadius = Double.parseDouble(crashRadiusField.getText());
                        if (crashRadius < 0 || crashRadius > 10) {
                            validInput = false;
                            crashRadiusField.setForeground(Color.RED);
                            JOptionPane.showMessageDialog(iniframe,
                                    "Crash radius should be between 1 and 10",
                                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        validInput = false;
                        crashRadiusField.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(iniframe,
                                "Crash radius should be an integer",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }


                // Validate the number of runways
                // (Similarly validate the other fields)

                if (validInput) {
                    // Proceed with the simulation initialization
                    // For example, start the simulation with the provided values
                    iniframe.setVisible(false);
                    simulationSettings(numOfAirplanes, numOfAirports, numOfRunways, numOfTerminals, timeInterval, crashRadius,loggerLevel);
                    startSimFrame();
                    // ...
                }
            }
        });

        // Add focus listeners to reset the text field color when the user edits the field
        airplanesField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                airplanesField.setForeground(Color.BLACK);
            }
        });


        // Add the panel to the frame's content pane
        iniframe.getContentPane().add(panel);

        iniframe.pack();
        iniframe.setLocationRelativeTo(null);
        iniframe.setVisible(true);

    }


    /**
     * Sets simulation parameters depending on the filled values.
     * @param numOfAirplanes
     * @param numOfAirports
     * @param numOfRunways
     * @param numOfTerminals
     * @param timeInterval
     * @param crashRadius
     */
    private void simulationSettings(int numOfAirplanes, int numOfAirports, int numOfRunways, int numOfTerminals, double timeInterval, double crashRadius, String level) {
        Simulation.numOfAirplanes = numOfAirplanes;
        Simulation.numOfAirports = numOfAirports;
        Airport.numOfRunways = numOfRunways;
        Airport.numOfTerminals = numOfTerminals;
        Airplane.timeInterval = timeInterval;
        Airplane.crashRadius = crashRadius;

        // Create a custom filter
        Filter logFilter = new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                // Filter out log records with a level lower than WARNING
                return record.getLevel().intValue() >= Level.parse(level).intValue();
            }
        };

        // Set the filter on the logger
        Simulation.logger.setFilter(logFilter);
    }

    /**
     * Creates the worldmap and simulation window.
     */
    private void startSimFrame() {
        JFrame frame = new JFrame("Simulation");
        frame.setLayout(new BorderLayout(10, 5));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            Simulation.running = true;
            startPlanes();
            JButton stopButton = new JButton("Stop");
            stopButton.addActionListener(ev -> {
                if (stopButton.getText().equals("Stop")) {
//                    setAirplaneStarted(false);
                    System.out.println("Simulation Stopped.");
                    stopButton.setText("Start again");
                } else {
//                    setAirplaneStarted(true);
                    System.out.println("Simulation started");
                    stopButton.setText("Stop");
                }
            });
            buttons.add(stopButton);
            buttons.remove(startButton);
            frame.revalidate();
            frame.repaint();
        });


        buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttons.add(startButton);
        frame.add(buttons, BorderLayout.SOUTH);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        Simulation.createAirports(this);
        Simulation.createAirplanes(this);
        frame.setVisible(true);
    }

    /**
     * Starts all airplane threads and wait until they are all started.
     */
    private void startPlanes() {
        if (started) {
            return;
        }
        started = true;
        List<Thread> threads = new ArrayList<>();
        CyclicBarrier barrier = new CyclicBarrier(AirplaneList.size() + 1); // "+1" for the main thread

        // Create and start all airplane threads
        for (Airplane airplane : AirplaneList) {
            Thread thread = new Thread(() -> {
                try {
                    barrier.await(); // Wait until all airplanes are ready to start
                    airplane.start();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            threads.add(thread);
        }

        // Wait for all airplanes to reach the barrier
        try {
            barrier.await(); // Unblock all airplane threads to start simultaneously
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("All airplanes have started their simulations.");
    }

    /**
     * Sets airplane started true or false
     * @param started
     */
    private void setAirplaneStarted(boolean started) {
        Airplane.started = started;
    }


    /**
     * Adds airplane to the worldmap
     * @param airplane
     */
    public void addAirplane(Airplane airplane) {
        AirplaneList.add(airplane);
        repaint();
    }


    /**
     * @return List of airplanes on the worldmap
     */
    public List<Airplane> getAirplaneList() {
        return AirplaneList;
    }

    /**
     * @return List of airports o nthe worldmap
     */
    public List<Airport> getAirportList() {
        return airportList;
    }


    /**
     * Removes airplane from the world map
     * @param plane
     */
    public void removeAirplane(Airplane plane) {
        AirplaneList.remove(plane);
    }

    /**
     * This function si called on worldmap.repaint(). Actualize simulation info and draw it.
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Calculate the scale factors based on the current size of the panel and the zoom scale
        double scaleX = getWidth() / (mapWidth * scale);
        double scaleY = getHeight() / (mapHeight * scale);

        // Apply the scale transformations
        g2d.scale(scaleX, scaleY);

        // Calculate the translated coordinates for panning
        int translatedOffsetX = (int) (offsetX / scaleX);
        int translatedOffsetY = (int) (offsetY / scaleY);

        // Apply the translation transformations
        g2d.translate(translatedOffsetX, translatedOffsetY);

        // Set the world map image as the background
        g2d.drawImage(worldMapImage.getImage(), 0, 0, (int) mapWidth, (int) mapHeight, this);

        if (clickedPlane != null) {
            updateTable(clickedPlane);
        }


        drawAirports(g2d);
        drawAirplanes(g2d);
        drawCrashes(g2d);
    }


    /**
     * Draw airplanes which are in the air.
     * @param g2d
     */
    private void drawAirplanes(Graphics2D g2d) {
        for (Airplane airplane : AirplaneList) {
            if (airplane.isAlive() && !airplane.on_ground) {
                double airplaneLongitude = airplane.getLongitude();
                double airplaneLatitude = airplane.getLatitude();

                // Calculate the pixel coordinates based on the GPS coordinates and reference point
                double airplaneX = referenceX + (airplaneLongitude * (mapWidth / (maxLongitude - minLongitude)));
                double airplaneY = referenceY - (airplaneLatitude * (mapHeight / (maxLatitude - minLatitude)));

                double arrivalAirportLongitude = airplane.flight.getArrivalAirport().getLongitude();
                double arrivalAirportLatitude = airplane.flight.getArrivalAirport().getLatitude();

                // Calculate the tilting angle (bearing) towards the arrival airport
                double tiltingAngle = calculateTiltingAngle(airplaneLongitude, airplaneLatitude, arrivalAirportLongitude, arrivalAirportLatitude);
                if (airplane.isPerfomingCycle()) {
                    tiltingAngle = 90 - airplane.angle;
                }

                // Load the airplane image and apply rotation
                transform = AffineTransform.getTranslateInstance(airplaneX, airplaneY);
                transform.rotate(Math.toRadians(tiltingAngle), airplaneImage.getIconWidth() / 2, airplaneImage.getIconHeight() / 2);
                g2d.drawImage(airplaneImage.getImage(), transform, null);

            }
        }
    }


    /**
     * Calculates angle for the airplane to be pointing at, aka towards airport.
     * @param airplaneLongitude
     * @param airplaneLatitude
     * @param arrivalAirportLongitude
     * @param arrivalAirportLatitude
     * @return
     */
    private double calculateTiltingAngle(double airplaneLongitude, double airplaneLatitude, double arrivalAirportLongitude, double arrivalAirportLatitude) {
        double deltaX = arrivalAirportLongitude - airplaneLongitude;
        double deltaY = arrivalAirportLatitude - airplaneLatitude;
        double angle = Math.atan2(deltaY, deltaX);
        return 90 - Math.toDegrees(angle);
    }


    /**
     * Draw airports on the map
     * @param g2d
     */
    private void drawAirports(Graphics2D g2d) {
        for (Airport airport : airportList) {
            double airportLongitude = airport.getLongitude();
            double airportLatitude = airport.getLatitude();

            // Calculate the pixel coordinates based on the GPS coordinates and reference point
            double airportX = referenceX + (airportLongitude * (mapWidth / (maxLongitude - minLongitude)));
            double airportY = referenceY - (airportLatitude * (mapHeight / (maxLatitude - minLatitude)));

            // Draw the airport as a blue circle
            int circleRadius = 10;
            g2d.setColor(Color.RED);
            g2d.fill(new Ellipse2D.Double(airportX - circleRadius, airportY - circleRadius, 2 * circleRadius, 2 * circleRadius));

            // Add tooltip to the airport circle
            String airportName = airport.getName();
            String tooltipText = "Airport: " + airportName;
            g2d.setColor(Color.BLACK);
            g2d.drawString(airportName, (int) airportX, (int) airportY);

            // Register the tooltip for the airport circle
            ToolTipManager.sharedInstance().registerComponent(this);
            this.setToolTipText(tooltipText);
        }
    }

    /**
     * Draw crashes on the map.
     * @param g2d
     */
    private void drawCrashes(Graphics2D g2d) {
        for (double[] crash : crashList) {


            // Calculate the pixel coordinates based on the GPS coordinates and reference point
            double crashX = referenceX + (crash[0] * (mapWidth / (maxLongitude - minLongitude)));
            double crashY = referenceY - (crash[1] * (mapHeight / (maxLatitude - minLatitude)));

            // Draw the airport as a blue circle
            int circleRadius = 10;
            g2d.setColor(Color.YELLOW);
            g2d.fill(new Ellipse2D.Double(crashX - circleRadius, crashY - circleRadius, 2 * circleRadius, 2 * circleRadius));


        }
    }


    /**
     * Add crash to the worldmap.
     * @param longitude
     * @param latitude
     */
    public void addCrash(double longitude, double latitude) {
        double[] crash = {longitude, latitude};
        crashList.add(crash);
        repaint();
    }


    /**
     * Adds airport to the worldmap
     * @param airport
     */
    public void addAirport(Airport airport) {
        airportList.add(airport);
        repaint();
    }
}

