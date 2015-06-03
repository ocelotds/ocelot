package demo;

import fr.hhdev.ocelot.annotations.DataService;

/**
 * Classe de test sur le pojoResolver
 * @author hhfrancois
 */
@DataService(resolver = "pojo")
public class TestPojoService {

	public String getMessage(int i) {
		return "Message from pojo service getMessage(int "+i+")";
	}

	public String getMessage(String i) {
		return "Message from pojo service getMessage(String "+i+")";
	}
}
