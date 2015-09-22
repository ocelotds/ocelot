/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import org.ocelotds.annotations.JsCacheResult;
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
import javax.tools.Diagnostic;
import org.ocelotds.KeyMaker;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public class DataServiceVisitorJsBuilder extends AbstractDataServiceVisitor {

	protected final Messager messager;

	protected final KeyMaker keyMaker;

	/**
	 * 
	 * @param environment 
	 */
	public DataServiceVisitorJsBuilder(ProcessingEnvironment environment) {
		super(environment);
		this.messager = environment.getMessager();
		this.keyMaker = new KeyMaker();
	}

	@Override
	public String visitType(TypeElement typeElement, Writer writer) {
		try {
			createClassComment(typeElement, writer);
			String jsclsname = getJsClassname(typeElement);
			writer.append("function ").append(jsclsname).append("() {\n");
			String classname = typeElement.getQualifiedName().toString();
			writer.append("\tthis.ds = \"").append(classname).append("\";\n");
			writer.append("}\n");
			writer.append(jsclsname).append(".prototype = {\n");
			List<ExecutableElement> methodElements = ElementFilter.methodsIn(typeElement.getEnclosedElements());
			Iterator<ExecutableElement> iterator = methodElements.iterator();
			Collection<String> methodProceeds = new ArrayList<>();
			boolean first = true;
			while (iterator.hasNext()) {
				ExecutableElement methodElement = iterator.next();
				if (isConsiderateMethod(methodProceeds, methodElement)) {
					visitMethodElement(first, methodProceeds, classname, jsclsname, methodElement, writer);
					first = false;
				}
			}
			writer.append("\n};\n");
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
		if (!first) { // previous method exist
			writer.append(",\n");
		}
		String methodName = methodElement.getSimpleName().toString();
		List<String> argumentsType = getArgumentsType(methodElement);
		List<String> arguments = getArguments(methodElement);
		TypeMirror returnType = methodElement.getReturnType();
		createMethodComment(methodElement, arguments, argumentsType, returnType, writer);

		writer.append("\t").append(methodName).append(" : function (");
		if (arguments.size() != argumentsType.size()) {
			messager.printMessage(Diagnostic.Kind.ERROR, (new StringBuilder())
					  .append("Cannot Create service : ").append(jsclsname).append(" cause method ")
					  .append(methodName).append(" arguments inconsistent - argNames : ")
					  .append(arguments.size()).append(" / args : ").append(argumentsType.size()).toString());
			return;
		}
		int i = 0;
		while (i < argumentsType.size()) {
			writer.append((String) arguments.get(i));
			if ((++i) < arguments.size()) {
				writer.append(", ");
			}
		}
		writer.append(") {\n");

		createMethodBody(classname, methodElement, arguments.iterator(), writer);

		writer.append("\t}");
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
					writer.append("/**\n *").append(computeComment(comment, " ")).append("/\n");
				}
			}
		} else {
			writer.append("/**\n *").append(computeComment(comment, " ")).append("/\n");
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
		String methodComment = environment.getElementUtils().getDocComment(methodElement);
		writer.append("\t/**\n");
		// The javadoc comment
		if (methodComment != null) {
			methodComment = methodComment.split("@")[0];
			int lastIndexOf = methodComment.lastIndexOf('\n');
			if (lastIndexOf >= 0) {
				methodComment = methodComment.substring(0, lastIndexOf); // include the \n
			}
			writer.append("\t *").append(computeComment(methodComment, "\t ")).append("\n");
		}
		// La liste des arguments de la javadoc
		Iterator<String> typeIterator = argumentsType.iterator();
		for (String argumentName : argumentsName) {
			String type = typeIterator.next();
			writer.append("\t * @param {").append(type).append("} ").append(argumentName).append("\n");
		}
		// Si la methode retourne ou non quelque chose
		if (!returnType.toString().equals("void")) {
			writer.append("\t * @return {").append(returnType.toString()).append("}\n");
		}
		writer.append("\t */\n");
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
	void createMethodBody(String classname, ExecutableElement methodElement, Iterator<String> arguments, Writer writer) throws IOException {
		String methodName = methodElement.getSimpleName().toString();
		writer.append("\t\tvar op = \"").append(methodName).append("\";\n");
		StringBuilder args = new StringBuilder("");
		StringBuilder paramNames = new StringBuilder("");
		StringBuilder keys = new StringBuilder("");
		if (arguments != null && arguments.hasNext()) {
			JsCacheResult jcr = methodElement.getAnnotation(JsCacheResult.class);
			boolean allArgs = true;
			// if there is a jcr annotation with value diferrent of *, so we dont use all arguments
			if (null != jcr && null != jcr.keys() && (jcr.keys().length == 0 || (jcr.keys().length > 0 && !"*".equals(jcr.keys()[0])))) {
				allArgs = false;
				for (int i = 0; i < jcr.keys().length; i++) {
					String arg = jcr.keys()[i];
					keys.append(getKeyFromArg(arg));
					if (i < jcr.keys().length - 1) {
						keys.append(",");
					}
				}
			}
			while (arguments.hasNext()) {
				String arg = arguments.next();
				if (allArgs) {
					keys.append(arg);
				}
				args.append(arg);
				paramNames.append("\"").append(arg).append("\"");
				if (arguments.hasNext()) {
					args.append(",");
					paramNames.append(",");
					if (allArgs) {
						keys.append(",");
					}
				}
			}
		}
		String md5 = "\"" + keyMaker.getMd5(classname + "." + methodName);
		writer.append("\t\tvar id = " + md5 + "_\" + JSON.stringify([").append(keys.toString()).append("]).md5();\n");
		writer.append("\t\treturn OcelotPromiseFactory.createPromise(this.ds, id, op, [").append(paramNames.toString()).append("], [").append(args.toString()).append("]").append(");\n");
	}
	
	/**
	 * Transform arg to valid key. protect js NPE
	 * considers if arg or subfield is null
	 * example : if arg == c return c
	 * if arg == c.user return (c)?c.user:null
	 * if arg == c.user.u_id return (c&&c.user)?c.user.u_id:null
	 * @param arg
	 * @return 
	 */
	String getKeyFromArg(String arg) {
		String[] objs = arg.split("\\.");
		StringBuilder result = new StringBuilder();
		if(objs.length>1) {
			StringBuilder obj = new StringBuilder(objs[0]);
			result.append("(").append(obj);
			for (int i = 1; i < objs.length-1; i++) {
				result.append("&&");
				obj.append(".").append(objs[i]);
				result.append(obj);
			}
			result.append(")?").append(arg).append(":null");
		} else {
			result.append(arg);
		}
		return result.toString();
	}
	
}
