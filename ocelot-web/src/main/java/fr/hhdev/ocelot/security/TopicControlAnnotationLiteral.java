/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.security;

import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author hhfrancois
 */
public class TopicControlAnnotationLiteral extends AnnotationLiteral<TopicControl> implements TopicControl {
	private static final long serialVersionUID = 1L;
	
	private final String value;

	public TopicControlAnnotationLiteral(String value) {
		this.value = value;
	}
	
	

	@Override
	public String value() {
		return value;
	}
	
}
