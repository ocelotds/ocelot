/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = "TEST")
public class ClassAsDataService {

	public void methodWithSomeArguments(String s, Integer i, String[] a, Collection<String> c, Map<String, Integer> m) {

	}

	public void methodWith2Arguments(Integer i, String s) {

	}

	public String methodReturnString(String a) {
		return "r1";
	}

	public String methodReturnString2(String a) {
		return "r3";
	}

	public String methodThrowException(String a) throws AbstractMethodError {
		throw new AbstractMethodError("MyMessage");
	}
	
	public String methodThrowViolationConstraint(String a) throws ConstraintViolationException {
		throw new ConstraintViolationException(null);
	}

	@JsCacheResult
	public String methodReturnCachedString(String a) {
		return "r5";
	}

	@JsonMarshaller(LocaleMarshaller.class)
	public Locale methodWithMarshaller(String a) {
		return Locale.FRANCE;
	}

	public void methodWithBadUnmarshaller(@JsonUnmarshaller(BadMarshaller.class) String a) {
	}

	public void methodWithUnmarshaller(@JsonUnmarshaller(LocaleMarshaller.class) Locale l) {
	}

	static class BadMarshaller implements org.ocelotds.marshalling.IJsonMarshaller<String> {

		public BadMarshaller(String t) {

		}

		@Override
		public String toJava(String json) throws JsonUnmarshallingException {
			return "";
		}

		@Override
		public String toJson(String obj) throws JsonMarshallingException {
			return null;
		}
	}
}
