package cz.cvut.fel.pjv.data;

import cz.cvut.fel.pjv.OpenSky.OpenSky;
import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;


/**
 * Creates files of data that are used in the simulation. Gets API calls and save the results in the json file.
 */
public class Creation {

    // This class exist only as a creation tool for .json data files in order to save time and API calls in my simulation
    public static void main(String[] args) {
        OpenSky opensky = new OpenSky();



//    Creation of data.json file which gives information about aprox. 10k airplanes
//        JSONArray jsonArray = opensky.getAllStates();
//        String jsonString = jsonArray.toString();
//
//        try (FileWriter fileWriter = new FileWriter("C:\\Users\\jarek\\IdeaProjects\\Semestralka-PJV\\Semestralka-kod\\kopcajar_semestralka\\src\\main\\java\\cz\\cvut\\fel\\pjv\\data.json")) {
//            fileWriter.write(jsonString);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }




//    Creation of flightdata.json file
        JSONArray jsonArrayF = OpenSky.getAllFlights(1621978800,1621993715);
        String jsonStringF = jsonArrayF.toString();

        try (FileWriter fileWriter = new FileWriter("C:\\Users\\jarek\\IdeaProjects\\Semestralka-PJV\\Semestralka-kod\\kopcajar_semestralka\\src\\main\\java\\cz\\cvut\\fel\\pjv\\flightdata.json")) {
            fileWriter.write(jsonStringF);
        } catch (IOException e) {
            e.printStackTrace();
        }



/*
    Creation of airport_info.json which holds information about a lot of world aiports

        List<String> icao24Codes = new ArrayList<>();

        // Call the function for each ICAO24 code and add it to the array
        icao24Codes.add("KATL"); // Hartsfield-Jackson Atlanta International Airport, United States
        icao24Codes.add("EHAM"); // Amsterdam Airport Schiphol, Netherlands
        icao24Codes.add("EDDF"); // Frankfurt Airport, Germany
        icao24Codes.add("RJAA"); // Narita International Airport, Japan
        icao24Codes.add("LFML"); // Marseille Provence Airport, France
        icao24Codes.add("LEMD"); // Adolfo Suárez Madrid–Barajas Airport, Spain
        icao24Codes.add("LSZH"); // Zurich Airport, Switzerland
        icao24Codes.add("VIDP"); // Indira Gandhi International Airport, India
        icao24Codes.add("VABB"); // Chhatrapati Shivaji Maharaj International Airport, India
        icao24Codes.add("WSSS"); // Singapore Changi Airport, Singapore
        icao24Codes.add("NZAA"); // Auckland Airport, New Zealand
        icao24Codes.add("CYYZ"); // Toronto Pearson International Airport, Canada
        icao24Codes.add("SBGR"); // São Paulo-Guarulhos International Airport, Brazil
        icao24Codes.add("SCEL"); // Comodoro Arturo Merino Benítez International Airport, Chile
        icao24Codes.add("OTHH"); // Hamad International Airport, Qatar
        icao24Codes.add("EHAM"); // Amsterdam Airport Schiphol, Netherlands
        icao24Codes.add("RJBB"); // Kansai International Airport, Japan
        icao24Codes.add("WIII"); // Soekarno-Hatta International Airport, Indonesia
        icao24Codes.add("RKSI"); // Incheon International Airport, South Korea
        icao24Codes.add("VIDP"); // Indira Gandhi International Airport, India
        icao24Codes.add("VOBL"); // Kempegowda International Airport, India
        icao24Codes.add("ZBAA"); // Beijing Capital International Airport, China
        icao24Codes.add("ZSPD"); // Shanghai Pudong International Airport, China
        icao24Codes.add("VABB"); // Chhatrapati Shivaji Maharaj International Airport, India
        icao24Codes.add("RKSI"); // Incheon International Airport, South Korea
        icao24Codes.add("OTHH"); // Hamad International Airport, Qatar
        icao24Codes.add("CYYZ"); // Toronto Pearson International Airport, Canada
        icao24Codes.add("SBGR"); // São Paulo-Guarulhos International Airport, Brazil
        icao24Codes.add("EDDF"); // Frankfurt Airport, Germany
        icao24Codes.add("KATL"); // Hartsfield-Jackson Atlanta International Airport, United States
        icao24Codes.add("LFPG"); // Paris Charles de Gaulle Airport, France
        icao24Codes.add("RJAA"); // Narita International Airport, Japan
        icao24Codes.add("LFML"); // Marseille Provence Airport, France
        icao24Codes.add("LEMD"); // Adolfo Suárez Madrid–Barajas Airport, Spain
        icao24Codes.add("LSZH"); // Zurich Airport, Switzerland
        icao24Codes.add("VOMM"); // Chennai International Airport, India
        icao24Codes.add("VOHS"); // Rajiv Gandhi International Airport, India
        icao24Codes.add("HECA"); // Cairo International Airport, Egypt
        icao24Codes.add("VABB"); // Chhatrapati Shivaji Maharaj International Airport, India
        icao24Codes.add("FACT"); // Cape Town International Airport, South Africa
        icao24Codes.add("OMDB"); // Dubai International Airport, United Arab Emirates
        icao24Codes.add("VHHH"); // Hong Kong International Airport, Hong Kong
        icao24Codes.add("VTBS"); // Suvarnabhumi Airport, Thailand
        icao24Codes.add("OMAA"); // Abu Dhabi International Airport, United Arab Emirates
        icao24Codes.add("WIII"); // Soekarno-Hatta International Airport, Indonesia
        icao24Codes.add("RJTT"); // Tokyo Haneda Airport, Japan
        icao24Codes.add("LFPO"); // Paris Orly Airport, France
        icao24Codes.add("LEBL"); // Barcelona-El Prat Airport, Spain
        icao24Codes.add("EDDM"); // Munich Airport, Germany
        icao24Codes.add("EDDB"); // Berlin Brandenburg Airport, Germany
        icao24Codes.add("UUEE"); // Sheremetyevo International Airport, Russia
        icao24Codes.add("EGKK"); // London Gatwick Airport, United Kingdom
        icao24Codes.add("EDDT"); // Berlin Tegel Airport, Germany
        icao24Codes.add("ZGSZ"); // Shenzhen Bao'an International Airport, China
        icao24Codes.add("RJCC"); // New Chitose Airport, Japan
        icao24Codes.add("CYYC"); // Calgary International Airport, Canada
        icao24Codes.add("VTSP"); // Phuket International Airport, Thailand
        icao24Codes.add("VDSR"); // Siem Reap International Airport, Cambodia
        icao24Codes.add("MMMX"); // Mexico City International Airport, Mexico
        icao24Codes.add("KLAX"); // Los Angeles International Airport, United States
        icao24Codes.add("EDDH"); // Hamburg Airport, Germany
        icao24Codes.add("EIDW"); // Dublin Airport, Ireland
        icao24Codes.add("LOWW"); // Vienna International Airport, Austria
        icao24Codes.add("EDDS"); // Stuttgart Airport, Germany
        icao24Codes.add("EGPH"); // Edinburgh Airport, United Kingdom
        icao24Codes.add("RJGG"); // Chubu Centrair International Airport, Japan
        icao24Codes.add("EPWA"); // Warsaw Chopin Airport, Poland
        icao24Codes.add("VTBS"); // Suvarnabhumi Airport, Thailand
        icao24Codes.add("OMAA"); // Abu Dhabi International Airport, United Arab Emirates
        icao24Codes.add("WIII"); // Soekarno-Hatta International Airport, Indonesia
        icao24Codes.add("RJTT"); // Tokyo Haneda Airport, Japan
        icao24Codes.add("LFPO"); // Paris Orly Airport, France
        icao24Codes.add("LEBL"); // Barcelona-El Prat Airport, Spain
        icao24Codes.add("EDDM"); // Munich Airport, Germany
        icao24Codes.add("EDDB"); // Berlin Brandenburg Airport, Germany
        icao24Codes.add("UUEE"); // Sheremetyevo International Airport, Russia
        icao24Codes.add("EGKK"); // London Gatwick Airport, United Kingdom
        icao24Codes.add("EDDT"); // Berlin Tegel Airport, Germany
        icao24Codes.add("ZGSZ"); // Shenzhen Bao'an International Airport, China
        icao24Codes.add("RJCC"); // New Chitose Airport, Japan
        icao24Codes.add("CYYC"); // Calgary International Airport, Canada
        icao24Codes.add("VTSP"); // Phuket International Airport, Thailand
        icao24Codes.add("VDSR"); // Siem Reap International Airport, Cambodia
        icao24Codes.add("MMMX"); // Mexico City International Airport, Mexico
        icao24Codes.add("KLAX"); // Los Angeles International Airport, United States
        icao24Codes.add("EDDH"); // Hamburg Airport, Germany
        icao24Codes.add("EIDW"); // Dublin Airport, Ireland
        icao24Codes.add("LOWW"); // Vienna International Airport, Austria
        icao24Codes.add("EDDS"); // Stuttgart Airport, Germany
        icao24Codes.add("EGPH"); // Edinburgh Airport, United Kingdom
        icao24Codes.add("RJGG"); // Chubu Centrair International Airport, Japan
        icao24Codes.add("EPWA"); // Warsaw Chopin Airport, Poland
        icao24Codes.add("OMDB"); // Dubai International Airport, United Arab Emirates
        icao24Codes.add("LSGG"); // Geneva Airport, Switzerland
        icao24Codes.add("LIRF"); // Rome Fiumicino Airport, Italy
        icao24Codes.add("PHNL"); // Daniel K. Inouye International Airport, United States
        icao24Codes.add("RJOO"); // Osaka International Airport, Japan
        icao24Codes.add("KDFW"); // Dallas/Fort Worth International Airport, United States
        icao24Codes.add("RKSI"); // Incheon International Airport, South Korea
        icao24Codes.add("YSSY"); // Sydney Kingsford Smith Airport, Australia
        icao24Codes.add("LEMG"); // Málaga Costa del Sol Airport, Spain
        icao24Codes.add("EDDW"); // Bremen Airport, Germany
        icao24Codes.add("NZAA"); // Auckland Airport, New Zealand
        icao24Codes.add("LFMN"); // Nice Côte d'Azur Airport, France
        icao24Codes.add("SBGR"); // São Paulo-Guarulhos International Airport, Brazil
        icao24Codes.add("ZBAA"); // Beijing Capital International Airport, China
        icao24Codes.add("CYYZ"); // Toronto Pearson International Airport, Canada
        icao24Codes.add("KORD"); // Chicago O'Hare International Airport, United States
        icao24Codes.add("VTBS"); // Suvarnabhumi Airport, Thailand
        icao24Codes.add("OMAA"); // Abu Dhabi International Airport, United Arab Emirates
        icao24Codes.add("WSSS"); // Singapore Changi Airport, Singapore
        icao24Codes.add("RJBB"); // Kansai International Airport, Japan
        icao24Codes.add("VTSP"); // Phuket International Airport, Thailand
        icao24Codes.add("OMAA"); // Abu Dhabi International Airport, United Arab Emirates
        icao24Codes.add("LSGG"); // Geneva Airport, Switzerland
        icao24Codes.add("LIRF"); // Rome Fiumicino Airport, Italy
        icao24Codes.add("PHNL"); // Daniel K. Inouye International Airport, United States
        icao24Codes.add("RJOO"); // Osaka International Airport, Japan
        icao24Codes.add("KDFW"); // Dallas/Fort Worth International Airport, United States
        icao24Codes.add("RKSI"); // Incheon International Airport, South Korea
        icao24Codes.add("YSSY"); // Sydney Kingsford Smith Airport, Australia
        icao24Codes.add("LEMG"); // Málaga Costa del Sol Airport, Spain
        icao24Codes.add("EDDW"); // Bremen Airport, Germany
        icao24Codes.add("NZAA"); // Auckland Airport, New Zealand
        icao24Codes.add("LFMN"); // Nice Côte d'Azur Airport, France
        icao24Codes.add("SBGR"); // São Paulo-Guarulhos International Airport, Brazil
        icao24Codes.add("ZBAA"); // Beijing

        // Add more ICAO24 codes as needed

        // JSON array to store airport information
        JSONArray airportInfoArray = new JSONArray();

        // Iterate through the ICAO24 codes and retrieve airport information
        for (String icao24Code : icao24Codes) {
            JSONArray airportInfo = opensky.getAirportInfoByIcao24(icao24Code);
            if (airportInfo != null) {
                airportInfoArray.put(airportInfo);
            }
        }

        // Save the airport information to a file
        try (FileWriter fileWriter = new FileWriter("C:\\Users\\jarek\\IdeaProjects\\Semestralka-PJV\\Semestralka-kod\\kopcajar_semestralka\\src\\main\\java\\cz\\cvut\\fel\\pjv\\airport_info.json")) {
            fileWriter.write(airportInfoArray.toString());
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
        */


    }}

