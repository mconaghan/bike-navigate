package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Utils 
{

	/**
	 * Calculate distance between two longtitude/latitude co-ordinates, using the Haversine formula.
	 * 
	 * Copy of code from http://stackoverflow.com/questions/120283/working-with-latitude-longitude-values-in-java,
	 * accessed on 24/10/2011. 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return distance in metres
	 */
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) 
	{		
	    double earthRadius = 6371000; //Average Earth radious in m
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return dist;
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @return distance in metres
	 */
	public static double distFrom(CoOrdinate from, CoOrdinate to)
	{
		return distFrom(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
	}

	/**
	 * Covert a UTF-8 input stream into a String, taken from http://www.kodejava.org/examples/266.html
	 * on 24/10/2011.
	 * 
	 * @param is Input stream
	 * 
	 * @return Contents of input stream as a String.
	 * @throws IOException
	 */
	public static String convertStreamToString(InputStream is) throws IOException 
	{
	    if (is != null) 
	    {
	           
	    	Writer writer = new StringWriter();

	        char[] buffer = new char[1024];
	        try 
	        {	                
	        	Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	                
	        	int n;
	            while ((n = reader.read(buffer)) != -1) 
	            {	                   
	            	writer.write(buffer, 0, n);	                
	            }
	        } 
	        finally 
	        {	                
	        	is.close();	            
	        }
	            
	        return writer.toString();
	    } 
	    else 
	    {      	            
	    	return "";	        
	    }	    
	}
	
	public static void makeLog(Exception e)
	{
		makeLog(e.getLocalizedMessage());
	}
	
	public static void makeLog(String log)
	{
		System.out.println("LOG: " + log);
	}
	
	public static String[] readFile(File f)
	{        
		List<String> lines = new ArrayList<String>();
        String line;
        
		try 
		{
			BufferedReader fileReader = new BufferedReader(new FileReader(f));        
	        
			while ((line = fileReader.readLine()) != null)   
			{
				lines.add(line);
			}
		}
		catch (IOException e) 
		{
			Utils.makeLog(e);
		}
		
		return (String[]) lines.toArray();
	}
	
	public static double calculateSpeed(double distance, double time)
	{
		return (distance / time);
	}
}
