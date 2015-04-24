/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import fr.hhdev.ocelot.annotations.DataService;
import javax.ejb.Stateless;

/**
 *
 * @author francois
 */
@Stateless
@DataService(resolverid = "ejb")
public class TestService {

	public String getMessage(int i) {
		return "Message su server "+i;
	}

	public String getMessage(String i) {
		return "Message su server "+i;
	}
}
