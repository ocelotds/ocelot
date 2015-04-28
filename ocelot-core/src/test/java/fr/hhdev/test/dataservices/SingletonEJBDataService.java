/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test.dataservices;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;


/**
 *
 * @author hhfrancois
 */
@DataService(resolverid = Constants.Resolver.EJB)
@Singleton
public class SingletonEJBDataService {
	private double d;
	
	@PostConstruct
	private void init() {
		d = Math.random();
	}

	public double getValue() {
		return d;
	}
}
