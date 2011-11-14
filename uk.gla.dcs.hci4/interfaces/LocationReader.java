package interfaces;

import src.CoOrdinate;

public interface LocationReader 
{
	CoOrdinate getCurrentLocation();
	
	/** Stop getting location **/
	void stop();
	
	/** start getting location info **/
	void start();
}
