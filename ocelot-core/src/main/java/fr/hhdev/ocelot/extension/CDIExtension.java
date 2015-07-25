/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Locale;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public class CDIExtension implements Extension {

	private final static Logger logger = LoggerFactory.getLogger(CDIExtension.class);

	private static final Annotation CONTEXTUALIZED_AT = new AnnotationLiteral<Contextualized>() {
	};
	private static final Annotation INJECT_AT = new AnnotationLiteral<Inject>() {
	};
	private static final Annotation INIT_AT = new AnnotationLiteral<InitialLanguage>() {
	};

	void processProducer(@Observes ProcessProducer pp) {
		Producer producer = pp.getProducer();
		if (producer.toString().contains("InitialLanguage")) {
			logger.debug("{}", producer);
		}
	}

	void afterTypeDiscovery(@Observes AfterTypeDiscovery atd) {
		logger.debug("SPI create builder");
		AnnotatedTypeBuilder<Locale> builder = new AnnotatedTypeBuilder();
		logger.debug("SPI add class");
		builder.readFromType(Locale.class);
		Constructor<Locale> constructor = null;
		try {
			logger.debug("SPI get constructor");
			constructor = Locale.class.getConstructor(String.class);
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new DeploymentException(ex); // meilleure pratique de faire avorter le déployement si tu as ce genre de problème.
		}
		logger.debug("SPI get constructor {}", constructor.toString());
		logger.debug("SPI add Qualifier @InitialLanguage sur arg0 du constructeur");
		builder.addToConstructorParameter(constructor, 0, INIT_AT);
		/*
		 L'annotaion @Inject ne peut pas être utilisée sur un paramètre, mais sur le constructeur
		 */
		builder.addToConstructor(constructor, INJECT_AT); // @Inject sur le constructeur pour dire que ses paramètres doivent être injectés
		logger.debug("SPI add @Contextualized sur class");
		builder.addToClass(CONTEXTUALIZED_AT); // inutile de rajouter l'annotation Default
		logger.debug("SPI add AnnotatedType that will become a bean");
		atd.addAnnotatedType(builder.create(), "locale_ctxz");
		logger.debug("{} is now a bean", Locale.class);
	}
}
