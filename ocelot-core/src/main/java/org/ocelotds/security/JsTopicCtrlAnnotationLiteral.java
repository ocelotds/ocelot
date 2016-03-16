/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.JsTopicControl;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class JsTopicCtrlAnnotationLiteral extends AnnotationLiteral<JsTopicControl> implements JsTopicControl {
	private static final long serialVersionUID = 1L;
	
	private final String value;

	public JsTopicCtrlAnnotationLiteral(String value) {
		this.value = value;
	}
	
	

	@Override
	public String value() {
		return value;
	}
	
}
