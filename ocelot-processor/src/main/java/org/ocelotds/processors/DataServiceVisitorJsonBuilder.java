/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public class DataServiceVisitorJsonBuilder extends AbstractDataServiceVisitor {

	final private boolean first;

	/**
	 *
	 * @param environment
	 * @param first
	 */
	public DataServiceVisitorJsonBuilder(ProcessingEnvironment environment, boolean first) {
		super(environment);
		this.first = first;
	}
	
	boolean isFirst() {
		return first;
	}

	@Override
	public void _visitType(TypeElement typeElement, Writer writer) throws IOException {
		if (!isFirst()) {
			writer.append(",");
		}
		String jsclsname = getJsClassname(typeElement);
		String instancename = getJsInstancename(jsclsname);
		String classname = typeElement.getQualifiedName().toString();
		writer.append("{");
		writer.append(QUOTE).append("name").append(QUOTE).append(":").append(QUOTE).append(instancename).append(QUOTE).append(",");
		createClassComment(typeElement, writer);
		writer.append(QUOTE).append("methods").append(QUOTE).append(":[");
		browseAndWriteMethods(ElementFilter.methodsIn(typeElement.getEnclosedElements()), classname, writer);
		writer.append("]}");
	}

	/**
	 *
	 * @param first
	 * @param methodProceeds
	 * @param classname
	 * @param jsclsname
	 * @param methodElement
	 * @param writer
	 * @throws IOException
	 */
	@Override
	void visitMethodElement(int first, String classname, ExecutableElement methodElement, Writer writer) throws IOException {
		String methodName = methodElement.getSimpleName().toString();
		List<String> argumentsType = getArgumentsType(methodElement);
		List<String> arguments = getArguments(methodElement);
		TypeMirror returnType = methodElement.getReturnType();
		if (first != 0) { // previous method exist
			writer.append(",");
		}
		writer.append("{");
		writer.append(QUOTE).append("name").append(QUOTE).append(":").append(QUOTE).append(methodName).append(QUOTE).append(",");
		createMethodComment(methodElement, writer);
		writer.append(QUOTE).append("returntype").append(QUOTE).append(":").append(QUOTE).append(returnType.toString()).append(QUOTE).append(",");
		writer.append(QUOTE).append("argtypes").append(QUOTE).append(":[");
		Iterator ite = argumentsType.iterator();
		while (ite.hasNext()) {
			writer.append(QUOTE).append("" + ite.next()).append(QUOTE);
			if (ite.hasNext()) {
				writer.append(",");
			}
		}
		writer.append("],");
		writer.append(QUOTE).append("argnames").append(QUOTE).append(":[");
		ite = arguments.iterator();
		while (ite.hasNext()) {
			writer.append(QUOTE).append("" + ite.next()).append(QUOTE);
			if (ite.hasNext()) {
				writer.append(",");
			}
		}
		writer.append("]");
		writer.append("}");
	}

	/**
	 * Create comment of the class
	 *
	 * @param typeElement
	 * @param writer
	 * @throws IOException
	 */
	void createClassComment(TypeElement typeElement, Writer writer) throws IOException {
		String comment = getElementUtils().getDocComment(typeElement);
		if (comment != null) {
//			writer.append("\"comment\":\"").append(comment).append("\",");
		}
	}

	/**
	 * Create javascript comment from Method comment
	 *
	 * @param methodElement
	 * @param argumentsName
	 * @param argumentsType
	 * @param returnType
	 * @param writer
	 * @throws IOException
	 */
	void createMethodComment(ExecutableElement methodElement, Writer writer) throws IOException {
		String comment = getElementUtils().getDocComment(methodElement);
		if (comment != null) {
//			writer.append("\"comment\":\"").append(comment).append("\",");
		}
	}
}
