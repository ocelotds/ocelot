/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.spi;

/**
 *
 * @author hhfrancois
 */
public interface DataServiceResolver {

	Object resolveDataService(String dataService) throws DataServiceException;

}
