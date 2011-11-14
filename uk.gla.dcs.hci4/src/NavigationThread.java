package src;

import interfaces.LocationReader;
import interfaces.NavigationActivity;

public class NavigationThread implements Runnable
{
    final int ISSUE_DIRECTION_DISTANCE = 100; // issue next direction when end point is 100m away
    final int ISSUE_PROXIMITY_ALERT    = 50;  // issue proximity alert when end point is 50m away
    final int ISSUE_NEXT_DIRECTION     = 20;
    
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
        boolean    finished                = false; // Should the thread keep going?
        boolean    loadNextDirection       = true;  // Time to load the next leg of the journey?
        boolean    issuedDirection         = false; // Has the direction been issued for current journey leg?
        boolean    startedProximityAlert   = false; // Has the promity alert been started for current journey leg?
        boolean    issueProximityAlert     = true;  // Should a proximity alert be issed for this journey leg?
        
        JourneyLeg currentLeg = null;
        CoOrdinate endPoint   = null; // The end point of the current journey elg.
        
        CoOrdinate currentPosition = lr.getCurrentLocation();
        CoOrdinate oldPosition     = null;
        
        long       currentTime = System.currentTimeMillis();
        long       lastTime;
        
        /* Used to control how often logs are made, to avoid too many logs */
        int logCounter = 0;
        final int logFrequency = 20;
        
        double previousDistanceToEndpoint;
	 	double distanceToEndpoint = 0;
	 	
	 	/* A count of the number of consecutive iterations the user has moved further away from
	 	 * the target.  used to decide when to recalculate a journey.
	 	 */
	 	int goneBackwardsCounter = 0;
	 	final int GONE_BACKWARDS_COUNTER_LIMIT = 10;
        
		while (!finished)
        {			
			if (loadNextDirection)
       	 	{				
				if (!journey.haveAnotherLeg())
				{
					finished = true;
					na.finishJourney();
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
					na.finishJourney();
					break;
				}
				
       		 	endPoint   = currentLeg.getEnd();       		 
       		 	
       		 	loadNextDirection       = false;
       		 	issuedDirection         = false;
       		    startedProximityAlert   = false;    
       		    issueProximityAlert     = true;
       		    
       		    Vibration.stopProximityAlert();
       	  	}
       	 
       	 	oldPosition     = currentPosition;
       	 	currentPosition = lr.getCurrentLocation();
       	
       	 	if ((currentPosition != null) && (oldPosition != null))
       	 	{  
       	 		previousDistanceToEndpoint = distanceToEndpoint;
       	 		distanceToEndpoint = Utils.distFrom(currentPosition, endPoint);
       	 		
       	 		// No point in doing anything location has changed
       	 		if (distanceToEndpoint != previousDistanceToEndpoint)
       	 		{
       	 			// Have we gotten closer?
       	 			if (distanceToEndpoint > previousDistanceToEndpoint)
       	 			{
       	 				goneBackwardsCounter++;
       	 				Utils.makeLog("Went backwards again");
       	 			}
       	 			else
       	 			{
       	 				goneBackwardsCounter = 0;
       	 			}
       	 			
       	 			if (goneBackwardsCounter >= GONE_BACKWARDS_COUNTER_LIMIT)
       	 			{
       	 				// Silently restart the journey (no pop up to say journey is finished)
       	 				Utils.makeLog("Restarting journey");
       	 				finished = true;
       	 				na.restartJourney();
       	 				break;
       	 			}
       	 			
       	 			lastTime    = currentTime;   	 		
       	 			currentTime = System.currentTimeMillis(); 
       	 		
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
       	 						// This means that there is no direction to issue. which probably
       	 						// means that this is the last journey leg.
       	 					}	 
       	 				}        		         		 
       	 			}
       	 
       	 			if ((issuedDirection)        && 
       	 					(!startedProximityAlert) && 
       	 					(issueProximityAlert)    &&
       	 					(distanceToEndpoint < ISSUE_PROXIMITY_ALERT))
       	 			{
       	 				na.startProximityAlert();
       	 				startedProximityAlert = true;
       	 			}       	 		
     	 			
       	 			String log = "Current position is " + currentPosition.getLatitude() + "," + currentPosition.getLongitude() + "\n" + 
       	 						 "Current end point is " + endPoint.getLatitude() + "," + endPoint.getLongitude() + "\n" +
       	 						 "Distance to endPoint is " + distanceToEndpoint;
       	 			makeLog(log, logCounter, logFrequency);
       	 		}   
       	 		else
       	 		{
       	 			makeLog("Location has not changed", logCounter, logFrequency);
       	 		}
       	 	}
       	 	else
       	 	{   	 		
       	 		makeLog("No location received yet", logCounter, logFrequency);
       	 	}
       	 
       	 	try 
       	 	{
       	 		Thread.sleep(500);
       	 	} 
       	 	catch (InterruptedException e) 
       	 	{
       	 		Utils.makeLog(e);
       	 	}
        } 
	}
	
	/** Make a log, if it is time to do so. **/
	private void makeLog(String log, int logCounter, int logFrequency)
	{
		if (logCounter == logFrequency)
	 	{
	 		logCounter = 0;
	 		Utils.makeLog(log);
	 	}
		
		logCounter++;
	}
}