/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.messaging.ConstraintViolation;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ConstraintServicesTest {

	@InjectMocks
	@Spy
	ConstraintServices instance;
	
	@Mock
	Logger logger;

	/**
	 * Test of extractViolations method, of class ConstraintServices.
	 */
	@Test
	public void testExtractViolations() {
		System.out.println("extractViolations");
		List<String> paramNames = Arrays.asList("arg0", "arg1");
		ConstraintViolationException cve = mock(ConstraintViolationException.class);
		Set<javax.validation.ConstraintViolation<?>> constraintViolations = new HashSet<>();
		constraintViolations.add(mock(javax.validation.ConstraintViolation.class));
		constraintViolations.add(mock(javax.validation.ConstraintViolation.class));
		when(cve.getConstraintViolations()).thenReturn(constraintViolations);
		doReturn(mock(ConstraintViolation.class)).when(instance).extractViolation(any(javax.validation.ConstraintViolation.class), any(List.class));
		ConstraintViolation[] result = instance.extractViolations(cve, paramNames);
		assertThat(result).hasSize(2);
	}

	/**
	 * Test of extractViolations method, of class ConstraintServices.
	 */
	@Test
	public void testExtractViolation() {
		System.out.println("extractViolation");
		List<String> paramNames = Collections.EMPTY_LIST;
		doNothing().when(instance).extractViolationInfoFromNodes(any(Iterator.class), any(ConstraintViolation.class));
		doReturn("NAME").when(instance).getArgumentName(any(ConstraintViolation.class), any(List.class));
		javax.validation.ConstraintViolation cv = mock(javax.validation.ConstraintViolation.class);
		Path path = mock(Path.class);
		when(cv.getPropertyPath()).thenReturn(path);
		when(path.iterator()).thenReturn(mock(Iterator.class));
		ConstraintViolation result = instance.extractViolation(cv, paramNames);
		assertThat(result.getName()).isEqualTo("NAME");
	}

	/**
	 * Test of getArgumentName method, of class ConstraintServices.
	 */
	@Test
	public void testGetArgumentName() {
		System.out.println("getArgumentName");
		ConstraintViolation cv = new ConstraintViolation();
		cv.setIndex(1);
		String expResult = "arg1";
		List<String> paramNames = Arrays.asList("arg0", expResult, "arg2");
		String result = instance.getArgumentName(cv, paramNames);
		assertThat(result).isEqualTo(expResult);
	}

	/**
	 * Test of extractViolationInfoFromNodes method, of class ConstraintServices.
	 */
	@Test
	public void testExtractViolationInfoFromNodes() {
		System.out.println("extractViolationInfoFromNodes");
		List<Path.Node> list = Collections.EMPTY_LIST;
		Iterator<Path.Node> nodeIterator = list.iterator();
		ConstraintViolation cv = new ConstraintViolation();
		instance.extractViolationInfoFromNodes(nodeIterator, cv);
		verify(instance, never()).extractViolationInfoFromNode(any(Path.Node.class), any(ConstraintViolation.class));
	}

	/**
	 * Test of extractViolationInfoFromNodes method, of class ConstraintServices.
	 */
	@Test
	public void testExtractViolationInfoFromNodes2() {
		System.out.println("extractViolationInfoFromNodes");
		List<Path.Node> list = new ArrayList<>();
		list.add(mock(Path.Node.class));
		list.add(mock(Path.Node.class));
		Iterator<Path.Node> nodeIterator = list.iterator();
		ConstraintViolation cv = new ConstraintViolation();
		doNothing().when(instance).extractViolationInfoFromNode(any(Path.Node.class), any(ConstraintViolation.class));
		instance.extractViolationInfoFromNodes(nodeIterator, cv);
		verify(instance, times(2)).extractViolationInfoFromNode(any(Path.Node.class), any(ConstraintViolation.class));
	}

	/**
	 * Test of extractViolationInfoFromNode method, of class ConstraintServices.
	 */
	@Test
	public void testExtractViolationInfoFromNode() {
		System.out.println("extractViolationInfoFromNode");
		Path.Node node = mock(Path.Node.class);
		when(node.getKind()).thenReturn(ElementKind.METHOD).thenReturn(ElementKind.PARAMETER).thenReturn(ElementKind.PROPERTY);
		when(node.getName()).thenReturn("arg0").thenReturn("prop");
		doReturn(5).when(instance).getIndexFromArgname(anyString());

		ConstraintViolation cv = new ConstraintViolation();
		instance.extractViolationInfoFromNode(node, cv);
		assertThat(cv.getMessage()).isNull();
		assertThat(cv.getIndex()).isEqualTo(0);
		assertThat(cv.getProp()).isNull();
		cv = new ConstraintViolation();
		instance.extractViolationInfoFromNode(node, cv);
		assertThat(cv.getMessage()).isNull();
		assertThat(cv.getIndex()).isEqualTo(5);
		assertThat(cv.getProp()).isNull();
		cv = new ConstraintViolation();
		instance.extractViolationInfoFromNode(node, cv);
		assertThat(cv.getMessage()).isNull();
		assertThat(cv.getIndex()).isEqualTo(0);
		assertThat(cv.getProp()).isEqualTo("prop");
	}

	/**
	 * Test of getIndexFromArgname method, of class ConstraintServices.
	 */
	@Test
	public void testGetIndexFromArgname() {
		System.out.println("getIndexFromArgname");
		int result = instance.getIndexFromArgname("arg0");
		assertThat(result).isEqualTo(0);
		
		result = instance.getIndexFromArgname("arg10");
		assertThat(result).isEqualTo(10);
	}

}