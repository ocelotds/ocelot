/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.spi;

/**
 * Dataservice exception
 * @author hhfrancois
 */
public class DataServiceException extends Exception {

	/**
	 * 
	 * @param dataService 
	 */
	public DataServiceException(String dataService) {
		super(dataService);
	}
	
	/**
	 * 
	 * @param dataService
	 * @param t 
	 */
	public DataServiceException(String dataService, Throwable t) {
		super(dataService, t);
	}
}
