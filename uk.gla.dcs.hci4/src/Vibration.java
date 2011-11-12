package src;

import android.os.Vibrator;

public class Vibration implements Runnable
{
	public static long NO_WAIT              = 0;
	public static long VERY_LONG_VIBRATION  = 10000;
	public static long LONG_VIBRATION       = 1000;
	public static long SHORT_VIBRATION      = 500;
	public static long SHORT_PAUSE          = 500;
	
	private final long[] pattern;
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
			// Android doesn't cope well with sleeps
			pattern = new long[]{SHORT_PAUSE, SHORT_VIBRATION, SHORT_PAUSE, SHORT_VIBRATION, SHORT_PAUSE, 
					            SHORT_VIBRATION, SHORT_PAUSE, SHORT_VIBRATION, SHORT_PAUSE, 
					            SHORT_VIBRATION, SHORT_PAUSE, SHORT_VIBRATION};
			vibrator = v;
			loopForever = true;
			proximityAlert = this;
			
			Thread t = new Thread(this);
			t.start();			
		}		
	}
	
	public static void stopProximityAlert()
	{
		Utils.makeLog("Proximity alert cancelled");
		proximityAlert = null;
	}

	@Override
	public void run() 
	{	
		boolean doLoop = true;
		
		while(doLoop)
		{
			vibrator.vibrate(pattern, -1);
			
			doLoop = loopForever && (proximityAlert == this);
		}		
	}
}
