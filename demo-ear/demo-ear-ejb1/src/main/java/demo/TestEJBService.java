package demo;

import javax.ejb.LocalBean;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import javax.ejb.Stateless;

/**
 * Classe de test sur l'ejbResolver
 * @author hhfrancois
 */
@Stateless
@LocalBean
@DataService(resolver = Constants.Resolver.EJB)
public class TestEJBService {

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

	@JsCacheResult(minute = 1, keys = {"i", "u.id", "s", "b"})
	public String getMessageCached2(int i, User u, long l, short s, boolean b) {
		return "Message from ejb service getMessageCached():"+Math.random();
	}
}
