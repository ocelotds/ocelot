/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.annotations.JsCacheResult;
import fr.hhdev.ocelot.annotations.TransientDataService;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
			writer.append("\tthis.ds = \"").append(typeElement.getQualifiedName().toString()).append("\";\n");
			List<ExecutableElement> methodElements = ElementFilter.methodsIn(typeElement.getEnclosedElements());
			for (ExecutableElement methodElement : methodElements) {
				if (isConsiderateMethod(methodElement)) {
					String methodName = methodElement.getSimpleName().toString();
					List<String> argumentsType = getArgumentsType(methodElement);
					List<String> arguments = getArguments(methodElement);
					TypeMirror returnType = methodElement.getReturnType();
					writer.append("\n");
					createMethodComment(methodElement, arguments, argumentsType, returnType, writer);

					writer.append("\tthis.").append(methodName).append(" = function (");
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

					createMethodBody(methodElement, arguments.iterator(), writer);

					writer.append("\t};\n");
				}
			}
			writer.append("}\n");
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
	 * @param methodElement
	 * @return
	 */
	public boolean isConsiderateMethod(ExecutableElement methodElement) {
		// Herited from Object
		TypeElement objectElement = environment.getElementUtils().getTypeElement(Object.class.getName());
		if(objectElement.getEnclosedElements().contains(methodElement)) {
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
			// Le commentaire de la javadoc
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
				writer.append("\t * @param ").append(argumentName).append(" : ").append(type).append("\n");
			}
			// Si la methode retourne ou non quelque chose
			if (!returnType.toString().equals("void")) {
				writer.append("\t * @return ").append(returnType.toString()).append("\n");
			}
			writer.append("\t */\n");
		} catch (IOException ex) {
		}
	}

	/**
	 * Create javascript method body
	 *
	 * @param methodElement
	 * @param arguments
	 * @param writer
	 */
	protected void createMethodBody(ExecutableElement methodElement, Iterator<String> arguments, Writer writer) {
		try {
			writer.append("\t\tvar op = \"").append(methodElement.getSimpleName()).append("\";\n");
			StringBuilder args = new StringBuilder("");
			StringBuilder paramNames = new StringBuilder("");
			StringBuilder keys = new StringBuilder("");
			if (arguments != null && arguments.hasNext()) {
				JsCacheResult jcr = methodElement.getAnnotation(JsCacheResult.class);
				boolean allArgs = Objects.isNull(jcr) || (jcr.keys().length > 0 && "*".equals(jcr.keys()[0]));
				if(!allArgs) {
					keys.append(String.join(",", jcr.keys()));
				}
				while (arguments.hasNext()) {
					String arg = arguments.next();
					if(allArgs) {
						keys.append(arg);
					}
					args.append(arg);
					paramNames.append("\"").append(arg).append("\"");
					if (arguments.hasNext()) {
						args.append(",");
						paramNames.append(",");
						if(allArgs) {
							keys.append(",");
						}
					}
				}
			}
			writer.append("\t\tvar id = (this.ds + \".\" + op + \"(\" + JSON.stringify([").append(keys.toString()).append("]) + \")\");\n");
			writer.append("\t\treturn getOcelotToken.call(this, id.md5(), op, [").append(paramNames.toString()).append("], [").append(args.toString()).append("]").append(");\n");
		} catch (IOException ex) {
		}
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
