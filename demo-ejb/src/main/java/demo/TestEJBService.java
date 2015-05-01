package demo;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import javax.ejb.Stateless;

/**
 * Classe de test sur l'ejbResolver
 * @author hhfrancois
 */
@Stateless
@DataService(resolver = Constants.Resolver.EJB)
public class TestEJBService {

	public String getMessage(int i) {
		return "Message from ejb service getMessage(int "+i+")";
	}

	public String getMessage(String i) {
		return "Message from ejb service getMessage(String "+i+")";
	}
}
