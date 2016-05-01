/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
	public void testCompare() {
		System.out.println("compare");
		String name1 = "a";
		String name2 = "b";
		ExecutableElement o1 = mock(ExecutableElement.class);
		ExecutableElement o2 = mock(ExecutableElement.class);
		Name n1 = mock(Name.class);
		Name n2 = mock(Name.class);
		when(o1.getSimpleName()).thenReturn(n1);
		when(o2.getSimpleName()).thenReturn(n2);
		when(n1.toString()).thenReturn(name1);
		when(n2.toString()).thenReturn(name2);
		doReturn(5).when(instance).compareByArgument(any(ExecutableElement.class), any(ExecutableElement.class));
		int result = instance.compare(o1, o2);
		assertThat(result).isEqualTo(-1);
		result = instance.compare(o2, o1);
		assertThat(result).isEqualTo(1);
		result = instance.compare(o1, o1);
		assertThat(result).isEqualTo(5);
	}
	/**
	 * Test of compareByArgument method, of class.
	 */
	@Test
	public void compareByArgumentTest() {
		System.out.println("compareByArgument");
		int size1 = 2;
		int size2 = 3;
		ExecutableElement o1 = mock(ExecutableElement.class);
		ExecutableElement o2 = mock(ExecutableElement.class);
		List l1 = mock(List.class);
		List l2 = mock(List.class);
		when(o1.getParameters()).thenReturn(l1);
		when(o2.getParameters()).thenReturn(l2);
		when(l1.size()).thenReturn(size1);
		when(l2.size()).thenReturn(size2);
		int result = instance.compareByArgument(o1, o2);
		assertThat(result).isPositive();
		result = instance.compareByArgument(o2, o1);
		assertThat(result).isNegative();
		result = instance.compareByArgument(o1, o1);
		assertThat(result).isZero();
	}

}