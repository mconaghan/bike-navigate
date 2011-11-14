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
               Vibration.LONG_VIBRATION});
	
	final static Pattern TURN = Pattern.compile(".*(T|t)urn.*");
	final static Pattern SLIGHT = Pattern.compile(".*(S|s)light.*");
	final static Pattern KEEP = Pattern.compile(".*(K|k)eep.*");
	final static Pattern CONTINUE = Pattern.compile(".*(C|c)ontinue.*");
	final static Pattern EXIT = Pattern.compile(".*(E|e)xit.*");
	final static Pattern RIGHT = Pattern.compile(".*(R|r)ight.*");
	final static Pattern LEFT = Pattern.compile(".*(L|l)eft.*");	
	final static Pattern TAKE_THE = Pattern.compile(".*(T|t)ake the.*");
	final static Pattern FIRST = Pattern.compile(".*(1(st|ST)|(F|f)irst).*");
	final static Pattern SECOND = Pattern.compile(".*((2nd|ND)|(S|s)econd).*");
	final static Pattern THIRD = Pattern.compile(".*(3(rd|RD)|(T|t)hird).*");
	final static Pattern FOURTH = Pattern.compile(".*(4(th|TH)|(F|f)ourth).*");
	final static Pattern FIFTH = Pattern.compile(".*(5(th|TH)|(F|f)ifth).*");
	final static Pattern SIXTH = Pattern.compile(".*(6(th|TH)|(S|s)ixth).*");
	
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
		Matcher keepMatcher = KEEP.matcher(s);
		Matcher continueMatcher = CONTINUE.matcher(s);
		Matcher exitMatcher = EXIT.matcher(s);
		Matcher leftMatcher = LEFT.matcher(s);
		Matcher rightMatcher = RIGHT.matcher(s);
		Matcher takeTheMatcher = TAKE_THE.matcher(s);
		Matcher firstMatcher = FIRST.matcher(s);
		Matcher secondMatcher = SECOND.matcher(s);
		Matcher thirdMatcher = THIRD.matcher(s);
		Matcher fourthMatcher = FOURTH.matcher(s);
		Matcher fifthMatcher = FIFTH.matcher(s);
		Matcher sixthMatcher = SIXTH.matcher(s);
		
		if (turnMatcher.find())
		{
			if      (leftMatcher.find())  { d = TURN_LEFT;}
			else if (rightMatcher.find()) {	d = TURN_RIGHT;}
			else { throw new DirectionException("Don't know which way to turn");}
		}
		else if ((slightMatcher.find()) || (keepMatcher.find()))
		{
			// Treat "keep right" as "slight right"
			if      (leftMatcher.find())  { d = SLIGHT_LEFT;}
			else if (rightMatcher.find()) {	d = SLIGHT_RIGHT;}
			else { throw new DirectionException("Don't know which way to slight");}
		}
		else if ((exitMatcher.find()) ||(takeTheMatcher.find()))
		{
			if      (firstMatcher.find()) {d = FIRST_EXIT;}
			else if (secondMatcher.find()) {d = SECOND_EXIT;}
			else if (thirdMatcher.find()) {d = THIRD_EXIT;}
			else if (fourthMatcher.find()) {d = FOURTH_EXIT;}
			else if (fifthMatcher.find()) {d = FIFTH_EXIT;}
			else if (sixthMatcher.find()) {d = SIXTH_EXIT;}
			else if (leftMatcher.find())  {d = TURN_LEFT; }
			else if (rightMatcher.find()) {d = TURN_RIGHT;}
			
			else {throw new DirectionException("Could not parse 'take the' or 'exit' direction:." + s);}
		}
		else if (continueMatcher.find())
		{
			// This is deliberately last, some instructions say "TURN X then continue",
			// and we want to pull out the first bit and ignore the continue bit (which is implied but no command)
			d = CONTINUE_STRAIGHT;
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