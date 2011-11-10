package interfaces;

import src.JourneyLeg;

public interface NavigationActivity 
{

	public void setDirection(String direction);
	public void issueDirection(JourneyLeg jl);
	public void setDistanceTravelled(String distanceTravelled);
	public void setDistanceLeft(String distanceLeft);
	public void setSpeed(String speed);
	public void startProximityAlert();
}
