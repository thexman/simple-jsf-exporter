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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.a9ski.jsf.exporter.DataTableExcelExporter;
import com.a9ski.jsf.exporter.DataTableExporterOptions;

@ManagedBean
@ViewScoped
public class CarsBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5074319983866348080L;

	private final static String[] colors;

	private final static String[] brands;

	static {
		colors = new String[10];
		colors[0] = "Black";
		colors[1] = "White";
		colors[2] = "Green";
		colors[3] = "Red";
		colors[4] = "Blue";
		colors[5] = "Orange";
		colors[6] = "Silver";
		colors[7] = "Yellow";
		colors[8] = "Brown";
		colors[9] = "Maroon";

		brands = new String[] { "BMW", "Mercedes", "Volvo", "Audi", "Renault", "Fiat", "Volkswagen", "Honda", "Jaguar", "Ford", "Opel", "Mazda", "Toyota" };
	}

	private final Random r = new Random(1024);

	private final List<Car> cars = new ArrayList<Car>();

	public CarsBean() {
		super();
	}

	private String random(final String[] items) {
		return items[r.nextInt(items.length)];
	}

	@PostConstruct
	public void init() {
		for (int i = 0; i < 100; i++) {
			cars.add(new Car(i, random(brands), random(colors), r.nextInt(15) + 2000, r.nextInt(30) * 1000));
		}
	}

	public List<Car> getCars() {
		return cars;
	}

	public Class<?> getExporter() {
		return DataTableExcelExporter.class;
	}
	
	public Serializable getExportOptions() {
		final DataTableExporterOptions options = new DataTableExporterOptions();
		options.setFirstHeaderRow(5);
		return options;
	}

}
