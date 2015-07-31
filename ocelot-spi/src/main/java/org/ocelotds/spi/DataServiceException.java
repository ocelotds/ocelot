/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.spi;

/**
 *
 * @author hhfrancois
 */
public class DataServiceException extends Exception {

	public DataServiceException(String dataService) {
		super(dataService);
	}
	
	public DataServiceException(String dataService, Throwable t) {
		super(dataService, t);
	}
}
