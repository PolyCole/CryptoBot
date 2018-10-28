import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.github.Draylar.CMC_APIWrapper;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import org.json.*;

/*
 * Author: Cole Polyak
 * 19 October 2018
 * Main.java
 * 
 * This class provides functionality for the crypto currency twitter bot: @bot_coles
 * The bot wil tweet four times daily, detailing the highest changes in the value of a 
 * currency, either up or down.
 * 
 * The bot uses data provided by CoinMarketCap's API. 
 */

public class Main 
{
	private static HashMap<String, Currency> currencies = new HashMap<String, Currency>();
	private static ArrayList<Currency> maxDailyChanges = new ArrayList<Currency>(5);
	private static ArrayList<Currency> maxWeeklyChanges = new ArrayList<Currency>(5);

	// Allows us to log the activity and duties of the bot.
	private static Log log = new Log("log.txt");
	
	public static void main(String[] args)
	{		
		try
		{
			CMC_APIWrapper api = new CMC_APIWrapper();
			for(int i = 0; i < 365; ++i)
			{
				// Tweeting four times daily, seven days a week.
				for(int j = 0; j < 28; ++j)
				{
					updateData(api.getTopCurrencies(100));
					updateChangeMaxes(true);
					generateTweet("Daily");
					Thread.sleep(21600000);
				}

				// For our one weekly update.
				updateData(api.getTopCurrencies(100));
				updateChangeMaxes(false);
				generateTweet("Weekly");
			}

		}
		catch(InterruptedException e)
		{
			processErrorTweet();
			System.exit(1);
		}
	}

	// In case the program errors, so we're able to see it from the twitter feed.
	private static void processErrorTweet()
	{
		Twitter twitter = TwitterFactory.getSingleton();
		try
		{
			twitter.updateStatus("Encountered InterruptedException. Exiting.");
			writeToLog("Encountered InterruptedException", "severe");
			System.out.println("TWEETED ERROR");
		}
		catch(TwitterException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Method reads in the JSON information from the top 100 cryptocurrencies
	 * and updates or creates their filings as needed.
	 * @param input : The JSON file returned by the CMC api.
	 */
	private static void updateData(String input)
	{
		String[] inputSplit = input.split("},");

		// Ensuring each entry in the array is in JSON Format.
		for(int i = 0; i < inputSplit.length; ++i)
		{
			if(i == 0) inputSplit[0] = inputSplit[0].substring(1);
			if(i == inputSplit.length - 1)
			{
				inputSplit[i] = inputSplit[i].substring(0, inputSplit[i].indexOf(']'));
			}

			inputSplit[i] += "},";
		}

		JSONObject obj = null;

		// Processing each entry.
		for(int i = 0; i < inputSplit.length; ++i)
		{
			obj = new JSONObject(inputSplit[i]);
			String id = obj.getString("id");

			// Entry exists.
			if(currencies.get(id) != null)
			{
				updateEntry(id, obj);
			}
			// Need to create a new entry.
			else
			{
				createNewCurrency(id, obj);
			}

		}
	}

	/**
	 * This method updates the entry for an existing currency.
	 * @param id : The id of the currency to be updated.
	 * @param obj : The JSON Object of the new information.
	 */
	private static void updateEntry(String id, JSONObject obj)
	{
		Currency target = currencies.get(id);

		Double price = Double.parseDouble(obj.getString("price_usd"));
		Double dayChange = Double.parseDouble(obj.getString("percent_change_24h"));
		Double weekChange = Double.parseDouble(obj.getString("percent_change_7d"));

		// Updating.
		target.setPrice(price);
		target.setDayChange(dayChange);
		target.setWeekChange(weekChange);
	}

	/**
	 * This method creates a new entry for currency not in the map yet.
	 * @param id : The ID of the currency to be added.
	 * @param obj : The JSON object of the new information
	 */
	private static void createNewCurrency(String id, JSONObject obj)
	{
		String name = obj.getString("name");
		String symbol = obj.getString("symbol");
		Double price = Double.parseDouble(obj.getString("price_usd"));
		Double dayChange = Double.parseDouble(obj.getString("percent_change_24h"));

		Double weekChange;

		try
		{
			weekChange = Double.parseDouble(obj.getString("percent_change_7d"));
		}
		catch(JSONException e)
		{
			// Shoutout.
			System.err.println("\"Consider the situation handled.\" -Eddy Rogers");
			weekChange = 0.00;
		}


		Currency newCurrency = new Currency(name, symbol, price, dayChange, weekChange);

		currencies.put(id, newCurrency);
	}


	/**
	 * Updates either the daily maxes or the weekly maxes.
	 * @param isDailyChanges : Indicates which time frame maxes are to be updated.
	 */
	private static void updateChangeMaxes(boolean isDailyChanges)
	{
		ArrayList<Currency> target;
		
		if(isDailyChanges){
			maxDailyChanges = new ArrayList<Currency>(5);
			target = maxDailyChanges;
		}else{
			maxWeeklyChanges = new ArrayList<Currency>(5);
			target = maxWeeklyChanges;
		}

		// Iterates over hash map.
		for(Map.Entry<String, Currency> item : currencies.entrySet())
		{
			Currency current = item.getValue();

			// Split to help with readability.
			double tempChange = isDailyChanges ? current.getDayChange() : current.getWeekChange();
			double change = processChange(tempChange);

			for(int i = 0; i <= target.size(); ++i)
			{
				// In the case that the maxes haven't been populated yet.
				if(target.size() < 5) 
				{
					target.add(current);
					break;
				}

				if(i == target.size()) break;

				// Value of current spot in maxes.
				double compareTo = processChange(isDailyChanges ? target.get(i).getDayChange() : target.get(i).getWeekChange());

				// Adding our current change to the array.
				if(compareTo < change)
				{
					target.add(i, current);
					target.remove(target.size()-1);
					break;
				}
			}
		}
	}

	/**
	 * 
	 * @param input : Weekly or Daily change.
	 * @return : The change value flipped so we're able to ignore negativity.
	 */
	private static double processChange(double input)
	{
		return input < 0 ? input * -1 : input;
	}


	/**
	 * Helper method for generating the tweet.
	 * @param timeFrame : Whether it's daily or weekly.
	 */
	private static void generateTweet(String timeFrame)
	{
		if("Daily".equals(timeFrame)) generateTweet(timeFrame, true);
		else generateTweet(timeFrame, false);
	}

	/**
	 * 
	 * @param timeFrame : Whether it's daily or weekly.
	 * @param isDailyMaxes : Indicating again whether it's daily or weekly.
	 */
	private static void generateTweet(String timeFrame, boolean isDailyMaxes)
	{
		StringBuilder sb = new StringBuilder();

		Currency current = null;

		// Determining which array to parse.
		ArrayList<Currency> target = isDailyMaxes ? maxDailyChanges : maxWeeklyChanges;

		sb.append(timeFrame + " changes: \n");

		// Adding each currency.
		for(int i = 0; i < target.size(); ++i)
		{
			current = target.get(i);
			double change = isDailyMaxes ? current.getDayChange() : current.getWeekChange();

			sb.append(i+1 + ". " + current.getName() + "   " + change + "% : " + "($"+current.getPrice()+")\n");
		}

		sendTweet(sb.toString());	
	}

	/**
	 * Sends the tweet!
	 * @param input : Tweet to send
	 */
	private static void sendTweet(String input)
	{	

		ConfigurationBuilder cb = new ConfigurationBuilder();

		// Replace with respective api keys.
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("******************************")
		.setOAuthConsumerSecret("******************************")
		.setOAuthAccessToken("******************************")
		.setOAuthAccessTokenSecret("******************************");
		TwitterFactory tf = new TwitterFactory(cb.build());

		Twitter twitter = tf.getInstance();
		try
		{
			twitter.updateStatus(input);
			writeToLog(input, "info");
			System.out.println("Tweeted: " + input);
		}
		catch(TwitterException e)
		{
			e.printStackTrace();
		}

		System.out.println("Successfully tweeted!");
	}
	
	/**
	 * This method writes the input to the log file. 
	 * @param input : Message to be logged.
	 * @param severity : How severe the message is.
	 */
	private static void writeToLog(String input, String severity)
	{
		if("warning".equals(severity)) log.logger.warning(input);
		else if("severe".equals(severity)) log.logger.severe(input);
		else log.logger.info(input);
	}
}
