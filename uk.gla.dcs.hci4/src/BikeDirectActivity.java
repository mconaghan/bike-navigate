/* TODO
 * test with kevin's phone version
 * 
 * FROM REAL USE:
 * Get verbal instruction twice (only sometimes)
 * proximity alert not issued until after the turn??
 * 
 * Report 
 * talk about threads
 * battery life
 */

package src;

import interfaces.DirectionParser;
import interfaces.LocationReader;
import interfaces.NavigationActivity;
import uk.ac.gla.dcs.hci4.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class BikeDirectActivity extends Activity implements NavigationActivity
{	
	private TextView distanceLeftLabel;
	private TextView distanceTravelledLabel;
    private TextView directionLabel;
    private TextView speedLabel;
    private TableLayout journeyStepsTable;
    
    private Vibrator vibrator;
    private LocationHandler locationHandler;
    
    private TextToSpeechHandler ttsHandler;
    
    private static Journey journey;
    
    private final String PROXIMITY_ALERT_STRING = "You are approaching the next turn";
    private final String ARRIVED_AT_DESTINATION_STRING = "You have arrived at your destination";
    
    // Pop-up dialog types
    private final int ROUTE_CANNOT_BE_FOUND_ERROR          = 0;
    private final int JOURNEY_FINISHED                     = 1;
    private final int DIRECTION_CANNOT_BE_UNDERSTOOD_ERROR = 2;
    
    private String startString;
    private String endString;
    
    private long lastKeyPressTime = 0;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // By default Network IO is disabled on main thread.  Nothing else to do
        // while waiting for Google, so disable this and just block waiting for
        // directions.
        try
        {
        	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        	.permitNetwork()
            .build());
        	
        }
        catch (NoClassDefFoundError e)
        {
        	// Older than Gingerbread, can't do this (and no need)
        }
        
        // Load start view
        setContentView(R.layout.start_view);      
        
        // Start getting location
        locationHandler = new LocationHandler((LocationManager)getSystemService(Context.LOCATION_SERVICE));
        
        // Get the vibrator for the phone
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Set-up TTS
        ttsHandler = new TextToSpeechHandler(this);		
    }    

    /** Called when TTS is loaded. */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	ttsHandler.init(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)) 
        {
        	Utils.makeLog("Detected volume key press");
        	
        	if (System.currentTimeMillis() - lastKeyPressTime < 500);
        	{        		
        		Utils.makeLog("Detected two volume key presses - reading out journey information");
                readOutJourneyInformation();
        	}
        	
        	lastKeyPressTime = System.currentTimeMillis();        	
        }

        return super.onKeyLongPress(keyCode, event);
    }
    
    @Override
    /** Called to create a dialog pop-up **/
    protected Dialog onCreateDialog(int id)
    {    	
    	String message = "";
    	
    	switch (id)
    	{
    		case (JOURNEY_FINISHED):
    			message = "You have finished your journey";break;
    		
    		case (ROUTE_CANNOT_BE_FOUND_ERROR):
    			message = "Route cannot be found, please try again";break;
    			
    		case (DIRECTION_CANNOT_BE_UNDERSTOOD_ERROR):
    			message = "Cannot understand the next direction";break;
    		
    		default:
    			// Coding error
    			throw new RuntimeException("Unknown dialog ID: " + id);    		
    	}
    	
    	// Build and display the pop-up
  		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message)
    		       .setCancelable(false)
    		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
		
		return builder.create();
    }
    
    public void endJourney(View v){ finishJourney();}
    
    public void restartJourney()
    {
    	// Force use of current location by removing existing start location
    	startString = "";
    	runOnUiThread(new Runnable(){ public void run()
		{
    		startNavigation(null);
		}});
    }
    
    /** Called when the user asks to start navigation - calculates and displays the journey. **/
    public void startNavigation(View v)
    {    	   	
        final EditText startText  = (EditText)findViewById(R.id.start_entry);
        final EditText endText    = (EditText)findViewById(R.id.end_entry);        
        
        // If this is the second subsequent journey, we will have stopped asking for location info, start again
        locationHandler.start();
        
    	// Get the start and end point
    	if (startText != null)
    	{
    		startString = startText.getText().toString();
    	}   
    	
    	// Try to use current location if no start has been provided
    	if (startString.equals(""))
    	{
    		if (locationHandler.getCurrentLocation() != null)
    		{    			
    			startString = String.valueOf(locationHandler.getCurrentLocation().getLatitude()) + "," + String.valueOf(locationHandler.getCurrentLocation().getLongitude());
    			Utils.makeLog("Using Current location: " + startString);
    		}   
    		else
    		{
    			Utils.makeLog("Current location cannot be found");
    		}
    	}
    	
    	if (endText != null)
    	{
    		endString = endText.getText().toString();
    	} 

		try 
		{
	    	// Calculate the journey
			journey = calculateJourney(startString, endString);
			
			// Update the journey view, need to call setContentView first otherwise it will be null
			setContentView(R.layout.journey_view);
			journeyStepsTable = (TableLayout)findViewById(R.id.myTableLayout);
			updateJourneyView(journey);
			
			// Switch to the navigation view
	    	setContentView(R.layout.navigation_view);
	        
	    	// Load the labels on the navigation view so that the navigation thread can update them.
	        distanceLeftLabel = (TextView)findViewById(R.id.distance_left_label);
	        directionLabel = (TextView)findViewById(R.id.direction_label);
	        speedLabel = (TextView)findViewById(R.id.speed_label);
	        distanceTravelledLabel = (TextView)findViewById(R.id.distance_travelled_label);
			
			// And display the results
	    	distanceLeftLabel.setText("Distance left: " + String.valueOf(journey.getSubsequentLegsDistance()));	
	    	
	    	startNavigationThread(journey, locationHandler);
		} 
		catch (RouteNotFoundException e1) 
		{
			displayRouteNotFoundError();
		}
		catch (DirectionException e1) 
		{
			displayBadDirectionError();
		}
    	catch (InterruptedException e)
    	{
    		Utils.makeLog("Navigation thread was interrupted");
    	}    	
    }
    
    /**
     * Update the information displayed on the journey view
     */
    private void updateJourneyView(Journey journey) 
    {    	    	
    	for (JourneyLeg jl : journey.getLegs())
    	{
    		TableRow tr = new TableRow(this);
        	tr.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	TextView tv = new TextView(this);
        	tv.setText(jl.getSimpleInstruction());
        	tv.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
        	tr.addView(tv);
        	
            journeyStepsTable.addView(tr, 
            		                  new TableLayout.LayoutParams(
                                        LayoutParams.WRAP_CONTENT,
                                        LayoutParams.WRAP_CONTENT));
    	}    			
    	
    	setContentView(journeyStepsTable);
    }

    /** Start a new thread to monitor current location and decide when instructions need to be issued or loaded **/
	private void startNavigationThread(Journey journey, LocationReader lr) throws InterruptedException
    {        
        NavigationThread nt = new NavigationThread(lr, journey, this);
        Thread t = new Thread(nt);
        t.start();
    }
    
	/** Calculate the journey from a start and end point **/
    private Journey calculateJourney(String start, String end) throws RouteNotFoundException, DirectionException
    {
   		GoogleDirectionsQuery q = new GoogleDirectionsQuery(start, end);
   		String directions = q.queryGoogleDirections();
    		
   		DirectionParser p = new GoogleDirectionsJSONParser();
   		Journey journey = p.parseJSONDirections(directions);
    	
   		return journey;
    }    

	// NAVIGATION ACTIVITY METHODS    
    
	public void readOutJourneyInformation(View v){readOutJourneyInformation();}
	public void readOutJourneyInformation()
	{		
		if (journey != null)
		{
			speak(journey.getSummary());
		}
		else
		{
			Utils.makeLog("Tried to read out journey information when there is no journey");
		}		
	}
	
    /** Issue a direction to the user. */
    public boolean issueDirection(JourneyLeg jl)
    {    	
    	boolean issuedCommand = true;
    	Direction d = jl.getNextDirection();
    	
    	if (d.equals(Direction.CONTINUE_STRAIGHT))
    	{
    		issuedCommand = false;
    	}
    	else
    	{
    		Utils.makeLog("issueDirection: " + jl.getSimpleInstruction());
        	speak(jl.getNextDirection().toString());
    		setDirection(jl.getSimpleInstruction());
    	    Vibration.startDirectionVibration(jl.getNextDirection(), vibrator); 
    	}
    	
    	return issuedCommand;	    
    }
    
    /** Issue the proximity alert to the user. **/
    public void startProximityAlert()
    {
    	Utils.makeLog("startProximityAlert");
    	speak(PROXIMITY_ALERT_STRING);
    	Vibration.startProximityAlert(vibrator);
    }    
        
    /** Tell the user that the journey has finished. **/
    public void finishJourney()
    {
    	runOnUiThread(new Runnable(){ public void run()
    		{
    			speak(ARRIVED_AT_DESTINATION_STRING);
    			Vibration.startDirectionVibration(Direction.ARRIVED, vibrator);
        		displayStartView(null);
        		showDialog(JOURNEY_FINISHED);
        		locationHandler.stop();
            	displayStartView(null);
            	journey = null;
    		}});      	
    }
    
	@Override
	/** Set the current direction. **/
	public void setDirection(String direction) 
	{
		if (directionLabel != null)
		{
			final String fDirection = direction;
			runOnUiThread(new Runnable(){ public void run(){directionLabel.setText(fDirection);}});
		}				
	}

	@Override
	/** Set the distance travelled. **/
	public void setDistanceTravelled(String distanceTravelled) 
	{
		if (distanceTravelledLabel != null)
		{
			final String fDistanceTravelled = distanceTravelled;
			
			runOnUiThread(new Runnable(){ public void run(){distanceTravelledLabel.setText(fDistanceTravelled);}});
		}		
	}

	@Override
	/** Set the distance left. **/
	public void setDistanceLeft(String distanceLeft) 
	{
		if (distanceLeftLabel != null)
		{
			final String fDistanceLeft = distanceLeft;
			runOnUiThread(new Runnable(){ public void run(){distanceLeftLabel.setText(fDistanceLeft);}});
		}		
	}

	@Override
	/** Set the current speed. **/
	public void setSpeed(String speed) 
	{
		if (speedLabel != null)
		{
			final String fSpeed = speed;
			runOnUiThread(new Runnable(){ public void run(){speedLabel.setText(fSpeed);}});
		}				
	}
	
	// ERROR DIALOGS
	private void displayRouteNotFoundError(){showDialog(ROUTE_CANNOT_BE_FOUND_ERROR);}
	private void displayBadDirectionError(){showDialog(DIRECTION_CANNOT_BE_UNDERSTOOD_ERROR);}
	
	// HELP VIEW METHODS
	public void demoLeftTurn(View v)   {Vibration.startDirectionVibration(Direction.TURN_LEFT,    vibrator);speak(Direction.TURN_LEFT.toString());}	
	public void demoRightTurn(View v)  {Vibration.startDirectionVibration(Direction.TURN_RIGHT,   vibrator);speak(Direction.TURN_RIGHT.toString());}
	public void demoSlightLeft(View v) {Vibration.startDirectionVibration(Direction.SLIGHT_LEFT,  vibrator);speak(Direction.SLIGHT_LEFT.toString());}	
	public void demoSlightRight(View v){Vibration.startDirectionVibration(Direction.SLIGHT_RIGHT, vibrator);speak(Direction.SLIGHT_RIGHT.toString());}
	public void demoFirstExit(View v){demoExit(v, 1);}
	public void demoSecondExit(View v){demoExit(v, 2);}
	public void demoThirdExit(View v){demoExit(v, 3);}
	public void demoFourthExit(View v){demoExit(v, 4);}
	public void demoFifthExit(View v){demoExit(v, 5);}
	public void demoSixthExit(View v){demoExit(v, 6);}

	public void demoProximity(View v)
	{
		speak(PROXIMITY_ALERT_STRING);
		
		Vibration.startProximityAlert(vibrator);
		Vibration.stopProximityAlert();		
	}
	
	public void demoExit(View v, int exitNumber)
	{
		Direction d;
		
		switch (exitNumber)
		{
			case(1):d = Direction.FIRST_EXIT;break;
			case(2):d = Direction.SECOND_EXIT;break;
			case(3):d = Direction.THIRD_EXIT;break;
			case(4):d = Direction.FOURTH_EXIT;break;
			case(5):d = Direction.FIFTH_EXIT;break;
			case(6):d = Direction.SIXTH_EXIT;break;
		
			default:
				throw new RuntimeException("Don't recognise the exit number: " + exitNumber);
		}
		
		Vibration.startDirectionVibration(d, vibrator);
		speak(d.toString());
	}
	
	private void speak(String speech){ttsHandler.speak(speech);}
		
	// CHANGE VIEW METHODS
	public void displayJourneyOverView(View v){setContentView(journeyStepsTable);}
    public void displayHelpView(View v){setContentView(R.layout.help_view);}     
    public void displayStartView(View v){setContentView(R.layout.start_view);}   
    
    public void displayNavigationView(View v)
    {
    	// The values from teh screen may be lost when we redraw, so keep a note of them.
    	String speed = (String)speedLabel.getText();
    	String distanceLeft = (String)distanceLeftLabel.getText();
    	String distanceTravelled = (String)distanceTravelledLabel.getText();
    	String direction = (String)directionLabel.getText(); 
    	
    	setContentView(R.layout.navigation_view);
        distanceLeftLabel = (TextView)findViewById(R.id.distance_left_label);
        directionLabel = (TextView)findViewById(R.id.direction_label);
        speedLabel = (TextView)findViewById(R.id.speed_label);
        distanceTravelledLabel = (TextView)findViewById(R.id.distance_travelled_label);
        
    	setSpeed(speed);
    	setDistanceLeft(distanceLeft);  	
    	setDirection(direction);
    	setDistanceTravelled(distanceTravelled);
    }
}