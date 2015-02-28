package com.a9ski.jsf;

import java.io.Serializable;

public class Car implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4133345101570688721L;
	
	private final long id;
	private final int year;
	private final String color;
	private final String brand;
	private final int price;
	
	public Car(long id, String brand, String color, int year, int price) {
		super();
		this.id = id;
		this.brand = brand;
		this.color = color;
		this.year = year;
		this.price = price;
	}

	public long getId() {
		return id;
	}

	public int getYear() {
		return year;
	}

	public String getColor() {
		return color;
	}

	public String getBrand() {
		return brand;
	}

	public int getPrice() {
		return price;
	} 
}
