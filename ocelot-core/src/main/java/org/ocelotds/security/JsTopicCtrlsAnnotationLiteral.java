/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.security;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.JsTopicControl;
import org.ocelotds.annotations.JsTopicControls;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class JsTopicCtrlsAnnotationLiteral extends AnnotationLiteral<JsTopicControls> implements JsTopicControls {

	private static final long serialVersionUID = 1L;

	@Override
	public JsTopicControl[] value() {
		return new JsTopicControl[]{};
	}

}
