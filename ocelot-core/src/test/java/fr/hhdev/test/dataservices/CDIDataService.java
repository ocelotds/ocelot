/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.test.dataservices;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 *
 * @author hhfrancois
 */
@DataService(resolverid = Constants.Resolver.CDI)
public class CDIDataService {
	
	@Inject
	BeanManager beanManager;
	
	public BeanManager getBeanManager() {
		return beanManager;
	}

}
