/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import org.ocelotds.annotations.JsCacheResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.ocelotds.KeyMaker;
import org.ocelotds.annotations.WsDataService;
import org.ocelotds.processors.stringDecorators.KeyForArgDecorator;
import org.ocelotds.processors.stringDecorators.NothingDecorator;
import org.ocelotds.processors.stringDecorators.QuoteDecorator;
import org.ocelotds.processors.stringDecorators.StringDecorator;
import org.ocelotds.frameworks.FwkWriter;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public class DataServiceVisitorJsBuilder extends AbstractDataServiceVisitor {

	protected final KeyMaker keyMaker;
	protected final FwkWriter fwk;

	/**
	 *
	 * @param environment
	 * @param fwk
	 */
	public DataServiceVisitorJsBuilder(ProcessingEnvironment environment, FwkWriter fwk) {
		super(environment);
		this.keyMaker = new KeyMaker();
		this.fwk = fwk;
	}

	@Override
	void _visitType(TypeElement typeElement, Writer writer) throws IOException {
		String jsclsname = getJsClassname(typeElement);
		String instanceName = getJsInstancename(jsclsname);
		fwk.writeHeaderService(writer, instanceName);
		String classname = typeElement.getQualifiedName().toString();
		writer.append(TAB).append("var _ds").append(SPACEOPTIONAL).append(EQUALS).append(SPACEOPTIONAL).append(QUOTE).append(classname).append(QUOTE).append(SEMICOLON).append(CR);
		writer.append(TAB).append("return").append(SPACEOPTIONAL).append(OPENBRACE).append(CR);
		browseAndWriteMethods(getOrderedMethods(typeElement, new MethodComparator()), classname, writer);
		writer.append(CR);
		writer.append(TAB).append(CLOSEBRACE).append(SEMICOLON).append(CR);
		fwk.writeFooterService(writer);
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
	void visitMethodElement(String classname, ExecutableElement methodElement, Writer writer) throws IOException {
		String methodName = methodElement.getSimpleName().toString();
		List<String> argumentsType = getArgumentsType(methodElement);
		List<String> arguments = getArguments(methodElement);
		TypeMirror returnType = methodElement.getReturnType();

		writer.append(TAB2).append(methodName).append(SPACEOPTIONAL).append(COLON).append(SPACEOPTIONAL)
				  .append(FUNCTION).append(SPACEOPTIONAL).append(OPENPARENTHESIS); //\t\tmethodName : function (
		int i = 0;
		while (i < argumentsType.size()) {
			writer.append((String) arguments.get(i)); // argname
			if ((++i) < arguments.size()) {
				writer.append(COMMA).append(SPACEOPTIONAL); //, 
			}
		}
		writer.append(CLOSEPARENTHESIS).append(SPACEOPTIONAL).append(OPENBRACE).append(CR); //) {\n

		createMethodBody(classname, methodElement, arguments, writer);

		writer.append(TAB2).append(CLOSEBRACE); //\t\t}
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
		boolean ws = isWebsocketDataService(methodElement);
		String args = stringJoinAndDecorate(arguments, COMMA, new NothingDecorator());
		String paramNames = stringJoinAndDecorate(arguments, COMMA, new QuoteDecorator());
//		String keys = computeKeys(methodElement, arguments);
		createReturnOcelotPromiseFactory(classname, methodName, ws, paramNames, args, writer);
	}
	
	/**
	 * Return if method have to called by websocket
	 * @param methodElement
	 * @return 
	 */
	boolean isWebsocketDataService(ExecutableElement methodElement) {
		return methodElement.getAnnotation(WsDataService.class) != null;
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
		String keys = stringJoinAndDecorate(arguments, COMMA, new NothingDecorator());
		if (arguments != null && !arguments.isEmpty()) {
			JsCacheResult jcr = methodElement.getAnnotation(JsCacheResult.class);
			// if there is a jcr annotation with value diferrent of *, so we dont use all arguments
			if (!considerateAllArgs(jcr)) {
				keys = stringJoinAndDecorate(Arrays.asList(jcr.keys()), COMMA, new KeyForArgDecorator());
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
	 * @param writer
	 * @throws IOException 
	 */
	void createReturnOcelotPromiseFactory(String classname, String methodName, boolean ws, String paramNames, String args, Writer writer) throws IOException {
		String md5 = keyMaker.getMd5(classname + DOT + methodName);
		writer.append(TAB3).append("return promiseFactory.create").append(OPENPARENTHESIS).append("_ds").append(COMMA).append(SPACEOPTIONAL)
				  .append(QUOTE).append(md5).append(QUOTE).append(COMMA).append(SPACEOPTIONAL)
				  .append(QUOTE).append(methodName).append(QUOTE).append(COMMA).append(SPACEOPTIONAL).append(""+ws).append(COMMA)
				  .append(SPACEOPTIONAL).append(OPENBRACKET).append(paramNames).append(CLOSEBRACKET).append(COMMA)
				  .append(SPACEOPTIONAL).append(OPENBRACKET).append(args).append(CLOSEBRACKET).append(CLOSEPARENTHESIS)
				  .append(SEMICOLON).append(CR);
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
		return null == jcr || (jcr.keys().length != 0 && ASTERISK.equals(jcr.keys()[0]));
	}
}
