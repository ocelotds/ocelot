/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.security;

import fr.hhdev.ocelot.annotations.JsTopicAccessControl;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author hhfrancois
 */
public class JsTopicACAnnotationLiteral extends AnnotationLiteral<JsTopicAccessControl> implements JsTopicAccessControl {
	private static final long serialVersionUID = 1L;
	
	private final String value;

	public JsTopicACAnnotationLiteral(String value) {
		this.value = value;
	}
	
	

	@Override
	public String value() {
		return value;
	}
	
}
