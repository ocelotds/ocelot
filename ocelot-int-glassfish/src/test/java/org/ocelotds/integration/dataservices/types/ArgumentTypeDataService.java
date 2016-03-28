/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.types;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.integration.MethodException;
import org.ocelotds.integration.objects.Result;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class ArgumentTypeDataService {
	public String methodWithNum(int i) {
		return "methodWithNum_" + i;
	}

	public String methodWithNumber(Integer i) {
		return "methodWithNumber_" + i;
	}

	public String methodWithBool(boolean i) {
		return "methodWithBool_" + i;
	}

	public String methodWithBoolean(Boolean i) {
		return "methodWithBoolean_" + i;
	}

	public String methodWithDate(Date i) {
		return "methodWithDate_" + i.getTime();
	}

	public String methodWithResult(Result i) {
		return "methodWithResult_" + i.getInteger();
	}

	public String methodWithArrayInteger(Integer[] i) {
		return "methodWithArrayInteger_" + i.length;
	}

	public String methodWithCollectionInteger(Collection<Integer> i) {
		return "methodWithCollectionInteger_" + i.size();
	}

	public String methodWithArrayResult(Result[] i) {
		return "methodWithArrayResult_" + i.length;
	}

	public String methodWithCollectionResult(Collection<Result> i) {
		return "methodWithCollectionResult_" + i.size();
	}

	public String methodWithMapResult(Map<String, Result> i) {
		return "methodWithMapResult_" + i.size();
	}

	public String methodWithCollectionOfCollectionResult(Collection<Collection<Result>> i) {
		return "methodWithCollectionOfCollectionResult_" + i.size();
	}
	
	@JsCacheResult(minute = 5, keys = {"a", "c.integer"})
	public String methodWithManyParameters(String a, int b, Result c, Collection<String> d) {
		return "methodWithManyParameters a="+a+" - b="+b+" - c="+c.getInteger()+" - d:"+d.size();
	}

	public void methodThatThrowException() throws MethodException {
		throw new MethodException("message of exception");
	}

	public String methodWithAlmostSameSignature(Integer i) {
		return "Integer";
	}

	public String methodWithAlmostSameSignature(String i) {
		return "String";
	}

	public String methodWithAlmostSameSignature(String i, String i2) {
		return "String2";
	}
}
