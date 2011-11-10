package interfaces;

import src.Journey;

public interface DirectionParser 
{
	/**
	 * Parse out a journey from some data.
	 * @param data The data containing the details of the journey.
	 * @return a journey.
	 */
	public Journey parseJSONDirections(String data);

}
