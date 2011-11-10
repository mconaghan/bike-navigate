package src;

import android.os.Vibrator;

public class Vibration implements Runnable
{
	public static int LONG_VIBRATION  = 1000;
	public static int SHORT_VIBRATION = 500;
	public static int SHORT_PAUSE     = -500;
	
	private final int[] pattern;
	private final Vibrator vibrator;
	private final boolean loopForever;
	
	/*
	 * There must be at most one proximity alert vibration at any time, this is 
	 * used to police this and to allow existing proximity alert to be killed.
	 */
	private static Vibration proximityAlert = null;	

	/**
	 * Start a vibration to issue a direction command.
	 * @param d
	 * @param v
	 */
	public static void startDirectionVibration(Direction d, Vibrator v){new Vibration(d, v);}
	private Vibration(Direction d, Vibrator v)
	{
		pattern = d.getVibrationPattern();
		vibrator = v;
		loopForever = false;
		
		Utils.makeLog("New vibration for " + d.toString());
		
		if (pattern != null)
		{
			Thread t = new Thread(this);
			t.start();
		}		
	}
		
	/**
	 * Start the proximity alert vibration.
	 * @param v
	 */
	public static void startProximityAlert(Vibrator v)	{new Vibration(v);}	
	private Vibration(Vibrator v)
	{
		if (proximityAlert != null)
		{
			Utils.makeLog("Already issued proximity alert");
			pattern = null;
			vibrator = null;
			loopForever = false;
		}
		else
		{
			pattern = new int[]{SHORT_VIBRATION, SHORT_PAUSE};
			vibrator = v;
			loopForever = true;
			
			Thread t = new Thread(this);
			t.start();
			proximityAlert = this;
		}		
	}
	
	public static void stopProximityAlert()
	{
		proximityAlert = null;
	}

	@Override
	public void run() 
	{
		int ii = 0;		
		
		while((ii < pattern.length) && (proximityAlert == this))
		{
			if (pattern[ii] > 0)
			{
				Utils.makeLog("Vibrating for " + pattern[ii] + " milliseconds");
				vibrator.vibrate(pattern[ii]);
			}
			else
			{
				try 
				{
					Thread.sleep(-(pattern[ii]));
				} 
				catch (InterruptedException e) 
				{
					Utils.makeLog("Vibration thread was interrupted");
				}
			}	
			ii++;
			
			// If we are to loop forever then go pack to the start of the pattern
			if (loopForever && ii == pattern.length)
			{
				ii = 0;
			}
		}		
	}
}
