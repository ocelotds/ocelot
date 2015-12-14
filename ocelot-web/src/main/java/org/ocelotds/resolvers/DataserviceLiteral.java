/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.resolvers;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.DataService;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class DataserviceLiteral extends AnnotationLiteral<DataService> implements DataService {

	@Override
	public String resolver() {
		return "";
	}

	@Override
	public String name() {
		return "";
	}

	
}
