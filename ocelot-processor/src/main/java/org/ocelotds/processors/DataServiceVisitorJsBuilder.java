/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import org.ocelotds.annotations.JsCacheResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.ocelotds.KeyMaker;
import org.ocelotds.processors.stringDecorators.KeyForArgDecorator;
import org.ocelotds.processors.stringDecorators.NothingDecorator;
import org.ocelotds.processors.stringDecorators.QuoteDecorator;
import org.ocelotds.processors.stringDecorators.StringDecorator;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public class DataServiceVisitorJsBuilder extends AbstractDataServiceVisitor {

	protected final KeyMaker keyMaker;

	/**
	 *
	 * @param environment
	 */
	public DataServiceVisitorJsBuilder(ProcessingEnvironment environment) {
		super(environment);
		this.keyMaker = new KeyMaker();
	}

	@Override
	void _visitType(TypeElement typeElement, Writer writer) throws IOException {
		createClassComment(typeElement, writer);
		String jsclsname = getJsClassname(typeElement);
		String instanceName = getJsInstancename(jsclsname);
		writer.append("var ").append(instanceName).append(" = (").append(FUNCTION).append(" () {").append(CR);
		writer.append(TAB).append("'use strict';").append(CR);
		String classname = typeElement.getQualifiedName().toString();
		writer.append(TAB).append("var _ds = ").append(QUOTE).append(classname).append(QUOTE).append(";").append(CR);
		writer.append(TAB).append("return {").append(CR);
		browseAndWriteMethods(ElementFilter.methodsIn(typeElement.getEnclosedElements()), classname, writer);
		writer.append(CR).append(TAB).append("};");
		writer.append(CR).append("})();").append(CR);
		writer.append("console.info(").append(QUOTE).append("Ocelotds create Service instance : ").append(instanceName).append(QUOTE).append(");").append(CR);
	}

	/**
	 *
	 * @param first
	 * @param classname
	 * @param methodElement
	 * @param writer
	 * @throws IOException
	 */
	@Override
	void visitMethodElement(int first, String classname, ExecutableElement methodElement, Writer writer) throws IOException {
		if (first != 0) { // previous method exist
			writer.append(",").append(CR);
		}
		String methodName = methodElement.getSimpleName().toString();
		List<String> argumentsType = getArgumentsType(methodElement);
		List<String> arguments = getArguments(methodElement);
		TypeMirror returnType = methodElement.getReturnType();
		createMethodComment(methodElement, arguments, argumentsType, returnType, writer);

		writer.append(TAB2).append(methodName).append(" : ").append(FUNCTION).append(" (");
		int i = 0;
		while (i < argumentsType.size()) {
			writer.append((String) arguments.get(i));
			if ((++i) < arguments.size()) {
				writer.append(", ");
			}
		}
		writer.append(") {").append(CR);

		createMethodBody(classname, methodElement, arguments, writer);

		writer.append(TAB2).append("}");
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
		if (comment == null) {
			List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
			for (TypeMirror typeMirror : interfaces) {
				TypeElement element = (TypeElement) getTypeUtils().asElement(typeMirror);
				comment = getElementUtils().getDocComment(element);
				if (comment != null) {
					writer.append("/**").append(CR).append(" *").append(computeComment(comment, " ")).append("/").append(CR);
				}
			}
		} else {
			writer.append("/**").append(CR).append(" *").append(computeComment(comment, " ")).append("/").append(CR);
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
	void createMethodComment(ExecutableElement methodElement, List<String> argumentsName, List<String> argumentsType, TypeMirror returnType, Writer writer) throws IOException {
		String methodComment = getElementUtils().getDocComment(methodElement);
		writer.append(TAB2).append("/**").append(CR);
		// The javadoc comment
		if (methodComment != null) {
			methodComment = methodComment.split("@")[0];
			int lastIndexOf = methodComment.lastIndexOf(CR);
			if (lastIndexOf >= 0) {
				methodComment = methodComment.substring(0, lastIndexOf); // include the \n
			}
			writer.append(TAB2).append(" *").append(computeComment(methodComment, TAB2 + " ")).append(CR);
		}
		// La liste des arguments de la javadoc
		Iterator<String> typeIterator = argumentsType.iterator();
		for (String argumentName : argumentsName) {
			String type = typeIterator.next();
			writer.append(TAB2).append(" * @param {").append(type).append("} ").append(argumentName).append(CR);
		}
		// Si la methode retourne ou non quelque chose
		if (!returnType.toString().equals("void")) {
			writer.append(TAB2).append(" * @return {").append(returnType.toString()).append("}").append(CR);
		}
		writer.append(TAB2).append(" */").append(CR);
	}

	/**
	 * Create javascript method body
	 *
	 * @param classname
	 * @param methodElement
	 * @param arguments
	 * @param writer
	 * @throws IOException
	 */
	void createMethodBody(String classname, ExecutableElement methodElement, List<String> arguments, Writer writer) throws IOException {
		String methodName = getMethodName(methodElement);
		String args = stringJoinAndDecorate(arguments, ",", new NothingDecorator());
		String paramNames = stringJoinAndDecorate(arguments, ",", new QuoteDecorator());
		String keys = computeKeys(methodElement, arguments);
		createReturnOcelotPromiseFactory(classname, methodName, paramNames, args, keys, writer);
	}
	
	/**
	 * Return method name from Excecutable element
	 * @param methodElement
	 * @return 
	 */
	String getMethodName(ExecutableElement methodElement) {
		return methodElement.getSimpleName().toString();
	}
	
	/**
	 * Generate key part for variable part of md5
	 * @param methodElement
	 * @param arguments
	 * @return 
	 */
	String computeKeys(ExecutableElement methodElement, List<String> arguments) {
		String keys = stringJoinAndDecorate(arguments, ",", new NothingDecorator());
		if (arguments != null && !arguments.isEmpty()) {
			JsCacheResult jcr = methodElement.getAnnotation(JsCacheResult.class);
			// if there is a jcr annotation with value diferrent of *, so we dont use all arguments
			if (!considerateAllArgs(jcr)) {
				keys = stringJoinAndDecorate(Arrays.asList(jcr.keys()), ",", new KeyForArgDecorator());
			}
		}
		return keys;
	}
	
	/**
	 * Return body js line that return the OcelotPromise
	 * @param classname
	 * @param methodName
	 * @param paramNames
	 * @param args
	 * @param keys
	 * @param writer
	 * @throws IOException 
	 */
	void createReturnOcelotPromiseFactory(String classname, String methodName, String paramNames, String args, String keys, Writer writer) throws IOException {
		String md5 = keyMaker.getMd5(classname + "." + methodName);
		writer.append(TAB3).append("return OcelotPromiseFactory.createPromise(_ds, ").append(QUOTE).append(md5).append("_").append(QUOTE).append(" + JSON.stringify([").append(keys).append("]).md5()").append(", ").append(QUOTE).append(methodName).append(QUOTE).append(", [").append(paramNames).append("], [").append(args).append("]").append(");").append(CR);
	}

	/**
	 * Join list and separate by sep, each elements is decorate by 'decorator'
	 *
	 * @param list
	 * @param decoration
	 * @return
	 */
	String stringJoinAndDecorate(final List<String> list, final String sep, StringDecorator decorator) {
		if (decorator == null) {
			decorator = new NothingDecorator();
		}
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			boolean first = true;
			for (String argument : list) {
				if (!first) {
					sb.append(sep);
				}
				sb.append(decorator.decorate(argument));
				first = false;
			}
		}
		return sb.toString();
	}

	/**
	 * Check if we have to considerate all arguments jcr(keys={"*"})
	 *
	 * @param jcr
	 * @return
	 */
	boolean considerateAllArgs(JsCacheResult jcr) {
//		if (null == jcr) {
//			return true;
//		}
//		if (jcr.keys().length == 0) {
//			return false;
//		}
//		return "*".equals(jcr.keys()[0]);
		return null == jcr || (jcr.keys().length != 0 && "*".equals(jcr.keys()[0]));
	}
}
