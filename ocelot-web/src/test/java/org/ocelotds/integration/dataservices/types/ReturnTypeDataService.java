/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.integration.dataservices.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.objects.Result;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
@ApplicationScoped
public class ReturnTypeDataService {
	Date d;
	
	@PostConstruct
	private void init() {
		d = new Date();
	}

	public void getVoid() {
	}

	public String getString() {
		return "FOO";
	}

	public int getNum() {
		return 1;
	}

	public Integer getNumber() {
		return 2;
	}

	public boolean getBool() {
		return Boolean.TRUE;
	}

	public Boolean getBoolean() {
		return Boolean.FALSE;
	}

	public Date getDate() {
		return d;
	}

	public Result getResult() {
		return Result.getMock();
	}

	public Collection<Integer> getCollectionInteger() {
		Collection<Integer> result = new ArrayList<>();
		result.add(1);
		result.add(2);
		result.add(3);
		result.add(4);
		return result;
	}

	public Collection<Result> getCollectionResult() {
		Collection<Result> result = new ArrayList<>();
		result.add(Result.getMock());
		result.add(Result.getMock());
		result.add(Result.getMock());
		result.add(Result.getMock());
		return result;
	}

	public Collection<Collection<Result>> getCollectionOfCollectionResult() {
		Collection<Collection<Result>> result = new ArrayList<>();
		result.add(getCollectionResult());
		result.add(getCollectionResult());
		result.add(getCollectionResult());
		result.add(getCollectionResult());
		return result;
	}

	public Map<String, Result> getMapResult() {
		Map<String, Result> result = new HashMap<>();
		result.put("1", Result.getMock());
		result.put("2", Result.getMock());
		result.put("3", Result.getMock());
		result.put("4", Result.getMock());
		return result;
	}
}
