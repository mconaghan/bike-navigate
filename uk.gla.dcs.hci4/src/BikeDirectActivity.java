/* TODO
 * Remove formatting from instructions
 * Implement distance travlled
 * Put speed in km/h
 * Make text visible on start screen
 * Fix instruction order - currently don't get instruction until after passed the end point
 * Tweak distance values, being there needs  to be 10m
 * Add view to see overview of directions
 * Add button to read out position and how long left
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class BikeDirectActivity extends Activity implements LocationListener, LocationReader, NavigationActivity
{
	Location gLocation;
	LocationManager gLocationManager;
	
	TextView distanceLeftLabel;
    TextView distanceTravelledLabel;
    TextView directionLabel;
    TextView speedLabel;
    TableLayout journeyStepsTable;
    
    Vibrator vibrator;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // By default Network IO is disabled on main thread.  Nothing else to do
        // while waiting for Google, so disable this and just block waiting for
        // directions.
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .permitNetwork()
        .build());
        setContentView(R.layout.start_view);      
        
        gLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Route cannot be found, please try again")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog dialog = builder.create();
		
		return dialog;
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

    	Journey journey = null;
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
	    	final TextView routeLabel = (TextView)findViewById(R.id.route_label);
	        
	        distanceLeftLabel = (TextView)findViewById(R.id.distance_left_label);
	        distanceTravelledLabel = (TextView)findViewById(R.id.distance_travelled_label);
	        directionLabel = (TextView)findViewById(R.id.direction_label);
	        speedLabel = (TextView)findViewById(R.id.speed_label);
			
			// And display the results
	        routeLabel.setText("From " + startString + " to " + endString);
	    	distanceLeftLabel.setText("Distance left: " + String.valueOf(journey.getDistance()));
	    	distanceTravelledLabel.setText("Distance travelled: 0");			
	    	
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
    public void issueDirection(JourneyLeg jl)
    {    	
    	Utils.makeLog("issueDirection: " + jl.getSimpleInstruction());
		setDirection(jl.getSimpleInstruction());
	    Vibration.startDirectionVibration(jl.getDirection(), vibrator);    	
    }
    
    public void startProximityAlert()
    {
    	Utils.makeLog("startProximityAlert");
    	Vibration.startProximityAlert(vibrator);
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
		showDialog(0);//TODO remove magic number
	}
	
	//HELP VIEW METHODS
	public void demoLeftTurn(View v)   {Vibration.startDirectionVibration(Direction.TURN_LEFT,    vibrator);}	
	public void demoRightTurn(View v)  {Vibration.startDirectionVibration(Direction.TURN_RIGHT,   vibrator);}
	public void demoSlightLeft(View v) {Vibration.startDirectionVibration(Direction.SLIGHT_LEFT,  vibrator);}	
	public void demoSlightRight(View v){Vibration.startDirectionVibration(Direction.SLIGHT_RIGHT, vibrator);}	
	//TODO more of these
	
	//CHANGE VIEW METHODS
	public void displayJourneyOverView(View v){setContentView(journeyStepsTable);}
    public void displayHelpView(View v){setContentView(R.layout.help_view);}    
    public void displayStartView(View v){setContentView(R.layout.start_view);}
}