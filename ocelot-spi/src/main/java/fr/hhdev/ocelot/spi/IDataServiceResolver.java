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
public interface IDataServiceResolver {

	/**
	 * Return a instance for class by contextual resolver
	 * @param <T>
	 * @param clazz
	 * @return a dataservice
	 * @throws DataServiceException 
	 */
	<T> T resolveDataService(Class<T> clazz) throws DataServiceException;
}
