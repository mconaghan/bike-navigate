package src;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class GoogleDirectionsQuery 
{	
	private String from;
	private String to;
	
	public GoogleDirectionsQuery(String from, String to)
	{		
		this.from = URLEncoder.encode(from);
		this.to = URLEncoder.encode(to);
	}
	
    public String queryGoogleDirections() throws RouteNotFoundException
    {
    	String template = "http://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&sensor=true&region=uk&avoid=highways&mode=driving&units=metric&alternatives=false";
    	String response = "";
    	
    	try
    	{
    		String requestURI = String.format(template, from, to);
    		Utils.makeLog("Sending request to Google Directions : " + requestURI);
    		
    		response = executeHttpGet(requestURI);
    		Utils.makeLog("Resposne from Google Directions : " + response);
		} 
    	catch (ClientProtocolException e) 
    	{
    		Utils.makeLog("ClientProtocolException from Google Directions: " + e.toString());
    		throw new RouteNotFoundException(e);
		} 
    	catch (URISyntaxException e) 
    	{
    		Utils.makeLog("URISyntaxException from Google Directions: " + e.toString());
    		throw new RouteNotFoundException(e);
		} 
    	catch (IOException e) 
    	{
    		Utils.makeLog("IOException from Google Directions: " + e.toString());
    		throw new RouteNotFoundException(e);
		}    	
    	
    	if (response.contains("NOT_FOUND") || response.contains("INVALID_REQUEST"))
    	{
    		Utils.makeLog("Google coudn't find route. From '" + from + "' to '" + to + "'. Googe said: " + response);
    		throw new RouteNotFoundException("Google said: " + response);
    	}
    	
    	return response;
    }
    
    /**
     * Taken from: http://w3mentor.com/learn/java/android-development/android-http-services/example-of-http-get-request-using-httpclient-in-android/
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws ClientProtocolException 
     * @throws Exception
     */
    private String executeHttpGet(String uri) throws URISyntaxException, 
                                                     ClientProtocolException, 
                                                     IOException
    {
        InputStream in = null;
        String responseString = "";
        
		try 
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(uri));
            HttpResponse response = client.execute(request);
            in = response.getEntity().getContent();
            responseString = Utils.convertStreamToString(in);             
        } 
		catch (Exception e)
		{
			Utils.makeLog("Exception in executeHttpGet: " + e.toString());
		}
		finally 
		{            
			if (in != null) 
			{                
				try 
				{
                    in.close();
                } 
				catch (IOException e) 
				{
                    e.printStackTrace();
                }
            }
        }		
		return responseString;
    }
}