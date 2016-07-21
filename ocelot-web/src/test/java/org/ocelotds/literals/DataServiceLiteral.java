/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.literals;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.DataService;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class DataServiceLiteral extends AnnotationLiteral<DataService> implements DataService {

	private static final long serialVersionUID = 1L;

	@Override
	public String resolver() {
		return "cdi";
	}

	@Override
	public String name() {
		return "";
	}
}
