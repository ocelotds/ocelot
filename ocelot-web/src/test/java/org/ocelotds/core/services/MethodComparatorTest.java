/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.core.services;

import java.lang.reflect.Method;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodComparatorTest {

	@InjectMocks
	@Spy
	MethodComparator instance;

	/**
	 * Test of compare method, of class MethodComparator.
	 */
	@Test
	public void testCompareM1afterM2() {
		System.out.println("compare");
		Method m1 = getMethodByName("methodA");
		Method m2 = getMethodByName("methodC");
		int result = instance.compare(m1, m2);
		assertThat(result).isEqualTo(1);
	}

	/**
	 * Test of compare method, of class MethodComparator.
	 */
	@Test
	public void testCompareM1equalM2() {
		System.out.println("compare");
		Method m1 = getMethodByName("methodA");
		Method m2 = getMethodByName("methodB");
		int result = instance.compare(m1, m2);
		assertThat(result).isEqualTo(-1);
	}
	
	/**
	 * Test of compare method, of class MethodComparator.
	 */
	@Test
	public void testCompareM1beforeM2() {
		System.out.println("compare");
		Method m1 = getMethodByName("methodC");
		Method m2 = getMethodByName("methodB");
		int result = instance.compare(m1, m2);
		assertThat(result).isEqualTo(-1);
	}

	Method getMethodByName(String name) {
		Method[] methods = this.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if(method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}
	
	void methodA(String s1, String s2) {
		
	}
	void methodB(String s1, String s2) {
		
	}
	void methodC(String s1) {
		
	}

}