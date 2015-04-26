/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.TransientDataService;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
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
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Visitor of class annoted fr.hhdev.ocelot.annotations.DataService
 * Generate javascript classes
 * 
 * @author hhfrancois
 */
public class DataServiceVisitor implements ElementVisitor<String, Writer> {

	protected ProcessingEnvironment environment;

	public DataServiceVisitor(ProcessingEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public String visitType(TypeElement typeElement, Writer writer) {
		DataService annotation = typeElement.getAnnotation(DataService.class);
		try {
			createLicenceComment(writer);
			createClassComment(typeElement, writer);
			writer.append("function ").append(typeElement.getSimpleName()).append("() {\n");
			writer.append("\tthis.fid = \"").append(annotation.resolverid()).append("\";\n");
			writer.append("\tthis.ds = \"").append(typeElement.getQualifiedName().toString()).append("\";\n");
			List<ExecutableElement> methodElements = ElementFilter.methodsIn(typeElement.getEnclosedElements());
			for (ExecutableElement methodElement : methodElements) {
				if (isConsiderateMethod(methodElement)) {
					String methodName = methodElement.getSimpleName().toString();
					List<String> argumentsType = getArgumentsType(methodElement);
					List<String> arguments = getArguments(methodElement);
					TypeMirror returnType = methodElement.getReturnType();
					writer.append("\n");
					createMethodComment(methodElement, arguments, returnType, writer);

					writer.append("\tthis.").append(methodName).append(" = function (");
					if (arguments.size() != argumentsType.size()) {
						environment.getMessager().printMessage(Diagnostic.Kind.ERROR, (new StringBuilder()).append("Cannot Create service : ").append(typeElement.getSimpleName()).append(" cause method ").append(methodElement.getSimpleName()).append(" arguments inconsistent - argNames : ").append(arguments.size()).append(" / args : ").append(argumentsType.size()).toString(), typeElement);
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

	/* This Source Code Form is subject to the terms of the Mozilla Public
	 * License, v. 2.0. If a copy of the MPL was not distributed with this
	 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
	 */
	/**
	 * RAjoute la licence MPL 2.0
	 * @param writer 
	 */
	protected void createLicenceComment(Writer writer) {
		try {
			writer.append("/* This Source Code Form is subject to the terms of the Mozilla Public\n");
			writer.append(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n");
			writer.append(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n");
			writer.append(" */\n");
		} catch(IOException ioe) {}
	}

	/**
	 * Crée un commentaire pour la classe
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
					if(comment!=null) {
						writer.append("/**\n *").append(comment.replaceAll("\n", "\n *")).append("/\n");
					}
				}
			} else {
				writer.append("/**\n *").append(comment.replaceAll("\n", "\n *")).append("/\n");
			}
		} catch(IOException ioe) {}
	}

	/**
	 * Retourne true si la methode doit etre traitee
	 *
	 * @param methodElement
	 * @return
	 */
	public boolean isConsiderateMethod(ExecutableElement methodElement) {
		// Si la méthode est annotée transient
		List<? extends AnnotationMirror> annotationMirrors = methodElement.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annotationMirrors) {
			if (annotationMirror.getAnnotationType().toString().equals(TransientDataService.class.getName())) {
				return false;
			}
		}

		// Si la méthode est statique ou non publique
		if (!methodElement.getModifiers().contains(Modifier.PUBLIC) || methodElement.getModifiers().contains(Modifier.STATIC)) {
			return false;
		}
		// Si ce n'est pas une méthode de Object
		TypeElement objectElement = environment.getElementUtils().getTypeElement(Object.class.getName());
		return !objectElement.getEnclosedElements().contains(methodElement);
	}

	/**
	 * Retourne la liste ordonnee du type des arguments de la methode
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
	 * Retourne la liste ordonnee du nom des arguments de la methode
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
	 * Creer la javadoc de la methode en javascript
	 *
	 * @param methodElement
	 * @param argumentsName
	 * @param returnType
	 * @param writer
	 */
	protected void createMethodComment(ExecutableElement methodElement, List<String> argumentsName, TypeMirror returnType, Writer writer) {
		try {
			String methodComment = environment.getElementUtils().getDocComment(methodElement);
			writer.write("\t/**\n");
			// Le commentaire de la javadoc
			if (methodComment != null) {
				methodComment = methodComment.split("@")[0];
				int lastIndexOf = methodComment.lastIndexOf("\n");
				if (lastIndexOf >= 0) {
					methodComment = methodComment.substring(0, lastIndexOf);
				}
				String comment = methodComment.replaceAll("\n", "\n\t *");
				writer.append("\t *\n").append(comment);
			}
			// La liste des arguments de la javadoc
			for (String argumentName : argumentsName) {
				writer.append("\t * @param ").append(argumentName).append("\n");
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
	 * Cree le corps de la methode
	 *
	 * @param methodElement
	 * @param arguments
	 * @param writer
	 */
	protected void createMethodBody(ExecutableElement methodElement, Iterator<String> arguments, Writer writer) {
		try {
			writer.append("\t\treturn getOcelotEvent.call(this, \"").append(methodElement.getSimpleName().toString()).append("\", [");
			if (arguments != null) {
				while (arguments.hasNext()) {
					writer.append(arguments.next());
					if (arguments.hasNext()) {
						writer.append(", ");
					}
				}
			}
			writer.append("]);\n");
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
