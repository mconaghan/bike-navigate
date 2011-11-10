package src;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Direction 
{

	CONTINUE_STRAIGHT(null),
	TURN_RIGHT(new int[]{Vibration.LONG_VIBRATION, 
			             Vibration.SHORT_PAUSE, 
			             Vibration.LONG_VIBRATION, 
			             Vibration.SHORT_PAUSE, 
			             Vibration.LONG_VIBRATION}),
	TURN_LEFT(new int[]{Vibration.LONG_VIBRATION}),
	SLIGHT_RIGHT(new int[]{Vibration.LONG_VIBRATION, 
                           Vibration.SHORT_PAUSE, 
                           Vibration.LONG_VIBRATION, 
                           Vibration.SHORT_PAUSE, 
                           Vibration.LONG_VIBRATION,
                           Vibration.SHORT_PAUSE,
                           Vibration.SHORT_VIBRATION}),
	SLIGHT_LEFT(new int[]{Vibration.LONG_VIBRATION, 
                          Vibration.SHORT_PAUSE, 
                          Vibration.SHORT_VIBRATION});
	//TODO
//	FIRST_EXIT,
//	SECOND_EXIT,
//	THIRD_EXIT,
//	FOURTH_EXIT,
//	FIFTH_EXIT,
//	SIXTH_EXIT,
//	SEVENTH_EXIT,
//	EIGHT_EXIT,
//	NINTH_EXIT;
	
	final static Pattern TURN = Pattern.compile(".*(T|t)urn.*");
	final static Pattern SLIGHT = Pattern.compile(".*(S|s)light.*");
	final static Pattern CONTINUE = Pattern.compile(".*(C|c)ontinue.*straight.*");
	final static Pattern EXIT = Pattern.compile(".*(E|e)xit.*");
	final static Pattern RIGHT = Pattern.compile(".*(R|r)ight.*");
	final static Pattern LEFT = Pattern.compile(".*(L|l)eft.*");
	
	private int[] vibrationPattern;
	
	private Direction (int vibrationPattern[])
	{
		this.vibrationPattern = vibrationPattern;
	}
	
	public int[] getVibrationPattern()
	{
		return vibrationPattern;
	}
	
	public static Direction parseDirection(String s) throws DirectionException
	{
		Direction d;
		
		Matcher turnMatcher = TURN.matcher(s);
		Matcher slightMatcher = SLIGHT.matcher(s);
		Matcher continueMatcher = CONTINUE.matcher(s);
		Matcher exitMatcher = EXIT.matcher(s);
		Matcher leftMatcher = LEFT.matcher(s);
		Matcher rightMatcher = RIGHT.matcher(s);
		
		if (turnMatcher.find())
		{
			if (leftMatcher.find())
			{
				d = TURN_LEFT;
			}
			else if (rightMatcher.find())
			{
				d = TURN_RIGHT;				
			}
			else
			{
				throw new DirectionException("Don't know which way to turn");
			}
		}
		else if (slightMatcher.find())
		{
			if (leftMatcher.find())
			{
				d = SLIGHT_LEFT;
			}
			else if (rightMatcher.find())
			{
				d = SLIGHT_RIGHT;				
			}
			else
			{
				throw new DirectionException("Don't know which way to slight");
			}
		}
		else if (continueMatcher.find())
		{
			d = CONTINUE_STRAIGHT;
		}
		else if (exitMatcher.find())
		{
			throw new DirectionException("Not implemented roundabouts yet.");
		}
		else
		{
			throw new DirectionException("Cannot parse direction");
		}
		
		return d;
	}	
}
