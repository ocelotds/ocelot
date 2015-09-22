/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.Messager;
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
public class DataServiceVisitorHtmlBuilder extends AbstractDataServiceVisitor {

	protected final Messager messager;

	/**
	 *
	 * @param environment
	 */
	public DataServiceVisitorHtmlBuilder(ProcessingEnvironment environment) {
		super(environment);
		this.messager = environment.getMessager();
	}

	@Override
	public String visitType(TypeElement typeElement, Writer writer) {
		try {
			String jsclsname = getJsClassname(typeElement);
			String classname = typeElement.getQualifiedName().toString();
			List<ExecutableElement> methodElements = ElementFilter.methodsIn(typeElement.getEnclosedElements());
			Iterator<ExecutableElement> iterator = methodElements.iterator();
			Collection<String> methodProceeds = new ArrayList<>();

			writer.append("<div>").append("<h3>");
			createClassComment(typeElement, writer);
			writer.append("<br/>\n").append(classname).append(" {</h3>").append("\n");
			boolean first = true;
			while (iterator.hasNext()) {
				ExecutableElement methodElement = iterator.next();
				if (isConsiderateMethod(methodProceeds, methodElement)) {
					visitMethodElement(first, methodProceeds, classname, jsclsname, methodElement, writer);
					first = false;
				}
			}
			writer.append("<h3>").append("}</h3></div>\n");
		} catch (IOException ex) {
		}
		return null;
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
	void visitMethodElement(boolean first, Collection<String> methodProceeds, String classname, String jsclsname, ExecutableElement methodElement, Writer writer) throws IOException {
		String methodName = methodElement.getSimpleName().toString();
		List<String> argumentsType = getArgumentsType(methodElement);
		List<String> arguments = getArguments(methodElement);
		TypeMirror returnType = methodElement.getReturnType();
		writer.append("<code>");
		createMethodComment(methodElement, arguments, argumentsType, returnType, writer);
		writer.append("</code>").append("<span><code>&nbsp;&nbsp;&nbsp;").append(escapeLtGt(returnType.toString())).append("&nbsp;").append(methodName).append("(</code>");
		int i = 0;
		while (i < argumentsType.size()) {
			writer.append("<input type=\"text\" placeholder=\"").append((String) arguments.get(i)).append("\"/>");
			if ((++i) < arguments.size()) {
				writer.append("<code>,&nbsp;</code>");
			}
		}
		writer.append("<code>)&nbsp;</code>\n").append("<button classname=\"").append(jsclsname).append("\" methodname=\"").append(methodElement.getSimpleName().toString()).append("\" onclick=\"processCall(event)\">");
		writer.append("Submit").append("</button>\n");
		writer.append("</span>").append("<br/>\n");
	}

	/**
	 * Create comment of the class
	 *
	 * @param typeElement
	 * @param writer
	 * @throws IOException 
	 */
	void createClassComment(TypeElement typeElement, Writer writer) throws IOException {
		String comment = environment.getElementUtils().getDocComment(typeElement);
		if (comment == null) {
			List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
			for (TypeMirror typeMirror : interfaces) {
				TypeElement element = (TypeElement) environment.getTypeUtils().asElement(typeMirror);
				comment = environment.getElementUtils().getDocComment(element);
				if (comment != null) {
					writer.append("/**\n<br/>&nbsp;*").append(computeComment(comment, "<br/>&nbsp;")).append("/\n");
				}
			}
		} else {
			writer.append("/**\n<br/>&nbsp;*").append(computeComment(comment, "<br/>&nbsp;")).append("/\n");
		}
	}

	/**
	 * Escape < and > in comment
	 *
	 * @param comment
	 * @return
	 */
	String escapeLtGt(String comment) {
		return comment.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
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
		String methodComment = environment.getElementUtils().getDocComment(methodElement);
		writer.append("&nbsp;&nbsp;&nbsp;/**\n<br/>");
		// The javadoc comment
		if (methodComment != null) {
			methodComment = methodComment.split("@")[0];
			int lastIndexOf = methodComment.lastIndexOf('\n');
			if (lastIndexOf >= 0) {
				methodComment = methodComment.substring(0, lastIndexOf); // include the \n
			}
			writer.append("&nbsp;&nbsp;&nbsp;&nbsp;*").append(computeComment(methodComment, "<br/>&nbsp;&nbsp;&nbsp;&nbsp;")).append("\n<br/>");
		}
		// La liste des arguments de la javadoc
		Iterator<String> typeIterator = argumentsType.iterator();
		for (String argumentName : argumentsName) {
			String type = typeIterator.next();
			writer.append("&nbsp;&nbsp;&nbsp;&nbsp;* @param {").append(escapeLtGt(type)).append("} ").append(argumentName).append("\n<br/>");
		}
		// Si la methode retourne ou non quelque chose
		if (!returnType.toString().equals("void")) {
			writer.append("&nbsp;&nbsp;&nbsp;&nbsp;* @return {").append(escapeLtGt(returnType.toString())).append("}\n<br/>");
		}
		writer.append("&nbsp;&nbsp;&nbsp;&nbsp;*/\n<br/>");
	}
}
