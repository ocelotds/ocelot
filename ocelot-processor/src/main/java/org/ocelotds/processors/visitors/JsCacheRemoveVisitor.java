/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors.visitors;

import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

/**
 *
 * @author hhfrancois
 */
public class JsCacheRemoveVisitor implements ElementVisitor<Void, Writer> {

	protected final ProcessingEnvironment environment;
	protected final Messager messager;

	public JsCacheRemoveVisitor(ProcessingEnvironment environment) {
		this.environment = environment;
		this.messager = environment.getMessager();
	}

	@Override
	public Void visit(Element e, Writer p) {
		return null;
	}

	@Override
	public Void visit(Element e) {
		return null;
	}

	@Override
	public Void visitPackage(PackageElement e, Writer p) {
		return null;
	}

	@Override
	public Void visitType(TypeElement e, Writer p) {
		return null;
	}

	@Override
	public Void visitVariable(VariableElement e, Writer p) {
		return null;
	}

	@Override
	public Void visitExecutable(ExecutableElement e, Writer p) {
		try {
			p.append(e.getEnclosingElement().toString()).append(".").append(e.getSimpleName()).append("=");
			boolean first = true;
			for (VariableElement variableElement : e.getParameters()) {
				if (!first) {
					p.append(",");
				}
				p.append(variableElement.toString());
				first = false;
			}
			p.append("\n");
		} catch (IOException ex) {
		}
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterElement e, Writer p) {
		return null;
	}

	@Override
	public Void visitUnknown(Element e, Writer p) {
		return null;
	}

}
