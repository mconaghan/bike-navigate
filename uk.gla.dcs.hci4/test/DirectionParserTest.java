package test;

import interfaces.DirectionParser;
import java.io.InputStream;
import java.util.List;
import src.BikeDirectActivity;
import src.Direction;
import src.DirectionException;
import src.Journey;
import src.JourneyLeg;
import src.GoogleDirectionsJSONParser;
import src.Utils;
import android.content.res.AssetManager;
import android.test.ActivityInstrumentationTestCase2;

public class DirectionParserTest extends ActivityInstrumentationTestCase2<BikeDirectActivity>
{
	
	private AssetManager am;
	private String testdata1;
	
	public DirectionParserTest() 
	{
		super("uk.ac.gla.dcs.hci4.src", BikeDirectActivity.class);
	}
	
	@Override
    public void setUp() throws Exception {
        super.setUp();
        
        //Read test file into memory
        BikeDirectActivity activity = this.getActivity();
        am = activity.getAssets();
        InputStream is = am.open("g206ez-g767rn");        
        testdata1 = Utils.convertStreamToString(is);       
    }
	
	/**
	 * Test parsing some pre-fetched JSON directions.
	 */
	public void testParsing1() throws Exception
	{
		
		DirectionParser p = new GoogleDirectionsJSONParser();
		Journey journey = p.parseJSONDirections(testdata1);
		List<JourneyLeg> directions = journey.getLegs();
		assertEquals(10, directions.size());
		
		assertEquals(244, directions.get(0).getDistanceMetres());
		assertEquals(507, directions.get(1).getDistanceMetres());
		assertEquals(357, directions.get(2).getDistanceMetres());
		assertEquals(101, directions.get(3).getDistanceMetres());
		assertEquals(397, directions.get(4).getDistanceMetres());
		assertEquals(1127, directions.get(5).getDistanceMetres());
		assertEquals(785, directions.get(6).getDistanceMetres());
		assertEquals(7828, directions.get(7).getDistanceMetres());
		assertEquals(249, directions.get(8).getDistanceMetres());
		assertEquals(262, directions.get(9).getDistanceMetres());
		
		//First direction is not understood, expect an exception
		try
		{
			Direction.parseDirection(directions.get(0).getFullInstructions());
			fail("Should have failed to parse");
		}
		catch (DirectionException e)
		{
			//expected
		}
		
		assertEquals(Direction.TURN_LEFT, Direction.parseDirection(directions.get(1).getFullInstructions()));		
		assertEquals(Direction.SLIGHT_RIGHT, Direction.parseDirection(directions.get(2).getFullInstructions()));
		assertEquals(Direction.TURN_LEFT, Direction.parseDirection(directions.get(3).getFullInstructions()));
		assertEquals(Direction.CONTINUE_STRAIGHT, Direction.parseDirection(directions.get(4).getFullInstructions()));
		assertEquals(Direction.SLIGHT_LEFT, Direction.parseDirection(directions.get(5).getFullInstructions()));
		assertEquals(Direction.TURN_LEFT, Direction.parseDirection(directions.get(6).getFullInstructions()));
		assertEquals(Direction.TURN_RIGHT, Direction.parseDirection(directions.get(7).getFullInstructions()));
		assertEquals(Direction.TURN_LEFT, Direction.parseDirection(directions.get(8).getFullInstructions()));
		
		//Last direction is not understood, expect an exception
		try
		{
			Direction.parseDirection(directions.get(9).getFullInstructions());
			fail("Should have failed to parse");
		}
		catch (DirectionException e)
		{
			//expected
		}
		
		int totalDistance = 0;
		
		for (JourneyLeg d : directions)
		{
			int dist = d.getDistanceMetres();
			
			double calculatedDist = Utils.distFrom(d.getStart(), d.getEnd());
			
			// check that calculated distance is close enough to reported distance
			double diff = calculatedDist - dist;
			double error = diff / dist;
			assertEquals(true, error < 0.5);
			assertEquals(true, error > -0.5);
			
			totalDistance += calculatedDist;
		}		
		
		double totalDiff = totalDistance - journey.getDistance();
		assertEquals(true, totalDiff < 500);
		assertEquals(true, totalDiff > -500);
	}
}