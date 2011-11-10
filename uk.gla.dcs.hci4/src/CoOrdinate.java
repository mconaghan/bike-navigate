package src;

/**
 * A longitude/latitude co-ordinate.
 */
public class CoOrdinate 
{

	private double longitude;
	private double latitude;
	
	public CoOrdinate(String longitude, String lat)
	{
		this.longitude = Double.valueOf(longitude);
		latitude = Double.valueOf(lat);
	}
	
	public CoOrdinate(double longitude, double lat)
	{
		this.longitude = longitude;
		latitude = lat;
	}

	public double getLongitude() 
	{
		return longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}
}