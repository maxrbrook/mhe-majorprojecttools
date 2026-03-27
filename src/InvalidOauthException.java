/*
	Custom Class for handling API status errors (>= 300)
*/
class InvalidOauthException extends Exception
{
	public InvalidOauthException(String msg)
	{
		super (msg);
	}
}
