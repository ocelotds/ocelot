/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.cache;

import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.annotations.JsCacheResult;

/**
 *
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class LiteralJsCacheResult  extends AnnotationLiteral<JsCacheResult> implements JsCacheResult {

	int year = 0;
	int month = 0;
	int day = 0;
	int hour = 0;
	int minute = 0;
	int second = 0;
	int millisecond = 0;

	public LiteralJsCacheResult(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.millisecond = millisecond;
	}
	
	public LiteralJsCacheResult() {
		
	}
	

	@Override
	public int year() {
		return this.year;
	}

	@Override
	public int month() {
		return this.month;
	}

	@Override
	public int day() {
		return this.day;
	}

	@Override
	public int hour() {
		return this.hour;
	}

	@Override
	public int minute() {
		return this.minute;
	}

	@Override
	public int second() {
		return this.second;
	}

	@Override
	public int millisecond() {
		return this.millisecond;
	}

	@Override
	public String[] keys() {
		return null;
	}
}
