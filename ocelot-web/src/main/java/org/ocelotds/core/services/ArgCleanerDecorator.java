/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import org.ocelotds.core.Cleaner;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 * Decorator that clean json argument with correct implmentation
 * @author hhfrancois
 */
@Decorator
@Priority(0)
public abstract class ArgCleanerDecorator implements IArgumentConvertor {

	@Inject
	@Delegate
	@Any
	IArgumentConvertor argumentConvertor;

	@Inject
	private Cleaner cleaner;

	@Override
	public Object convertJsonToJava(String jsonArg, Type paramType, Annotation[] parameterAnnotations) throws JsonUnmarshallingException {
		return argumentConvertor.convertJsonToJava(cleaner.cleanArg(jsonArg), paramType, parameterAnnotations);
	}
	
}
