package src;

/**
 * Represents a single leg of a journey: a start and end point and information about how to get 
 * from the former to the latter.
 */
public class JourneyLeg 
{
	private int distanceLeftMetres;
	private int totalLegDistance;
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
		distanceLeftMetres = Integer.valueOf(distanceMetres);
		totalLegDistance = this.distanceLeftMetres;
		this.start = start;
		this.end = end;
		fullInstructions = instructions;
		direction = Direction.parseDirection(instructions);
	}
	
	public int getDistanceLeftMetres() {return distanceLeftMetres;}
	public int getTotalDistance() {return totalLegDistance;}
	public int getDistanceTravelledMetres() {return (totalLegDistance - distanceLeftMetres);}
	public CoOrdinate getStart() {return start;}
	public CoOrdinate getEnd() {return end;}
	public String getFullInstructions() {return fullInstructions;}
	public Direction getNextDirection(){return nextDirection;}
	public Direction getDirection(){return direction;}
	
	public String getSimpleInstruction()
	{
		return "Go " + distanceLeftMetres + "m and then " + (nextDirection == null ? " you're there!" : nextDirection.toString());
	}
	
	public void setNextDirection(Direction d){nextDirection = d;}
	public void setDistanceLeft(int distance){distanceLeftMetres = distance;};
}
