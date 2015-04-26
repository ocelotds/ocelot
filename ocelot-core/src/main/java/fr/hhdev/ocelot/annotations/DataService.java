package fr.hhdev.ocelot.annotations;

import fr.hhdev.ocelot.Constants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation permettant d'identifier les classes à exposer aux clients
 * @author hhfrancois
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataService {

	String resolverid() default Constants.Resolver.EJB;
}
