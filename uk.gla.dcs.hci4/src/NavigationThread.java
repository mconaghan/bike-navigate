package src;

import interfaces.LocationReader;
import interfaces.NavigationActivity;

public class NavigationThread implements Runnable
{

	final LocationReader lr;
	final Journey journey;
	final NavigationActivity na;
	
	public NavigationThread(LocationReader lr, Journey j, NavigationActivity na)
	{
		this.lr = lr;
		this.journey = j;
		this.na = na;
	}

	@Override
	public void run() 
	{        
        final int ISSUE_DIRECTION_DISTANCE = 50; // issue next direction when end point is 50m away
        final int ISSUE_PROXIMITY_ALERT    = 10; // issue proximity alert when end point is 10m away
        final int ISSUE_NEXT_DIRECTION     = 5;
 
        boolean    finished = false;
        boolean    loadNextDirection = true;
        boolean    issuedDirection = false;
        boolean    startedProximityAlert = false;
        
        JourneyLeg currentLeg = null;
        CoOrdinate endPoint = null;
        
        CoOrdinate currentPosition = lr.getCurrentLocation();
        CoOrdinate oldPosition;
        
        long       currentTime = System.currentTimeMillis();
        long       lastTime;
        
		while (!finished)
        {            	  
			
			if (loadNextDirection)
       	 	{				
				if (!journey.haveAnotherLeg())
				{
					finished = true;
					break;
				}
				
				currentLeg = journey.getNextLeg();
       		 	endPoint   = currentLeg.getEnd();
       		 
       		 	na.setDirection(currentLeg.getFullInstructions());
       		 	loadNextDirection = false;
       	  	}
       	 
       	 	oldPosition     = currentPosition;
       	 	currentPosition = lr.getCurrentLocation();
       	
       	 	if ((currentPosition != null) && (oldPosition != null))
       	 	{         		 
       	 		lastTime    = currentTime;
       	 		currentTime = System.currentTimeMillis();        	 
       	 
       	 		double distanceToEndpoint = Utils.distFrom(currentPosition, endPoint);
       	 		double speed = Utils.calculateSpeed(Utils.distFrom(oldPosition, currentPosition),      			                                      
       			                             (currentTime - lastTime));
       	 		na.setSpeed("Current speed: " + String.valueOf(speed));        	 
       	 
       	 		//if (currentPosition.equals(endPoint))
       	 	    if (distanceToEndpoint < ISSUE_NEXT_DIRECTION)
       	 		{
       	 			loadNextDirection = true;
       	 		}
       	 		else if (!issuedDirection)
       	 		{
       	 			if (distanceToEndpoint < ISSUE_DIRECTION_DISTANCE)
       	 			{
       	 				na.issueDirection(currentLeg.getFullInstructions());
       	 				issuedDirection = true;
       	 			}        		         		 
       	 		}
       	 
       	 		if (issuedDirection && (!startedProximityAlert))
       	 		{
       	 			if (distanceToEndpoint < ISSUE_PROXIMITY_ALERT)
       	 			{
       	 				na.startProximityAlert();
       	 				startedProximityAlert = true;
       	 			}
       	 		}
       	 		
       	 		na.setDistanceLeft(String.valueOf(distanceToEndpoint));
       	 
       	 		Utils.makeLog("Current position is " + currentPosition.getLatitude() + "," + currentPosition.getLongitude());
       	 		Utils.makeLog("Current end point is " + endPoint.getLatitude() + "," + endPoint.getLongitude());
       	 		Utils.makeLog("Distance to endPoint is " + distanceToEndpoint);
       	 	}
       	 	else
       	 	{
                Utils.makeLog("No location received yet");
       	 	}
       	 
       	 	try 
       	 	{
       	 		Thread.sleep(2000);
       	 	} 
       	 	catch (InterruptedException e) 
       	 	{
       	 		// TODO Auto-generated catch block
       	 		e.printStackTrace();
       	 	}
        } 		
	}
}