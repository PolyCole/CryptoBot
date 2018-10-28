import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * Author: Cole Polyak
 * 28 October 2018
 * Log.java
 * 
 * This class provides functionality for logging the activity of the bot to 
 * file. 
 */

public class Log 
{
	// Logger and handler.
	public Logger logger;
	FileHandler fileHandler;
	
	public Log(String filename)
	{
		// Creating log file.
		File f = new File(filename);
		
		try
		{
			// Ensuring file exists.
			if(!(f.exists()))
			{
				f.createNewFile();
			}
			
			// Creating our handler.
			fileHandler = new FileHandler(filename, true);
			
			// Connecting our handler to our logger.
			logger = Logger.getLogger("CryptoBotLogger");
			logger.addHandler(fileHandler);
			
			// Creating and connecting formatter.
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
		} 
		catch (SecurityException e) 
		{
			System.err.println("SecurityException Encountered");
			e.printStackTrace();
			System.exit(1);
		} 
		catch (IOException e) 
		{
			System.err.println("IOException Encountered");
			e.printStackTrace();
			System.exit(1);
		}
			
	}
}
