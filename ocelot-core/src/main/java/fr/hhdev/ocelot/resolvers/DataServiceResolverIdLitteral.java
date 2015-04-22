/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.resolvers;

import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author hhfrancois
 */
public class DataServiceResolverIdLitteral extends AnnotationLiteral<DataServiceResolverId> implements DataServiceResolverId {

	private final String val;
	public DataServiceResolverIdLitteral(String val) {
		this.val = val;
	}
	@Override
	public String value() {
		return val;
	}
	
}
