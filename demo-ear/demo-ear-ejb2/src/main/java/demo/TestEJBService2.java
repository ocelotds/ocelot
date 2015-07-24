package demo;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import fr.hhdev.ocelot.annotations.JsCacheResult;
import javax.ejb.Stateless;

/**
 * Classe de test sur l'ejbResolver
 * @author hhfrancois
 */
@Stateless
@DataService(resolver = Constants.Resolver.EJB)
public class TestEJBService2 {

	public String getMessage(int i) {
		return "Message from ejb service getMessage(int "+i+")";
	}

	public String getMessage(String i) {
		return "Message from ejb service getMessage(String "+i+")";
	}

	@JsCacheResult(minute = 1)
	public String getMessageCached() {
		return "Message from ejb service getMessageCached():"+Math.random();
	}
}
