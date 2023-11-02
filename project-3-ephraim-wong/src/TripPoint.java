import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip;

	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	// returns time
	public int getTime() {
		return time;
	}
	
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}
	
	/**
	 * 
	 * @return a copy of movingTrip ArrayList
	 */
	public static ArrayList<TripPoint> getMovingTrip(){
		return new ArrayList<>(movingTrip);
	}
	
	// uses the haversine formula for great sphere distance between two points
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}
	
	//to-do
	public static double avgMovingSpeed() {
		ArrayList<TripPoint> movingTrip = getMovingTrip();
		double movingDist = 0;
		for (int i = 1; i < movingTrip.size();i++) {
			TripPoint a = movingTrip.get(i-1);
			TripPoint b = movingTrip.get(i);
			movingDist += haversineDistance(a,b);
		}
		return (movingDist/movingTime());
	}
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}
	
	public static double movingTime() {
		ArrayList<TripPoint> movingTrip = getMovingTrip();
		double time = 0;
		double t1,t2 = 0;
		for(int i = 1; i < movingTrip.size();i++) {
//			time += i.getTime();
			t1 = movingTrip.get(i-1).getTime();
			t2 = movingTrip.get(i).getTime();
			time += t2-t1;
		}
		return time/60.0;
	}
	
	public static double stoppedTime() {
		return (totalTime()-movingTime());
	}
	
	// finds the total distance traveled over the trip
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	public String toString() {
		
		return null;
	}

	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}
	
	public static int h1StopDetection() throws FileNotFoundException, IOException {
		if (trip == null) {
			readFile("triplog.csv");
		}
		int stopCount = 0;
		movingTrip = getTrip();
		
		
		for (int i = 1; i < trip.size(); i++) {
			TripPoint a = trip.get(i-1);
			TripPoint b = trip.get(i);

			double dist = haversineDistance(a, b);	
			if (!(dist > 0.6)) {
				movingTrip.remove(b);
				stopCount++;
			}
		}
		return stopCount;
	}
//	public static int h2StopDetection() throws FileNotFoundException, IOException {
//		//not invoked in JUnit
//		if (trip == null) {
//			readFile("triplog.csv");
//		}
//		int stopCount = 0;
//		movingTrip = getTrip();
//		for (int i = 0; i < movingTrip.size(); i++) {
//			TripPoint a = movingTrip.get(i);
//			for (int j = i; j < movingTrip.size();j++) {
//				if(i != j) {
//					TripPoint b = movingTrip.get(j);
//					double dist = haversineDistance(a,b);
//					if(dist<=0.5) {
//						movingTrip.remove(b);
//						j--;
//						stopCount++;
//					}
//				}
//			}
//		}
//		return stopCount;
//	}
	public static int h2StopDetection() throws FileNotFoundException, IOException {
	    if (trip == null) {
	        readFile("triplog.csv");
	    }
	    int stopCount = 0;
	    movingTrip = getTrip();
	    
	    Iterator<TripPoint> iterator = movingTrip.iterator();
	    while(iterator.hasNext()) {
	    	TripPoint curr = iterator.next();
	    	ArrayList<TripPoint> stopZone = new ArrayList<>();
	    	
	    	for (TripPoint other: movingTrip) {
	    		double dist = haversineDistance(curr, other);
	    		if (curr != other && dist<=0.5) {
	    			stopZone.add(other);
	    		}
	    	}
	    	if (stopZone.size() >= 2) {
	    		stopCount += stopZone.size() +1;
	    		iterator.remove();
	    	}
	    }
	    return stopCount;
//	    ArrayList<TripPoint> detectedStops = new ArrayList<>(); // Create a list to store detected stops
////	    ArrayList<TripPoint> detectedStops2 = new ArrayList<>();
//	    ArrayList<ArrayList<TripPoint>> stopZone = new ArrayList<>();
//	    ArrayList<TripPoint> temp = new ArrayList<>();
//	    for (int i = 1; i < movingTrip.size(); i++) {
//	    	TripPoint a = movingTrip.get(i-1);
//	    	TripPoint b = movingTrip.get(i);
//	    	double dist = haversineDistance(a, b);
//	    	boolean nextValid = true;
//	    	while (true) {
//	    		if (dist <= 0.5) {
//	    			temp.add(a);
//	    		}
//	    		//	    	if (!temp.contains(a)) {
//	    		//	    		temp.add(a);
//	    		//	    	}
//	    		else{
//	    			for (int j = 0; j < temp.size(); j++) {
//	    				TripPoint compare = temp.get(j);
//	    				double dist2 = haversineDistance(b,compare);
//	    				if (dist2<=0.5 &&!temp.contains(b)) {
//	    					temp.add(b);
//	    				}
//	    			}
//	    			nextValid = false;
//	    		}
//	    	}
//	    	
//	    	
//	    	stopZone.add(temp);
//	    	stopCount += temp.size();
//	    	temp.clear();
//	    }
//	    movingTrip.removeAll(stopZone);
//	    return stopCount;
	    
	    
	    
//	    			TripPoint b = movingTrip.get(j);
	    			

//	    			if (dist <= 0.5 && !detectedStops.contains(b)) {
////	    				detectedStops.add(a);
//	    				detectedStops.add(b); // Add the detected stop to the list
////	    				detectedStops2.add(b);
	    				//stopCount++;
//	    				System.out.println("lat:"+b.getLat()+" lon:"+b.getLon());
//	    			}
//	    		}
//	    	}
//	    }
//	    int groupStopCount = 0;
//	    //[[stopZone], [single stop], [stopZone]]
//	    //stopZone >= 3 stops
////	    ArrayList<ArrayList<TripPoint>> stopZone = new ArrayList<>();
////	    ArrayList<TripPoint> detectedStops2 = new ArrayList<>();
////	    detectedStops2.addAll(detectedStops);
//	    
//	    
//	    for (TripPoint curr: detectedStops2) {
//	    	ArrayList<TripPoint> temp = new ArrayList<>();
//	    	for (TripPoint compare: ) {
//	    		double dist = haversineDistance(curr, compare);
//	    		if (curr!=compare && dist <=0.5 && !temp.contains(compare)) {
//	    			temp.add(compare);
////	    			break;
//	    		}
//	    	}
////	    	if(temp.size()>= 2) {
////	    		temp.add(curr);
////	    		stopZone.add(temp);
////	    	}
//	    	stopZone.add(temp);
//	    	detectedStops2.removeAll(temp);
//	    }
//	    // Remove the detected stops from the movingTrip ArrayList
//	    movingTrip.removeAll(detectedStops);
////	    stopCount+=groupStopCount;
//	    return stopZone.size();
	}
//	public static int validateZone
	
//	public static int h2StopDetection() throws FileNotFoundException, IOException {
//	    if (trip == null) {
//	        readFile("triplog.csv");
//	    }
//	    int stopCount = 0;
//	    movingTrip = getTrip();
//	    Set<TripPoint> cleared = new HashSet<>();
//	    for (TripPoint i : trip) {
//	    	if (!cleared.contains(i)) {
//	    		ArrayList<TripPoint> stopZone = stopZone(i, 0.5);
//	    		if (stopZone.size()>=3) {
//	    			stopCount++;
//	    			movingTrip.removeAll(stopZone);
//	    			cleared.addAll(stopZone);
//	    		}
//	    		
//	    	}
//	    }
//	    return stopCount;
//	}
//	public static ArrayList<TripPoint> stopZone(TripPoint center, double radius){
//		ArrayList<TripPoint> stopZone = new ArrayList<>();
//		for (TripPoint point : trip) {
//			if (point != center) {
//				double dist = haversineDistance(center, point);
//				if (dist <= 0.5) {
//					stopZone.add(point);
//				}
//			}
//		}
//		return stopZone;
//	}
}
