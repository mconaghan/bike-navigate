package src;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

/** Wrapper  around Android Text to Speech engine.
 */
public class TextToSpeechHandler implements OnInitListener
{
	public static final int MY_DATA_CHECK_CODE = 0;
	
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private Activity activity;
    
    public TextToSpeechHandler(Activity activity)
    {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        activity.startActivityForResult(checkIntent, TextToSpeechHandler.MY_DATA_CHECK_CODE);
        
        this.activity = activity;
    }
    
    public void init(int requestCode, int resultCode, Intent data)
    {
    	if (requestCode == MY_DATA_CHECK_CODE) 
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) 
            {
                // success, create the TTS instance
                tts = new TextToSpeech(activity, this);
            } 
            else 
            {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                activity.startActivity(installIntent);
            }
        }
    }

	@Override
	//TTS is ready
	public void onInit(int arg0) 
	{
		Utils.makeLog("TTS is ready");
		tts.setLanguage(Locale.UK);
		ttsReady = true;
	}
	
	/** Say something to the user using Text To Speech. **/
	public void speak(String speech)
	{
		if (ttsReady)
		{
			tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
		}
		else
		{
			Utils.makeLog("Tried to speak before TTS was ready");
		}
	}
}
