package src;

import java.util.List;

/**
 * Represents a single journey from one point to another.
 */
public class Journey
{
	/** Distance left in metres **/
	private int distance;
	
	/** The legs which make up the journey **/
	private List<JourneyLeg> legs;
	private int currentLeg = 0;
	
	public Journey(int dist, List<JourneyLeg> legs)
	{
		this.distance = dist;
		this.legs = legs;
	}

	public int getDistance() 
	{
		return distance;
	}
	
	public JourneyLeg getCurrentLeg()
	{
		return legs.get(currentLeg);
	}
	
	public JourneyLeg getNextLeg()
	{
		currentLeg++;
		return getCurrentLeg();
	}
	
	public boolean haveAnotherLeg()
	{
		if (currentLeg >= legs.size())
		{
			return true;
		}
		return false;
	}
	
	public List<JourneyLeg> getLegs()
	{
		return legs;
	}
}