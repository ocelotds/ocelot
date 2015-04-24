/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.annotations.DataService;
import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author francois
 */
@SupportedAnnotationTypes(value = {"fr.hhdev.ocelot.DataService"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JavascriptProcessor extends AbstractProcessor {

	/**
	 * Utilitaire pour accéder au système de fichiers
	 */
	private Filer filer;

	/**
	 * Utilitaire pour afficher des messages lors de la compilation
	 */
	private Messager messager;

	/**
	 * Initialisation de l'Annotation Processor. Permet surtout de rÃ©cupÃ©rer des rÃ©fÃ©rences vers le Filer et le Messager
	 */
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// RÃ©cupÃ©ration des packages annotÃ©s       
		Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(DataService.class);
		for (Element annotatedPackage : annotatedElements) {
//			try {
//				for (StandardLocation location : StandardLocation.values()) {
//					messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "" + location);
//					if (location.isOutputLocation()) {
//						FileObject createResource = filer.createResource(location, "root", "Generated");
//						messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "" + createResource.toUri().getPath());
//						try (Writer writer = createResource.openWriter()) {
//							writer.write(location.getName());
//						}
//					}
//				}
//				JavaFileObject createSourceFile = filer.createSourceFile("anno.Generated");
//				messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "" + createSourceFile.toUri().getPath());
//				try (Writer writer = createSourceFile.openWriter()) {
//					FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "test.js");
//					writer.write("package anno;\npublic class Generated {\n// " + resource.toUri().getPath() + "\n}");
//					try (Writer w = resource.openWriter()) {
//						w.write("alert('youpi')");
//					}
//				}
//			} catch (IOException e) {
//				messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
//			}
		}
		return true;
	}
}
