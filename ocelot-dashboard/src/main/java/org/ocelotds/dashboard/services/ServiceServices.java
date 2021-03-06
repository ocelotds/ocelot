/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DashboardOnDebug;
import org.ocelotds.annotations.DataService;
import org.ocelotds.core.UnProxyClassServices;
import org.ocelotds.dashboard.objects.OcelotMethod;
import org.ocelotds.dashboard.objects.OcelotService;
import org.ocelotds.dashboard.security.DashboardSecureProvider;
import org.ocelotds.marshalling.exceptions.JsonMarshallerException;
import org.ocelotds.objects.Options;
import org.ocelotds.security.OcelotSecured;

/**
 *
 * @author hhfrancois
 */
@DataService
@DashboardOnDebug
@OcelotSecured(provider = DashboardSecureProvider.class)
public class ServiceServices {
	@Inject
	private ServiceTools serviceTools;

	@Inject
	private UnProxyClassServices unProxyClassServices;
	
	@Any
	@Inject
	@DataService
	private Instance<Object> dataservices;

	/**
	 * Return all services present in application
	 * [{"name":"instancename", methods=[{"name":"methodname","returntype":"void","argtypes":["",""],"argnames":["name1","name2"],"argtemplates":["",""]}]}]
	 * @param httpSession
	 * @return 
	 */
	public List<OcelotService> getServices(HttpSession httpSession) {
		List<OcelotService> result = new ArrayList<>();
		Options options = (Options) httpSession.getAttribute(Constants.Options.OPTIONS);
		if(options == null) {
			options = new Options();
			httpSession.setAttribute(Constants.Options.OPTIONS, options);
		}
		options.setMonitor(true);
		for (Object dataservice : dataservices) {
			Class<?> cls = unProxyClassServices.getRealClass(dataservice.getClass());
			if(!cls.isAnnotationPresent(DashboardOnDebug.class) || options.isDebug()) {
				OcelotService ocelotService = new OcelotService(serviceTools.getInstanceNameFromDataservice(cls));
				result.add(ocelotService);
				addMethodsToMethodsService(cls.getMethods(), ocelotService.getMethods());
			}
		}
		return result;
	}

	/**
	 * 
	 * @param methods
	 * @param ocelotService 
	 */
	void addMethodsToMethodsService(Method[] methods,  List<OcelotMethod> methodsService) {
		for (Method method : methods) {
			if (serviceTools.isConsiderateMethod(method)) {
				methodsService.add(getOcelotMethod(method));
			}
		}
	}
	
	OcelotMethod getOcelotMethod(Method method) {
		OcelotMethod ocelotMethod = new OcelotMethod(method.getName(), serviceTools.getShortName(serviceTools.getLiteralType(method.getGenericReturnType())));
		Annotation[][] annotations = method.getParameterAnnotations();
		Type[] types = method.getGenericParameterTypes();
		int index = 0;
		for (Type type : types) {
			ocelotMethod.getArgtypes().add(serviceTools.getShortName(serviceTools.getLiteralType(type)));
			try {
				ocelotMethod.getArgtemplates().add(serviceTools.getTemplateOfType(type, serviceTools.getJsonMarshaller(annotations[index])));
			} catch (JsonMarshallerException ex) {
				ocelotMethod.getArgtemplates().add(type.getTypeName());
			}
			index++;
		}
		return ocelotMethod;
	}
}
