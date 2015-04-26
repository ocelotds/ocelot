/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.processors;

import fr.hhdev.ocelot.annotations.DataService;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author hhfrancois
 */
@SupportedAnnotationTypes(value = {"fr.hhdev.ocelot.annotations.DataService"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class OcelotProcessor extends AbstractProcessor {

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
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		// On verifie si le set d'annotation est vide (elles ont déjà été traitées au round précédent)
		if (roundEnv.processingOver()) {
			return true; // Si c'est le cas on s'arrete la
		}
		// Récupération des packages annotés       
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "ocelot-services.js");
			try (Writer w = resource.openWriter()) {
				ElementVisitor visitor = new DataServiceVisitor(processingEnv);
				for (Element element : roundEnv.getElementsAnnotatedWith(DataService.class)) {
					messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " ==== GENERATION PROCESS ====== " + element);
					element.accept(visitor, w);
//					List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
//					for (AnnotationMirror annotationMirror : annotationMirrors) {
//					}
				}
			}
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		return true;
	}
}
