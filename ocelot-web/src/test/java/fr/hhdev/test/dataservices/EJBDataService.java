/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test.dataservices;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.OcelotI18nServices;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheRemove;
import fr.hhdev.test.Result;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@Stateless
@LocalBean
@DataService(resolver = Constants.Resolver.EJB)
public class EJBDataService implements GetValue {
	private static final Logger logger = LoggerFactory.getLogger(EJBDataService.class);

	private double d;

	@Inject
	private OcelotI18nServices ocelotServices;

	@PostConstruct
	protected void init() {
		d = Math.random();
	}

	@Override
	public double getValue() {
		return d;
	}

	public String getLocaleHello(String who) {
		return ocelotServices.getLocalizedMessage("test", "HELLOGUY", new Object[]{who});
	}

	@Override
	public void setValue(double d) {
		this.d = d;
	}
	
	@JsCacheRemove(cls = EJBDataService.class , methodName = "getLocaleHello", keys = {"a","r.integer"})
	public void generateCleanCacheMessage(String a, Result r) {
		
	} 
}
