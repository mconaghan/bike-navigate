package src;

import interfaces.LocationReader;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Manages the gathering and reporting of the users location.
 */
public class LocationHandler implements LocationListener, LocationReader
{
	Location gLocation;
	LocationManager gLocationManager;
	boolean started = false;
	
	public LocationHandler(LocationManager lm)
	{
        gLocationManager = lm;
        gLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        started = true;
	}
	
	@Override
	public void stop()
	{
		if (started)
		{
			gLocationManager.removeUpdates(this);
		}		
	}
	
	@Override
	public void start()
	{
		if (!started)
		{
			gLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}		
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
}