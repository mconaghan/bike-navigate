package test;

import interfaces.DirectionParser;
import src.BikeDirectActivity;
import src.CoOrdinate;
import src.Direction;
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
		GoogleDirectionsQuery q = new GoogleDirectionsQuery("G206EX,Glagow", "G11SS,Glasgow");
		String directions = q.queryGoogleDirections();
		
		DirectionParser p = new GoogleDirectionsJSONParser();
		Journey journey = p.parseJSONDirections(directions);
		
		assertEquals(13, journey.getLegs().size());
		assertEquals("Go 238m and then turn left", journey.getNextLeg().getSimpleInstruction());
		assertEquals(true, new CoOrdinate(-4.274740,55.87312000000001).veryCloseTo(journey.getCurrentLeg().getEnd()));
		assertEquals(Direction.TURN_LEFT, journey.getCurrentLeg().getNextDirection());
		
		//TODO elaborate on this.
	}
}
