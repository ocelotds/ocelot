/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import org.ocelotds.annotations.TransientDataService;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import org.ocelotds.annotations.DataService;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public abstract class AbstractDataServiceVisitor implements ElementVisitor<String, Writer> {

	protected final ProcessingEnvironment environment;
	/**
	 * Tools for log processor
	 */

	/**
	 * Tools for log processor
	 * @param environment
	 */
	public AbstractDataServiceVisitor(ProcessingEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Compute the name of javascript class
	 *
	 * @param typeElement
	 * @return
	 */
	String getJsClassname(TypeElement typeElement) {
		DataService dsAnno = typeElement.getAnnotation(DataService.class);
		String name = dsAnno.name();
		if (name.isEmpty()) {
			name = typeElement.getSimpleName().toString();
		}
		return name;
	}

	/**
	 * Transform raw multilines comment in prefixed comment
	 *
	 * @param comment
	 * @return
	 */
	String computeComment(String comment, String space) {
		String replace = "\n"+space+"*";
		return comment.replaceAll("\n", replace);
	}

	/**
	 * Return if the method have to considerate<br>
	 * The method is public,<br>
	 * Not annotated by TransientDataService<br>
	 * Not static and not from Object herited.
	 *
	 * @param methodProceeds
	 * @param methodElement
	 * @return
	 */
	boolean isConsiderateMethod(Collection<String> methodProceeds, ExecutableElement methodElement) {
		int argNum = methodElement.getParameters().size();
		String signature = methodElement.getSimpleName().toString() + "(" + argNum + ")";
		// Check if method ith same signature has been already proceed.
		if (methodProceeds.contains(signature)) {
			return false;
		}
		methodProceeds.add(signature);
		// Herited from Object
		TypeElement objectElement = environment.getElementUtils().getTypeElement(Object.class.getName());
		if (objectElement.getEnclosedElements().contains(methodElement)) {
			return false;
		}
		// Static, not public ?
		if (!methodElement.getModifiers().contains(Modifier.PUBLIC) || methodElement.getModifiers().contains(Modifier.STATIC)) {
			return false;
		}
		// TransientDataService ?
		List<? extends AnnotationMirror> annotationMirrors = methodElement.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annotationMirrors) {
			if (annotationMirror.getAnnotationType().toString().equals(TransientDataService.class.getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get argument types list from method
	 *
	 * @param methodElement
	 * @return
	 */
	List<String> getArgumentsType(ExecutableElement methodElement) {
		ExecutableType methodType = (ExecutableType) methodElement.asType();
		List<String> argumentsType = new ArrayList<>();
		for (TypeMirror argumentType : methodType.getParameterTypes()) {
			argumentsType.add(argumentType.toString());
		}
		return argumentsType;
	}

	/**
	 * Get argument names list from method
	 *
	 * @param methodElement
	 * @return
	 */
	List<String> getArguments(ExecutableElement methodElement) {
		List<String> arguments = new ArrayList<>();
		for (VariableElement variableElement : methodElement.getParameters()) {
			arguments.add(variableElement.toString());
		}
		return arguments;
	}

	@Override
	public String visit(Element e, Writer p) {
		return null;
	}

	@Override
	public String visit(Element e) {
		return null;
	}

	@Override
	public String visitPackage(PackageElement e, Writer p) {
		return null;
	}

	@Override
	public String visitVariable(VariableElement e, Writer p) {
		return null;
	}

	@Override
	public String visitExecutable(ExecutableElement e, Writer p) {
		return null;
	}

	@Override
	public String visitTypeParameter(TypeParameterElement e, Writer p) {
		return null;
	}

	@Override
	public String visitUnknown(Element e, Writer p) {
		return null;
	}
}
