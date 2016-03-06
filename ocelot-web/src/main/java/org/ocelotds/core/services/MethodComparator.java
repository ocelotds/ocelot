/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.core.services;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * sort method by less argument
 * @author hhfrancois
 */
public class MethodComparator implements Comparator<Method> {

	@Override
	public int compare(Method o1, Method o2) {
		int res = o1.getParameterTypes().length - o2.getParameterTypes().length;
		if (res == 0) {
			return -1;
		}
		return res;
	}

}
