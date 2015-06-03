/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.annotations.DataService;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Processor of annotation fr.hhdev.ocelot.annotations.DataService
 * @author hhfrancois
 */
@SupportedAnnotationTypes(value = {"fr.hhdev.ocelot.annotations.DataService"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class OcelotProcessor extends AbstractProcessor {

	private boolean disabled = false;
	/**
	 * Utilitaire pour accéder au système de fichiers
	 */
	private Filer filer;

	/**
	 * Utilitaire pour afficher des messages lors de la compilation
	 */
	private Messager messager;

	/**
	 * Initialisation de l'Annotation Processor. Permet surtout de récupérer des rÃ©fÃ©rences vers le Filer et le Messager
	 */
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
		File file = new File("ocelot.properties");
		Properties options = new Properties();
		try(Reader reader = new FileReader(file)) {
			options.load(reader);
			disabled = Boolean.parseBoolean((String) options.getOrDefault("disabled", false));
		} catch(IOException e) {
		}
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// On verifie si le set d'annotation est vide (elles ont déjà été traitées au round précédent)
		if (roundEnv.processingOver()) {
			return true; // Si c'est le cas on s'arrete la
		}
		if(disabled) {
			return true;
		}
		try {
			String seed = ("" + Math.random()).replaceAll("\\.", "");
			String servicesName = "ServiceProvider" + seed;
			FileObject servicesProvider = filer.createSourceFile("services." + servicesName);
			try (Writer writer = servicesProvider.openWriter()) {
				writer.append("package services;\n");
				writer.append("import java.io.Writer;\n");
				writer.append("import java.io.IOException;\n");
				writer.append("import fr.hhdev.ocelot.IServicesProvider;\n");
				writer.append("import javax.inject.Named;\n");
				writer.append("@Named\n");
				writer.append("public class " + servicesName + " implements IServicesProvider {\n");
				writer.append("	@Override\n");
				writer.append("	public void writeJavascriptServices(Writer writer) {\n");
				writer.append("		try {\n");
				ElementVisitor visitor = new DataServiceVisitor1(processingEnv);
				for (Element element : roundEnv.getElementsAnnotatedWith(DataService.class)) {
					messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " JAVASCRIPT GENERATION CLASS : " + element);
					element.accept(visitor, writer);
				}
				writer.append("		} catch(IOException ioe) {}\n");
				writer.append("	}\n");
				writer.append("}");
			}
			disabled = true;
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		} catch (Exception e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		// Récupération des packages annotés       
//		try {
//			File file = new File(webapp, "ocelot-services.js");
//			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "ocelot-services.js");
//			try (Writer writer = resource.openWriter()) {
////				try (Writer writer = new FileWriter(file)) {
//				createLicenceComment(writer);
//				ElementVisitor visitor = new DataServiceVisitor(processingEnv);
//				for (Element element : roundEnv.getElementsAnnotatedWith(DataService.class)) {
//					messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " JAVASCRIPT GENERATION CLASS : " + element);
//					element.accept(visitor, writer);
//				}
//			}
//		} catch (IOException e) {
//			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
//		} catch (Exception e) {
//		}
		return true;
	}

	/* This Source Code Form is subject to the terms of the Mozilla Public
	 * License, v. 2.0. If a copy of the MPL was not distributed with this
	 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
	 */
	/**
	 * Rajoute la licence MPL 2.0
	 *
	 * @param writer
	 */
	protected void createLicenceComment(Writer writer) {
		try {
			writer.append("/* This Source Code Form is subject to the terms of the Mozilla Public\n");
			writer.append(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n");
			writer.append(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n");
			writer.append(" * Classes generated by Ocelot Framework.\n");
			writer.append(" */\n");
		} catch (IOException ioe) {
		}
	}
}
