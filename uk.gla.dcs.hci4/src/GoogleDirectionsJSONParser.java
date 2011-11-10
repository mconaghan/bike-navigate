package src;

import interfaces.DirectionParser;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Class to parse JSON directions returned from Google directions API.
 */
public class GoogleDirectionsJSONParser implements DirectionParser
{

	public Journey parseJSONDirections(String input)
	{		
		LinkedList<JourneyLeg> directions = new LinkedList<JourneyLeg>();
		int totalDistance = 0;
		
		try 
		{
			JSONTokener tokeniser = new JSONTokener(input);
			JSONObject object = (JSONObject)tokeniser.nextValue();
			JSONArray routes = object.getJSONArray("routes");
			
			if (routes.length() > 1)
			{
				throw new RuntimeException("Too many routes");
			}
			
			JSONArray legs = ((JSONObject)routes.get(0)).getJSONArray("legs");
			
			if (legs.length() > 1)
			{
				throw new RuntimeException("Too many legs");
			}
			
			JSONObject totDistance = ((JSONObject)legs.get(0)).getJSONObject("distance");
			totalDistance = totDistance.getInt("value");
			
			JSONArray steps = ((JSONObject)legs.get(0)).getJSONArray("steps");
			
			for (int ii = 0; ii < steps.length(); ii++)
			{
				JSONObject step = (JSONObject)steps.get(ii);
				String instructions = step.getString("html_instructions");
				JSONObject start = step.getJSONObject("start_location");
				JSONObject end = step.getJSONObject("end_location");
				JSONObject distance = step.getJSONObject("distance");
				String distanceMetres = distance.getString("value");
				
				CoOrdinate startPoint = new CoOrdinate(start.getString("lng"), start.getString("lat"));
				CoOrdinate endPoint = new CoOrdinate(end.getString("lng"), end.getString("lat"));
				
				directions.add(new JourneyLeg(distanceMetres, startPoint, endPoint, instructions));				
			}
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new Journey(totalDistance, directions);		
	}
}
