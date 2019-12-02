package parser;

// Wav file Exception class
// A.Greensted
// http://www.labbookpages.co.uk

public class WavFileException extends Exception
{
	private static final long serialVersionUID = 6644199519150023984L;

	public WavFileException()
	{
		super();
	}

	public WavFileException(String message)
	{
		super(message);
	}

	public WavFileException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WavFileException(Throwable cause) 
	{
		super(cause);
	}
}
