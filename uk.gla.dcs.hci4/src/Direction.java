package src;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Direction 
{

	ARRIVED(new long[]{Vibration.VERY_LONG_VIBRATION}),
	CONTINUE_STRAIGHT(null),
	TURN_RIGHT(new long[]{Vibration.NO_WAIT,
                         Vibration.LONG_VIBRATION, 
			             Vibration.SHORT_PAUSE, 
			             Vibration.LONG_VIBRATION, 
			             Vibration.SHORT_PAUSE, 
			             Vibration.LONG_VIBRATION}),
	TURN_LEFT(new long[]{Vibration.NO_WAIT,
                        Vibration.LONG_VIBRATION}),
	SLIGHT_RIGHT(new long[]{Vibration.NO_WAIT,
                           Vibration.LONG_VIBRATION, 
                           Vibration.SHORT_PAUSE, 
                           Vibration.LONG_VIBRATION, 
                           Vibration.SHORT_PAUSE, 
                           Vibration.LONG_VIBRATION,
                           Vibration.SHORT_PAUSE,
                           Vibration.SHORT_VIBRATION}),
	SLIGHT_LEFT(new long[]{Vibration.NO_WAIT,
                          Vibration.LONG_VIBRATION, 
                          Vibration.SHORT_PAUSE, 
                          Vibration.SHORT_VIBRATION}),
	FIRST_EXIT(new long[]{Vibration.NO_WAIT,
                         Vibration.LONG_VIBRATION}),
	SECOND_EXIT(new long[]{Vibration.NO_WAIT,
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION}),
	THIRD_EXIT(new long[]{Vibration.NO_WAIT,
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION}),
	FOURTH_EXIT(new long[]{Vibration.NO_WAIT,
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION}),
	FIFTH_EXIT(new long[]{Vibration.NO_WAIT,
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION, 
                Vibration.SHORT_PAUSE, 
                Vibration.LONG_VIBRATION}),
	SIXTH_EXIT(new long[]{Vibration.NO_WAIT,
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION}),
	SEVENTH_EXIT(new long[]{Vibration.NO_WAIT,
                 Vibration.LONG_VIBRATION, 
                 Vibration.SHORT_PAUSE, 
                 Vibration.LONG_VIBRATION, 
                 Vibration.SHORT_PAUSE, 
                 Vibration.LONG_VIBRATION, 
                 Vibration.SHORT_PAUSE, 
                 Vibration.LONG_VIBRATION, 
                 Vibration.SHORT_PAUSE, 
                 Vibration.LONG_VIBRATION, 
                 Vibration.SHORT_PAUSE, 
                 Vibration.LONG_VIBRATION, 
                 Vibration.SHORT_PAUSE, 
                 Vibration.LONG_VIBRATION}),
	EIGHT_EXIT(new long[]{Vibration.NO_WAIT,
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION}),
	NINTH_EXIT(new long[]{Vibration.NO_WAIT,
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION, 
               Vibration.SHORT_PAUSE, 
               Vibration.LONG_VIBRATION});
	
	final static Pattern TURN = Pattern.compile(".*(T|t)urn.*");
	final static Pattern SLIGHT = Pattern.compile(".*(S|s)light.*");
	final static Pattern CONTINUE = Pattern.compile(".*(C|c)ontinue.*");
	final static Pattern EXIT = Pattern.compile(".*(E|e)xit.*");
	final static Pattern RIGHT = Pattern.compile(".*(R|r)ight.*");
	final static Pattern LEFT = Pattern.compile(".*(L|l)eft.*");	
	final static Pattern TAKE_THE = Pattern.compile(".*(T|t)ake the.*");
	
	private long[] vibrationPattern;
	
	private Direction (long vibrationPattern[])
	{
		this.vibrationPattern = vibrationPattern;
	}
	
	public long[] getVibrationPattern()
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
		Matcher takeTheMatcher = TAKE_THE.matcher(s);
		
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
		else if (takeTheMatcher.find())
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
				throw new DirectionException("Not implemented roundabouts yet(2).");
			}
		}
		else
		{
			d = null;
		}
		
		return d;
	}	
	
	public String toString()
	{
		// Replace underscores with spaces.
		return this.name().replaceAll("_", " ").toLowerCase();		
	}
}