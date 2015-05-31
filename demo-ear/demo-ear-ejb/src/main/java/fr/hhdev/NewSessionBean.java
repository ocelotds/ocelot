/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;

/**
 *
 * @author martin
 */
@Stateless
@LocalBean
@DataService(resolver = Constants.Resolver.EJB)
public class NewSessionBean {

    public void businessMethod() {
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

}
