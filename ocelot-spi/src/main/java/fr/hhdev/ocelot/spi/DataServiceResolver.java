/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.spi;

/**
 * Implemente this class to do a resolver
 * @author hhfrancois
 */
public interface DataServiceResolver {

	/**
	 * Return a class for string in argument
	 * @param dataService
	 * @return
	 * @throws DataServiceException 
	 */
	Object resolveDataService(String dataService) throws DataServiceException;

}
