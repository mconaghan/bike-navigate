package test;

import interfaces.DirectionParser;
import src.BikeDirectActivity;
import src.GoogleDirectionsJSONParser;
import src.GoogleDirectionsQuery;
import src.Journey;
import android.test.ActivityInstrumentationTestCase2;

public class GoogleDirectionsQueryTest extends ActivityInstrumentationTestCase2<BikeDirectActivity>
{
	public GoogleDirectionsQueryTest() 
	{
		super("uk.ac.gla.dcs.hci4.src", BikeDirectActivity.class);
	}
		
	@Override
	public void setUp() throws Exception 
    {
		super.setUp();      
	}
	
	public void testBasicQuery() throws Exception
	{
		GoogleDirectionsQuery q = new GoogleDirectionsQuery("G206EZ,Glagow", "G128LT,Glasgow");
		String directions = q.queryGoogleDirections();
		
		DirectionParser p = new GoogleDirectionsJSONParser();
		Journey journey = p.parseJSONDirections(directions);
		
		assertEquals(5, journey.getLegs().size());
		//TODO elaborate on this.
	}
}
