/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test.dataservices;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
@Singleton
public class SingletonCDIDataService {
	
	private double d;
	
	@PostConstruct
	private void init() {
		d = Math.random();
	}

	public double getValue() {
		return d;
	}
	
	
}
