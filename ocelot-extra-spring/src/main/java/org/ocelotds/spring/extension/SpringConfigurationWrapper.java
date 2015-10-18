/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.spring.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import org.ocelotds.spring.OcelotSpringConfiguration;

/**
 *
 * @author hhfrancois
 */
public class SpringConfigurationWrapper<T> implements AnnotatedType {

		private final static  Annotation OSCLITERAL = new AnnotationLiteral<OcelotSpringConfiguration>() {};

		final AnnotatedType<T> type;

		SpringConfigurationWrapper(AnnotatedType<T> type) {
			this.type = type;
		}


		@Override
		public Class<T> getJavaClass() {
			return type.getJavaClass();
		}

		@Override
		public Set<AnnotatedConstructor<T>> getConstructors() {
			return type.getConstructors();
		}

		@Override
		public Set<AnnotatedMethod<? super T>> getMethods() {
			return type.getMethods();
		}

		@Override
		public Set<AnnotatedField<? super T>> getFields() {
			return type.getFields();
		}

		@Override
		public Type getBaseType() {
			return type.getBaseType();
		}

		@Override
		public Set<Type> getTypeClosure() {
			return type.getTypeClosure();
		}

		@Override
		public <X extends Annotation> X getAnnotation(final Class<X> annType) {
			return (X) (annType.equals(Alternative.class) ? OSCLITERAL : type.getAnnotation(annType));
		}

		@Override
		public Set<Annotation> getAnnotations() {
			Set<Annotation> annotations = new HashSet<>(type.getAnnotations());
			annotations.add(OSCLITERAL);
			return annotations;
		}

		@Override
		public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
			return annotationType.equals(OcelotSpringConfiguration.class) ? true : type.isAnnotationPresent(annotationType);
		}
	}