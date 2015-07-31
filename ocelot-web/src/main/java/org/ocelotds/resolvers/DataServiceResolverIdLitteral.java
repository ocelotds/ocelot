/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.resolvers;

import org.ocelotds.spi.DataServiceResolver;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Instance of Annotation
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class DataServiceResolverIdLitteral extends AnnotationLiteral<DataServiceResolver> implements DataServiceResolver {

 	private static final long serialVersionUID = 7844713893702557467L;
	
	private final String val;
	public DataServiceResolverIdLitteral(String val) {
		this.val = val;
	}
	@Override
	public String value() {
		return val;
	}
	
}
