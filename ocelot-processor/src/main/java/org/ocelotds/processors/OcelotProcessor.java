/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ocelotds.processors;

import org.ocelotds.annotations.DataService;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.Random;
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
 * Processor of annotation org.ocelotds.annotations.DataService
 * @author hhfrancois
 */
@SupportedAnnotationTypes(value = {"org.ocelotds.annotations.DataService"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class OcelotProcessor extends AbstractProcessor {

	private final static Random random = new Random();

	private boolean disabled = false;
	/**
	 * Tools for access filesystem
	 */
	private Filer filer;

	/**
	 * Tools for log processor
	 */
	private Messager messager;

	/**
	 * Init processor<br>
	 * get filer, messager<br>
	 * get options
	 * 
	 */
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
		Properties options = new Properties();
		try(Reader reader = new FileReader("ocelot.properties")) {
			options.load(reader);
			Object value = options.get("disabled");
			disabled = false;
			if(null!=value) {
				disabled = Boolean.parseBoolean((String) value);
			}
		} catch(IOException e) {
		}
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// check if process was done on previous round
		if (roundEnv.processingOver() || disabled) {
			return true; // Si c'est le cas on s'arrete la
		}
		// Create provider of ocelot-services.js
		String js = createJSServicesProvider();
		// Create file ocelot-services.js      
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", js);
			try (Writer writer = resource.openWriter()) {
				ElementVisitor visitor = new DataServiceVisitor(processingEnv);
				for (Element element : roundEnv.getElementsAnnotatedWith(DataService.class)) {
					messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " javascript generation class : " + element);
					element.accept(visitor, writer);
				}
			}
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		disabled = true;
		return true;
	}
	
	/**
	 *  Create provider of ocelot-services.js and return a part of unic name for ocelot-service.js
	 * 
	 * @return 
	 */
	private String createJSServicesProvider() {
		// Creation du provider de ocelot-services.js
		String prefix = "srv_" + random.nextInt(100_000_000);
		try {
			String servicesName = "ServiceProvider";
			FileObject servicesProvider = filer.createSourceFile(prefix+"." + servicesName);
			try (Writer writer = servicesProvider.openWriter()) {
				writer.append("package " + prefix + ";\n");
				writer.append("import org.ocelotds.Constants;\n");
				writer.append("import java.io.InputStream;\n");
				writer.append("import java.io.IOException;\n");
				writer.append("import java.io.Writer;\n");
				writer.append("import java.io.BufferedReader;\n");
				writer.append("import java.io.InputStreamReader;\n");
				writer.append("import org.ocelotds.IServicesProvider;\n");
				writer.append("import org.slf4j.Logger;\n");
				writer.append("import org.slf4j.LoggerFactory;\n");
				writer.append("public class ServiceProvider implements IServicesProvider {\n");
				writer.append("	private static final Logger logger = LoggerFactory.getLogger("+servicesName+".class);\n");
				writer.append("	@Override\n");
				writer.append("	public void streamJavascriptServices(Writer writer) {\n");
				writer.append("		InputStream injs = this.getClass().getClassLoader().getResourceAsStream(\""+prefix+".js\");\n");
				writer.append("		try (BufferedReader in = new BufferedReader(new InputStreamReader(injs, Constants.UTF_8))) {\n");
				writer.append("			String inputLine;\n");
				writer.append("			while ((inputLine = in.readLine()) != null) {\n");
				writer.append("				writer.write(inputLine);\n");
				writer.append("				writer.write(Constants.BACKSLASH_N);\n");
				writer.append("			}\n");
				writer.append("		} catch(IOException ex) {\n");
				writer.append("			logger.error(\"Generation of '"+prefix+".js' failed.\", ex);\n");
				writer.append("		}\n");
				writer.append("	}\n");
				writer.append("}");
			}
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		return prefix+".js";
	}
}
