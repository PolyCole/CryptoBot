/*
 * Author: Cole Polyak
 * 19 October 2018
 * Currency.java
 * 
 * This class holds the information for each currency. 
 */

public class Currency 
{
	private String name;
	private String symbol;

	private double price;
	private double dayChange;
	private double weekChange;
	
	public Currency(String name, String symbol, double price, double dayChange, double weekChange)
	{
		this.name = name;
		this.symbol = symbol;
		this.price = price;
		this.dayChange = dayChange;
		this.weekChange = weekChange;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	public double getDayChange() {
		return dayChange;
	}

	public void setDayChange(double dayChange) {
		this.dayChange = dayChange;
	}
	
	public double getWeekChange() {
		return weekChange;
	}

	public void setWeekChange(double weekChange) {
		this.weekChange = weekChange;
	}
	
	public String toString()
	{
		return name + ":" + symbol + ":" + price;
	}
}