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
        final int ISSUE_PROXIMITY_ALERT    = 20; // issue proximity alert when end point is 20m away
        final int ISSUE_NEXT_DIRECTION     = 10;
 
        boolean    finished                = false;
        boolean    loadNextDirection       = true;
        boolean    issuedDirection         = false;
        boolean    startedProximityAlert   = false;
        boolean    issueProximityAlert     = true;
        
        JourneyLeg currentLeg = null;
        CoOrdinate endPoint   = null;
        
        CoOrdinate currentPosition = lr.getCurrentLocation();
        CoOrdinate oldPosition     = null;
        
        long       currentTime = System.currentTimeMillis();
        long       lastTime;
        
        int logCounter = 0;
        final int logFrequency = 20;
        
		while (!finished)
        {			
			if (loadNextDirection)
       	 	{				
				if (!journey.haveAnotherLeg())
				{
					finished = true;
					break;
				}
				
				try
				{
					currentLeg = journey.getNextLeg();
				}
				catch (Exception e)
				{
					Utils.makeLog(e);
					finished = true;
					break;
				}
				
       		 	endPoint   = currentLeg.getEnd();       		 
       		 	
       		 	loadNextDirection       = false;
       		 	issuedDirection         = false;
       		    startedProximityAlert   = false;    
       		    issueProximityAlert     = true;
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
       	 		
       	 		// convert from metres per millisecond into km per hour
       	 		int speedInt = (int)((speed / 1000) * 1000 * 60 * 60); 

       	 		na.setSpeed("speed: " + String.valueOf(speedInt) + "km/h");  
       	 		
       	 		currentLeg.setDistanceLeft((int)distanceToEndpoint);
       	 		
       	 		na.setDirection(currentLeg.getSimpleInstruction());
       	 		na.setDistanceLeft("left: " + String.valueOf(journey.getSubsequentLegsDistance() + (int)distanceToEndpoint) + "m");
       	 		na.setDistanceTravelled("travelled: " + String.valueOf(journey.getTotalDistanceTravelled()) + "m");
       	 
       	 	    if (distanceToEndpoint < ISSUE_NEXT_DIRECTION)
       	 		{
       	 			loadNextDirection = true;
       	 		    Vibration.stopProximityAlert();
       	 		}
       	 		else if (!issuedDirection)
       	 		{
       	 			if (distanceToEndpoint < ISSUE_DIRECTION_DISTANCE)
       	 			{
       	 				try
       	 				{
       	 					boolean issuedCommand = na.issueDirection(currentLeg);
       	 					// Some times commands don't actually get relayed to the user e.g. 'continue straight',
       	 					// in those cases we don't want to issue a proximity alert.
       	 					issueProximityAlert = issuedCommand;
       	 					issuedDirection = true;
       	 				}
       	 				catch (NullPointerException e)
       	 				{
       	 					Utils.makeLog(e);
       	 					finished = true;
       	 					break;
       	 				}	 
       	 			}        		         		 
       	 		}
       	 
       	 		if (issuedDirection && (!startedProximityAlert))
       	 		{
       	 			if ((distanceToEndpoint < ISSUE_PROXIMITY_ALERT) && (issueProximityAlert))
       	 			{
       	 				na.startProximityAlert();
       	 				startedProximityAlert = true;
       	 			}
       	 		}
       	 		
       	 		if (logCounter == logFrequency)
       	 		{
       	 			logCounter = 0;       	 			
       	 			Utils.makeLog("Current position is " + currentPosition.getLatitude() + "," + currentPosition.getLongitude());
       	 			Utils.makeLog("Current end point is " + endPoint.getLatitude() + "," + endPoint.getLongitude());
       	 			Utils.makeLog("Distance to endPoint is " + distanceToEndpoint);
       	 		}       	 		
       	 	}
       	 	else
       	 	{   	 		
       	 		if (logCounter == logFrequency)
       	 		{
       	 			logCounter = 0;
       	 			Utils.makeLog("No location received yet");
       	 		}
       	 	}
       	 	
       	 	logCounter++;
       	 
       	 	try 
       	 	{
       	 		Thread.sleep(500);
       	 	} 
       	 	catch (InterruptedException e) 
       	 	{
       	 		Utils.makeLog(e);
       	 	}
        } 	
		
		na.finishJourney();
	}
}