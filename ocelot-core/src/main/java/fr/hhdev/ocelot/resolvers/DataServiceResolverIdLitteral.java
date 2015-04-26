package fr.hhdev.ocelot.resolvers;

import fr.hhdev.ocelot.spi.DataServiceResolverId;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Instance of Annotation
 * @author hhfrancois
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class DataServiceResolverIdLitteral extends AnnotationLiteral<DataServiceResolverId> implements DataServiceResolverId {

	private final String val;
	public DataServiceResolverIdLitteral(String val) {
		this.val = val;
	}
	@Override
	public String value() {
		return val;
	}
	
}
