package src;

import java.io.IOException;
import java.io.InputStream;
import android.content.res.AssetManager;
import interfaces.LocationReader;

/**
 * LocationReader while takes its location from a file.
 */
public class FileLocationReader implements LocationReader
{

	private String locationFilename;
	private String[] readLines = null;
	private int lastReadLine;
	private BikeDirectActivity app;
	
	public FileLocationReader(String filename, BikeDirectActivity activity)
	{
		app = activity;
		locationFilename = filename;
		
		readFile();	

		lastReadLine = -1;
	}
	
	private void readFile()
	{
        try
        {
        	AssetManager am = app.getAssets();
            InputStream is = am.open(locationFilename);        
            readLines = Utils.convertStreamToString(is).split("\\n");
        }
        catch(IOException e)
        {
        	throw new RuntimeException("Cannot read file " + locationFilename + " : " + e.getLocalizedMessage());
        }
	}
	
	@Override
	public CoOrdinate getCurrentLocation() 
	{
		if (readLines == null || readLines.length == 0)
		{
			throw new RuntimeException("No file to read location from");
		}
		
		// If we've reached the end of the file, read read it
		if ((readLines.length - 1) <= lastReadLine)
		{
			readFile();
		}		
		
		// If we're not at the last line, increment the pointer
		if ((readLines.length - 1) > lastReadLine)
		{
			lastReadLine++;
		}
		
		String line = readLines[lastReadLine];
		String[] tokens = line.split(":");
		
		if (tokens.length != 2)
		{
			throw new RuntimeException("Bad line in location input file");
		}
		
		CoOrdinate c = new CoOrdinate(tokens[0], tokens[1]);	
		
		return c;
	}

}
