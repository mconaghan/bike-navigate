package test;

import src.BikeDirectActivity;
import src.CoOrdinate;
import src.FileLocationReader;
import android.test.ActivityInstrumentationTestCase2;

public class FileLocationReaderTest extends ActivityInstrumentationTestCase2<BikeDirectActivity>
{
	public FileLocationReaderTest() 
	{
		super("uk.ac.gla.dcs.hci4.src", BikeDirectActivity.class);
	}
		
	@Override
	public void setUp() throws Exception 
    {
		super.setUp();      
	}
	
	public void testBasicQuery() throws Exception
	{  
        
		FileLocationReader r = new FileLocationReader("location.txt", this.getActivity());
		
		CoOrdinate c1 = new CoOrdinate("55.87471000000001", "-4.257560000000001");
		CoOrdinate c2 = new CoOrdinate("55.87471000000001", "-4.25756000000001");
		CoOrdinate c3 = new CoOrdinate("55.87471000070001", "-4.25756000000001");
		
		assertEquals(r.getCurrentLocation(), c1);
		assertEquals(r.getCurrentLocation(), c2);
		assertEquals(r.getCurrentLocation(), c3);
		assertEquals(r.getCurrentLocation(), c3);
	}
}