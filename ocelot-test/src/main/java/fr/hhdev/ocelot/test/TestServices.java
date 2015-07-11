/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.test;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheRemove;
import fr.hhdev.ocelot.annotations.JsCacheRemoveAll;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.messaging.MessageEvent;
import fr.hhdev.ocelot.messaging.MessageToClient;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class TestServices {
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
		return new Date();
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

	/**
	 * file in glassfish/domains/domain1/config/
	 *
	 * @param pathname
	 * @return
	 * @throws FileNotFoundException
	 */
	public InputStream getInputstreamInDomainConfig(String pathname) throws FileNotFoundException {
		return new FileInputStream(pathname);
	}

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

	@Inject
	@MessageEvent
	Event<MessageToClient> wsEvent;

	public void publish(String topic, int nb) {
		for (int i = 0; i < nb; i++) {
			MessageToClient messageToClient = new MessageToClient();
			messageToClient.setId(topic);
			messageToClient.setResult("Message From server "+1);
			wsEvent.fire(messageToClient);
		}
	}

	@JsCacheResult(minute = 1)
	public Collection<Integer> methodCached() {
		Collection<Integer> result = new ArrayList<>();
		for (int i = 0; i < new Double(Math.random() *100).intValue() ; i++) {
			result.add(i);
		}
		return result;
	}

	@JsCacheRemove(cls = TestServices.class, methodName = "methodCached")
	public void methodRemoveCache() {
	}

	@JsCacheRemoveAll
	public void methodRemoveAllCache() {
	}
}
