package src;

/**
 * Represents a single leg of a journey: a start and end point and information about how to get 
 * from the former to the latter.
 */
public class JourneyLeg 
{
	private int distanceMetres;
	private CoOrdinate start;
	private CoOrdinate end;
	private String fullInstructions;
	private Direction direction;
	private Direction nextDirection;
	
	public JourneyLeg(String distanceMetres, 
					  CoOrdinate start, 
					  CoOrdinate end, 
					  String instructions) throws DirectionException
	{
		this.distanceMetres = Integer.valueOf(distanceMetres);
		this.start = start;
		this.end = end;
		this.fullInstructions = instructions;
		this.direction = Direction.parseDirection(instructions);
	}
	
	public int getDistanceMetres() {return distanceMetres;}
	public CoOrdinate getStart() {return start;}
	public CoOrdinate getEnd() {return end;}
	public String getFullInstructions() {return fullInstructions;}
	public Direction getDirection(){return direction;}
	
	public String getSimpleInstruction()
	{
		return "Go " + distanceMetres + "m and then " + (nextDirection == null ? " you're there!" : nextDirection.toString());
	}
	
	public void setNextDirection(Direction d)
	{
		nextDirection = d;
	}
}
