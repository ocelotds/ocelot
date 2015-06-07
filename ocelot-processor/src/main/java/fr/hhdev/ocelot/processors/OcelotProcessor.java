/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.Constants;
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
import javax.tools.StandardLocation;

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
		if (roundEnv.processingOver() || disabled) {
			return true; // Si c'est le cas on s'arrete la
		}
		// Creation du provider de ocelot-services.js
		String js = createJSServicesProvider();
		// Creation du fichier ocelot-services.js      
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", js);
			try (Writer writer = resource.openWriter()) {
				ElementVisitor visitor = new DataServiceVisitor(processingEnv);
				for (Element element : roundEnv.getElementsAnnotatedWith(DataService.class)) {
					messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " JAVASCRIPT GENERATION CLASS : " + element);
					element.accept(visitor, writer);
				}
			}
		} catch (Exception e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		disabled = true;
		return true;
	}
	
	/**
	 * Crée le provider de ocelot-services.js
	 * 
	 * @return 
	 */
	private String createJSServicesProvider() {
		// Creation du provider de ocelot-services.js
		String seed = ("" + Math.random()).replaceAll("\\.", "");
		try {
			String servicesName = "ServiceProvider"+seed;
			FileObject servicesProvider = filer.createSourceFile("services." + servicesName);
			try (Writer writer = servicesProvider.openWriter()) {
				writer.append("package services;\n");
				writer.append("import fr.hhdev.ocelot.Constants;\n");
				writer.append("import java.io.InputStream;\n");
				writer.append("import java.io.OutputStream;\n");
				writer.append("import java.io.IOException;\n");
				writer.append("import fr.hhdev.ocelot.IServicesProvider;\n");
				writer.append("import org.slf4j.Logger;\n");
				writer.append("import org.slf4j.LoggerFactory;\n");
				writer.append("public class " + servicesName + " implements IServicesProvider {\n");
				writer.append("	private static final Logger logger = LoggerFactory.getLogger("+servicesName+".class);\n");
				writer.append("	@Override\n");
				writer.append("	public void streamJavascriptServices(OutputStream out) {\n");
				writer.append("		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(\"srv_"+seed+".js\")) {\n");
				writer.append("			byte[] buffer = new byte[1024];\n");
				writer.append("			int read;\n");
				writer.append("			while((read = in.read(buffer, 0, 1024))>=0) {\n");
				writer.append("				out.write(buffer, 0, read);\n");
				writer.append("			}\n");
				writer.append("		} catch(IOException ex) {\n");
				writer.append("			logger.error(\"Generation of 'srv_"+seed+".js' failed.\", ex);\n");
				writer.append("		}\n");
				writer.append("	}\n");
				writer.append("}");
			}
		} catch (Exception e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		return "srv_"+seed+".js";
	}
}
