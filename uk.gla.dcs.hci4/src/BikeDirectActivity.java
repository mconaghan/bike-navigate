/* TODO
 * Make text visible on start screen (only android 4)
 * Provide ability to use current location
 * Going to journey screen then back to navigation screen breaks things.
 * provide option to start again
 * break up this class
 * roundabouts
 * tidy up code/add comments
 * 
 * FROM REAL USE:
 * need to increase values to 100/50/20, and be clever if leg is < 100/50
 * Get verbal instruction twice
 * App leaves GPS running
 * putting app in background and bringing it back to different screen (same problem as goign to overview screen?)
 * proximity alert not issued until after the turn
 * 
 * WIBNI
 * Recalculate the journey.
 * 
 * Report 
 * talk about threads
 * battery life
 */

package src;

import java.util.Locale;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class BikeDirectActivity extends Activity implements LocationListener, LocationReader, NavigationActivity, OnInitListener
{
	private static final int MY_DATA_CHECK_CODE = 0;
	Location gLocation;
	LocationManager gLocationManager;
	
	TextView distanceLeftLabel;
    TextView distanceTravelledLabel;
    TextView directionLabel;
    TextView speedLabel;
    TableLayout journeyStepsTable;
    
    Vibrator vibrator;
    
    private TextToSpeech tts;
    private boolean ttsReady = false;
    
    private static Journey journey;
    
    private final String PROXIMITY_ALERT_STRING = "You are approaching the next turn";
    
    // Pop-up dialog types
    private final int ROUTE_CANNOT_BE_FOUND_ERROR = 0;
    private final int JOURNEY_FINISHED = 1;
    
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
        	//Older than Gingerbread, can't do this 
        }
        
        setContentView(R.layout.start_view);      
        
        gLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Check TTS is installed
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		
    }    

    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if (requestCode == MY_DATA_CHECK_CODE) 
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) 
            {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);
            } 
            else 
            {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {    	
    	String message = "";
    	
    	switch (id)
    	{
    	case (JOURNEY_FINISHED):
    		message = "You have finished your journey";break;
    		
    	case (ROUTE_CANNOT_BE_FOUND_ERROR):
    		message = "Route cannot be found, please try again";break;
    		
    	default:
    		throw new RuntimeException("Unknown dialog ID: " + id);
    		
    	}
    	
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
    
    public void startNavigation(View v)
    {    	   	
        final EditText startText  = (EditText)findViewById(R.id.start_entry);
        final EditText endText    = (EditText)findViewById(R.id.end_entry);        
        
    	// Get the start and end point
    	String startString = "not-found";
    	if (startText != null)
    	{
    		startString = startText.getText().toString();
    	}        
    	
    	String endString = "not-found";
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
	        
	        distanceLeftLabel = (TextView)findViewById(R.id.distance_left_label);
	        directionLabel = (TextView)findViewById(R.id.direction_label);
	        speedLabel = (TextView)findViewById(R.id.speed_label);
	        distanceTravelledLabel = (TextView)findViewById(R.id.distance_travelled_label);
			
			// And display the results
	    	distanceLeftLabel.setText("Distance left: " + String.valueOf(journey.getSubsequentLegsDistance()));	
	    	
	    	startNavigationThread(journey, this);
		} 
		catch (RouteNotFoundException e1) 
		{
			displayRouteNotFoundError();
		}
		catch (DirectionException e1) 
		{
			// change this to day there was a problem with Google
			displayRouteNotFoundError();
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
        	
            journeyStepsTable.addView(tr, new TableLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
    	}    			
    	
    	setContentView(journeyStepsTable);
    }

	private void startNavigationThread(Journey journey, LocationReader lr) throws InterruptedException
    {        
        NavigationThread nt = new NavigationThread(lr, journey, this);
        Thread t = new Thread(nt);
        t.start();
    }
    
    private Journey calculateJourney(String start, String end) throws RouteNotFoundException, DirectionException
    {
   		GoogleDirectionsQuery q = new GoogleDirectionsQuery(start, end);
   		String directions = q.queryGoogleDirections();
    		
   		DirectionParser p = new GoogleDirectionsJSONParser();
   		Journey journey = p.parseJSONDirections(directions);
    	
   		return journey;
    }

    // LOCATION READER METHODS

	@Override
	public CoOrdinate getCurrentLocation() 
	{
		CoOrdinate c = null;
		if (gLocation != null)
		{
			c = new CoOrdinate(gLocation.getLongitude(), gLocation.getLatitude());
		}
		return c;
	}
	
	// LOCATION LISTENER METHODS
	
	@Override
	public void onLocationChanged(Location location) 
	{
		gLocation = location;	
		Utils.makeLog("New location " + location.getLongitude() + ", " + location.getLatitude());
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		Utils.makeLog("onProviderDisabled " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		Utils.makeLog("onProviderEnabled " + provider);		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		Utils.makeLog("onStatusChanged " + provider);	
	}

	// NAVIGATION ACTIVITY METHODS    
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
    
    public void startProximityAlert()
    {
    	Utils.makeLog("startProximityAlert");
    	speak(PROXIMITY_ALERT_STRING);
    	Vibration.startProximityAlert(vibrator);
    }
    
    public void finishJourney()
    {
    	runOnUiThread(new Runnable(){ public void run()
    		{
    			Vibration.startDirectionVibration(Direction.ARRIVED, vibrator);
        		displayStartView(null);
        		showDialog(JOURNEY_FINISHED);
    		}});    	
    }
    
	@Override
	public void setDirection(String direction) 
	{
		if (directionLabel != null)
		{
			final String fDirection = direction;
			runOnUiThread(new Runnable(){ public void run(){directionLabel.setText(fDirection);}});
		}				
	}

	@Override
	public void setDistanceTravelled(String distanceTravelled) 
	{
		if (distanceTravelledLabel != null)
		{
			final String fDistanceTravelled = distanceTravelled;
			
			runOnUiThread(new Runnable(){ public void run(){distanceTravelledLabel.setText(fDistanceTravelled);}});
		}		
	}

	@Override
	public void setDistanceLeft(String distanceLeft) 
	{
		if (distanceLeftLabel != null)
		{
			final String fDistanceLeft = distanceLeft;
			runOnUiThread(new Runnable(){ public void run(){distanceLeftLabel.setText(fDistanceLeft);}});
		}		
	}

	@Override
	public void setSpeed(String speed) 
	{
		if (speedLabel != null)
		{
			final String fSpeed = speed;
			runOnUiThread(new Runnable(){ public void run(){speedLabel.setText(fSpeed);}});
		}				
	}
	
	private void displayRouteNotFoundError()
	{
		showDialog(ROUTE_CANNOT_BE_FOUND_ERROR);
	}
	
	//HELP VIEW METHODS
	public void demoLeftTurn(View v)   {Vibration.startDirectionVibration(Direction.TURN_LEFT,    vibrator);speak(Direction.TURN_LEFT.toString());}	
	public void demoRightTurn(View v)  {Vibration.startDirectionVibration(Direction.TURN_RIGHT,   vibrator);speak(Direction.TURN_RIGHT.toString());}
	public void demoSlightLeft(View v) {Vibration.startDirectionVibration(Direction.SLIGHT_LEFT,  vibrator);speak(Direction.SLIGHT_LEFT.toString());}	
	public void demoSlightRight(View v){Vibration.startDirectionVibration(Direction.SLIGHT_RIGHT, vibrator);speak(Direction.SLIGHT_RIGHT.toString());}
	
	public void demoProximity(View v)
	{
		speak(PROXIMITY_ALERT_STRING);
		
		Vibration.startProximityAlert(vibrator);
		Vibration.stopProximityAlert();		
	}
	
	public void demoFirstExit(View v){demoExit(v, 1);}
	public void demoSecondExit(View v){demoExit(v, 2);}
	public void demoThirdExit(View v){demoExit(v, 3);}
	public void demoFourthExit(View v){demoExit(v, 4);}
	public void demoFifthExit(View v){demoExit(v, 5);}
	public void demoSixthExit(View v){demoExit(v, 6);}
	
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
			case(7):d = Direction.SEVENTH_EXIT;break;
			case(8):d = Direction.EIGHT_EXIT;break;
			case(9):d = Direction.NINTH_EXIT;break;
		
			default:
				throw new RuntimeException("Don't recognise the exit number: " + exitNumber);
		}
		
		Vibration.startDirectionVibration(d, vibrator);
		speak(d.toString());
	}
		
	//CHANGE VIEW METHODS
	public void displayJourneyOverView(View v){setContentView(journeyStepsTable);}
    public void displayHelpView(View v){setContentView(R.layout.help_view);}     
    public void displayStartView(View v){setContentView(R.layout.start_view);}    	
    public void displayNavigationView(View v){setContentView(R.layout.navigation_view);}

	@Override
	//TTS is ready
	public void onInit(int arg0) 
	{
		Utils.makeLog("TTS is ready");
		tts.setLanguage(Locale.UK);
		ttsReady = true;
	}
	
	private void speak(String speech)
	{
		if (ttsReady)
		{
			tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
		}
		else
		{
			Utils.makeLog("Tried to speak before TTS was ready");
		}
	}
	
	public void readOutJourneyInformation(View v)
	{
		speak(journey.getSummary());
	}
}