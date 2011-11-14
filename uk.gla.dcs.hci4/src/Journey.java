package src;

import java.util.List;

/**
 * Represents a single journey from one point to another.
 */
public class Journey
{
	/** Distance left in metres **/
	private int distance;
	
	private int distanceOfLegsCovered = 0;
	
	/** The legs which make up the journey **/
	private List<JourneyLeg> legs;
	private int currentLeg = -1;
	
	public Journey(int dist, List<JourneyLeg> legs)
	{
		this.distance = dist;
		this.legs = legs;
	}

	/**
	 * Return the sum distance of all journey legs AFTER the current one.
	 * @return
	 */
	public int getSubsequentLegsDistance() 
	{
		return distance;
	}
	
	public int getTotalDistanceLeft()
	{
		return distance + getCurrentLeg().getDistanceLeftMetres();
	}
	
	public JourneyLeg getCurrentLeg()
	{
		return legs.get(currentLeg);
	}
	
	public JourneyLeg getNextLeg()
	{
		if (currentLeg > -1)
		{
			distanceOfLegsCovered += getCurrentLeg().getTotalDistance();
		}
		
		currentLeg++;
		
		// Adjust distance - remove the current leg from total distance.
		distance -= getCurrentLeg().getDistanceLeftMetres();
				
		return getCurrentLeg();
	}
	
	public boolean haveAnotherLeg()
	{
		if (currentLeg >= legs.size())
		{
			return false;
		}
		else
		{
			return true;
		}		
	}
	
	public List<JourneyLeg> getLegs()
	{
		return legs;
	}
	
	public String getSummary()
	{
		StringBuilder summary = new StringBuilder();
		
		summary.append(getCurrentLeg().getSimpleInstruction());
		summary.append(".");
		summary.append("In total you have " + getTotalDistanceLeft() + "metres still to go");
		
		return summary.toString();
	}

	public int getTotalDistanceTravelled() 
	{
		return (distanceOfLegsCovered + getCurrentLeg().getDistanceTravelledMetres());
	}
}