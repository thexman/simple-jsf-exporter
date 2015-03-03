/*
 * #%L
 * Simple JSF Exporter Primefaces Demo
 * %%
 * Copyright (C) 2015 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

	public Car(final long id, final String brand, final String color, final int year, final int price) {
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
