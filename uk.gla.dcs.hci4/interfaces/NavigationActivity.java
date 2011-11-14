package interfaces;

import src.JourneyLeg;

public interface NavigationActivity 
{
	public void setDirection(String direction);
	public boolean issueDirection(JourneyLeg jl);
	public void setDistanceTravelled(String distanceTravelled);
	public void setDistanceLeft(String distanceLeft);
	public void setSpeed(String speed);
	public void startProximityAlert();
	public void finishJourney();
	public void readOutJourneyInformation();
	public void restartJourney(); 
}
