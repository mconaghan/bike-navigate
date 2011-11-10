package src;

public class RouteNotFoundException extends Exception
{
	private static final long serialVersionUID = -3396389762303897277L;
	
	public RouteNotFoundException(String error)
	{
		super(error);
	}
	
	public RouteNotFoundException(Exception e)
	{
		super(e);
	}
}
