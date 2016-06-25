/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.core.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.messaging.ConstraintViolation;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class ConstraintServices {
	
	@Inject
	@OcelotLogger
	Logger logger;
	
	/**
	 *
	 * @param cve
	 * @return
	 */
	public ConstraintViolation[] extractViolations(ConstraintViolationException cve) {
		Set<javax.validation.ConstraintViolation<?>> constraintViolations = cve.getConstraintViolations();
		List<ConstraintViolation> resultList = new ArrayList<>();
		for (javax.validation.ConstraintViolation<?> constraintViolation : constraintViolations) {
			resultList.add(extractViolation(constraintViolation));
		}
		return resultList.toArray(new ConstraintViolation[]{});
	}
	
	ConstraintViolation extractViolation(javax.validation.ConstraintViolation<?> constraintViolation) {
		ConstraintViolation cv = new ConstraintViolation(constraintViolation.getMessage());
		extractViolationInfoFromNodes(constraintViolation.getPropertyPath().iterator(), cv);
		cv.setName(getArgumentName(cv));
		return cv;
	}

	String getArgumentName(ConstraintViolation cv) {
		return "arg"+cv.getIndex();
	}
	
	void extractViolationInfoFromNodes(Iterator<Path.Node> nodeIterator, ConstraintViolation cv) {
		while (nodeIterator.hasNext()) {
			extractViolationInfoFromNode(nodeIterator.next(), cv);
		}
	}

	void extractViolationInfoFromNode(Path.Node node, ConstraintViolation cv) {
		ElementKind kind = node.getKind();
		logger.debug("Kind:{} Index:{} Key:{} Name:{}", kind, node.getIndex(), node.getKey(), node.getName());
		
		if (ElementKind.PARAMETER.equals(kind)) {
			cv.setIndex(getIndexFromArgname(node.getName()));
		} else if (ElementKind.PROPERTY.equals(kind)) {
			cv.setProp(node.getName());
		}
	}

	/**
	 * 
	 * @param argname
	 * @return 
	 */
	int getIndexFromArgname(String argname) {
		String num = argname.substring(3);
		return Integer.parseInt(num);
	}
}
