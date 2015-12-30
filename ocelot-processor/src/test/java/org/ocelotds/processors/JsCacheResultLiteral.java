/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.JsCacheResult;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class JsCacheResultLiteral extends AnnotationLiteral<JsCacheResult> implements JsCacheResult{
	private final String[] keys;

	public JsCacheResultLiteral(String... keys) {
		this.keys = keys;
	}

	@Override
	public int year() {
		return 0;
	}

	@Override
	public int month() {
		return 0;
	}

	@Override
	public int day() {
		return 0;
	}

	@Override
	public int hour() {
		return 0;
	}

	@Override
	public int minute() {
		return 0;
	}

	@Override
	public int second() {
		return 0;
	}

	@Override
	public int millisecond() {
		return 0;
	}

	@Override
	public String[] keys() {
		return this.keys;
	}
	
}
