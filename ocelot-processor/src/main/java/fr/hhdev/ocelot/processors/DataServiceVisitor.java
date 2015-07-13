/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.annotations.TransientDataService;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.Messager;
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
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Visitor of class annoted fr.hhdev.ocelot.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public class DataServiceVisitor implements ElementVisitor<String, Writer> {

	private final ProcessingEnvironment environment;
	/**
	 * Tools for log processor
	 */
	private final Messager messager;

	public DataServiceVisitor(ProcessingEnvironment environment) {
		this.environment = environment;
		this.messager = environment.getMessager();
	}

	@Override
	public String visitType(TypeElement typeElement, Writer writer) {
		try {
			createClassComment(typeElement, writer);
			writer.append("function ").append(typeElement.getSimpleName()).append("() {\n");
			String classname = typeElement.getQualifiedName().toString();
			writer.append("\tthis.ds = \"").append(classname).append("\";\n");
			writer.append("}\n");
			writer.append(typeElement.getSimpleName()).append(".prototype = {\n");
			List<ExecutableElement> methodElements = ElementFilter.methodsIn(typeElement.getEnclosedElements());
			Iterator<ExecutableElement> iterator = methodElements.iterator();
			Collection<String> methodProceeds = new ArrayList<>();
			while (iterator.hasNext()) {
				ExecutableElement methodElement = iterator.next();
				if (isConsiderateMethod(methodProceeds, methodElement)) {
					String methodName = methodElement.getSimpleName().toString();
					List<String> argumentsType = getArgumentsType(methodElement);
					List<String> arguments = getArguments(methodElement);
					TypeMirror returnType = methodElement.getReturnType();
					writer.append("\n");
					createMethodComment(methodElement, arguments, argumentsType, returnType, writer);

					writer.append("\t").append(methodName).append(" : function (");
					if (arguments.size() != argumentsType.size()) {
						messager.printMessage(Diagnostic.Kind.ERROR, (new StringBuilder()).append("Cannot Create service : ").append(typeElement.getSimpleName()).append(" cause method ").append(methodElement.getSimpleName()).append(" arguments inconsistent - argNames : ").append(arguments.size()).append(" / args : ").append(argumentsType.size()).toString(), typeElement);
						return null;
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
					if (iterator.hasNext()) {
						writer.append(",");
					}
					writer.append("\n");
				}
			}
			writer.append("};\n");
		} catch (IOException ex) {
		}
		return null;
	}

	/**
	 * Create comment of the class
	 *
	 * @param typeElement
	 * @param writer
	 */
	protected void createClassComment(TypeElement typeElement, Writer writer) {
		try {
			String comment = environment.getElementUtils().getDocComment(typeElement);
			if (comment == null) {
				List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
				for (TypeMirror typeMirror : interfaces) {
					TypeElement element = (TypeElement) environment.getTypeUtils().asElement(typeMirror);
					comment = environment.getElementUtils().getDocComment(element);
					if (comment != null) {
						writer.append("/**\n *").append(comment.replaceAll("\n", "\n *")).append("/\n");
					}
				}
			} else {
				writer.append("/**\n *").append(comment.replaceAll("\n", "\n *")).append("/\n");
			}
		} catch (IOException ioe) {
		}
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
	public boolean isConsiderateMethod(Collection<String> methodProceeds, ExecutableElement methodElement) {
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
	public List<String> getArgumentsType(ExecutableElement methodElement) {
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
	public List<String> getArguments(ExecutableElement methodElement) {
		List<String> arguments = new ArrayList<>();
		for (VariableElement variableElement : methodElement.getParameters()) {
			arguments.add(variableElement.toString());
		}
		return arguments;
	}

	/**
	 * Create javascript comment from Method comment
	 *
	 * @param methodElement
	 * @param argumentsName
	 * @param argumentsType
	 * @param returnType
	 * @param writer
	 */
	protected void createMethodComment(ExecutableElement methodElement, List<String> argumentsName, List<String> argumentsType, TypeMirror returnType, Writer writer) {
		try {
			String methodComment = environment.getElementUtils().getDocComment(methodElement);
			writer.write("\t/**\n");
			// The javadoc comment
			if (methodComment != null) {
				methodComment = methodComment.split("@")[0];
				int lastIndexOf = methodComment.lastIndexOf("\n");
				if (lastIndexOf >= 0) {
					methodComment = methodComment.substring(0, lastIndexOf); // include the \n
				}
				writer.append("\t *").append(methodComment).append("\t *\n");
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
		} catch (IOException ex) {
		}
	}

	/**
	 * Create javascript method body
	 *
	 * @param classname
	 * @param methodElement
	 * @param arguments
	 * @param writer
	 */
	protected void createMethodBody(String classname, ExecutableElement methodElement, Iterator<String> arguments, Writer writer) {
		try {
			String methodName = methodElement.getSimpleName().toString();
			writer.append("\t\tvar op = \"").append(methodName).append("\";\n");
			StringBuilder args = new StringBuilder("");
			StringBuilder paramNames = new StringBuilder("");
			StringBuilder keys = new StringBuilder("");
			if (arguments != null && arguments.hasNext()) {
				JsCacheResult jcr = methodElement.getAnnotation(JsCacheResult.class);
				boolean allArgs = true;
				// if there is a jcr annotation with value diferrent of *, so we dont use all arguments
				if (null != jcr && null!=jcr.keys() && (jcr.keys().length == 0 || (jcr.keys().length > 0 && !"*".equals(jcr.keys()[0])))) {
					allArgs = false;
					for (int i = 0; i < jcr.keys().length; i++) {
						String arg = jcr.keys()[i];
						keys.append(arg);
						if(i<jcr.keys().length-1) {
							keys.append(",");
						}
					}
//					keys.append(String.join(",", jcr.keys()));
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
			String md5 = "\"" + getMd5(classname + "." + methodName);
			writer.append("\t\tvar id = " + md5 + "_\" + JSON.stringify([").append(keys.toString()).append("]).md5();\n");
			writer.append("\t\treturn OcelotTokenFactory.createCallToken(this.ds, id, op, [").append(paramNames.toString()).append("], [").append(args.toString()).append("]").append(");\n");
		} catch (IOException ex) {
		}
	}

	/**
	 * Create a md5 from string
	 *
	 * @param msg
	 * @return
	 */
	private String getMd5(String msg) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] bytes = msg.getBytes();
			md.update(bytes, 0, bytes.length);
			return new BigInteger(1, md.digest()).toString(16);
		} catch (NoSuchAlgorithmException ex) {
		}
		return null;
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

	/**
	 * Validation management
	 *
	 * @AssertFalse : The value of the field or property must be false.
	 * @AssertTrue : The value of the field or property must be true.
	 * @DecimalMax : @DecimalMax("30.00") : The value of the field or property must be a decimal value lower than or equal to the number in the value element.
	 * @DecimalMin : @DecimalMin("5.00") : The value of the field or property must be a decimal value greater than or equal to the number in the value element.
	 * @Digits : @Digits(integer=6, fraction=2) : The value of the field or property must be a number within a specified range. The integer element specifies the maximum integral digits for the number,
	 * and the fraction element specifies the maximum fractional digits for the number.
	 * @Future : The value of the field or property must be a date in the future.
	 * @Max : @Max(10) : The value of the field or property must be an integer value lower than or equal to the number in the value element.
	 * @Min : @Min(5) : The value of the field or property must be an integer value greater than or equal to the number in the value element.
	 * @NotNull : The value of the field or property must not be null.
	 * @Null : The value of the field or property must be null.
	 * @Past : The value of the field or property must be a date in the past.
	 * @Pattern : @Pattern(regexp="\\(\\d{3}\\)\\d{3}-\\d{4}") : The value of the field or property must match the regular expression defined in the regexp element.
	 * @Size : @Size(min=2, max=240) : The size of the field or property is evaluated and must match the specified boundaries. If the field or property is a String, the size of the string is evaluated.
	 * If the field or property is a Collection, the size of the Collection is evaluated. If the field or property is a Map, the size of the Map is evaluated. If the field or property is an array, the
	 * size of the array is evaluated. Use one of the optional max or min elements to specify the boundaries.
	 */
}
