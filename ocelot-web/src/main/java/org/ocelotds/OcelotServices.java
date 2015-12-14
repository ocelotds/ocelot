/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheRemove;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.core.SessionManager;
import org.ocelotds.core.UpdatedCacheManager;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopic;
import org.ocelotds.annotations.JsTopicName;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.ServiceProvider;
import org.ocelotds.annotations.TransientDataService;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.marshallers.LocaleMarshaller;
import org.ocelotds.marshallers.LocaleUnmarshaller;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.annotations.JsonUnmarshaller;
import org.ocelotds.objects.OcelotMethod;
import org.ocelotds.objects.OcelotService;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@DataService(resolver = Constants.Resolver.CDI)
public class OcelotServices {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	private UpdatedCacheManager updatedCacheManager;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private OcelotContext ocelotContext;

	@Inject
	private Session session;
	
	@Inject
	private ObjectMapper objectMapper;

	@Any
	@Inject
	@DataService(resolver = "")
	private Instance<Object> dataservices;

	@Any
	@Inject
	@ServiceProvider(Constants.Provider.JSON)
	private Instance<IServicesProvider> jsonServicesProviders;

	/**
	 * define locale for current user
	 *
	 * @param locale
	 */
	@JsCacheRemove(cls = OcelotServices.class, methodName = "getLocale", keys = {})
	public void setLocale(@JsonUnmarshaller(LocaleUnmarshaller.class) Locale locale) {
		ocelotContext.setLocale(locale);
	}

	/**
	 * get current user locale
	 *
	 * @return
	 */
	@JsCacheResult(year = 1)
	@JsonMarshaller(LocaleMarshaller.class)
	public Locale getLocale() {
		logger.debug("Receive getLocale call from client.");
		return ocelotContext.getLocale();
	}

	/**
	 * return current username from session
	 *
	 * @return
	 */
	public String getUsername() {
		return ocelotContext.getPrincipal().getName();
	}

	public Collection<String> getOutDatedCache(Map<String, Long> states) {
		return updatedCacheManager.getOutDatedCache(states);
	}

	@JsTopic
	public Integer subscribe(@JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic) throws IllegalAccessException {
		return sessionManager.registerTopicSession(topic, session);
	}

	@JsTopic
	public Integer unsubscribe(@JsTopicName(prefix = Constants.Topic.SUBSCRIBERS) String topic) {
		return sessionManager.unregisterTopicSession(topic, session);
	}

	public Integer getNumberSubscribers(String topic) {
		return sessionManager.getNumberSubscribers(topic);
	}

//	@JsonMarshaller(IServiceProviderMarshaller.class)
//	public Instance<IServicesProvider> getServices2() {
//		return jsonServicesProviders;
//	}
	/**
	 * Return all services present in application
	 * [{"name":"instancename", methods=[{"name":"methodname","returntype":"void","argtypes":["",""],"argnames":["name1","name2"],"argtemplates":["",""]}]}]
	 * @return 
	 */
	public List<OcelotService> getServices() {
		List<OcelotService> result = new ArrayList<>();
		for (Object dataservice : dataservices) {
			Class cls = getRealClass(dataservice.getClass());
			OcelotService ocelotService = new OcelotService(getInstanceNameFromDataservice(cls));
			result.add(ocelotService);
			Method[] methods = cls.getDeclaredMethods();
			for (Method method : methods) {
				if (isConsiderateMethod(method)) {
					OcelotMethod ocelotMethod = new OcelotMethod(method.getName(), getLiteralType(method.getGenericReturnType()));
					ocelotService.getMethods().add(ocelotMethod);
					Annotation[][] annotations = method.getParameterAnnotations();
					Type[] types = method.getGenericParameterTypes();
					int index = 0;
					for (Type type : types) {
						ocelotMethod.getArgtypes().add(getLiteralType(type));
						ocelotMethod.getArgnames().add("arg"+index);
						ocelotMethod.getArgtemplates().add(getTemplateOfType(type, isJsonUnmarshallerAnnotationPresent(annotations[index])));
						index++;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Return human reading of type
	 * @param type
	 * @return 
	 */
	String getLiteralType(Type type) {
		String result;
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			result = parameterizedType.toString();
		} else {
			result = ((Class) type).getCanonicalName();
		}
		return result;
	}

	/**
	 * The parameter is Annotation JsonUnmarshaller present
	 * @param annotations
	 * @return 
	 */
	private boolean isJsonUnmarshallerAnnotationPresent(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (JsonUnmarshaller.class.isAssignableFrom(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get template for unknown type (class or parameterizedType)
	 * @param type
	 * @param jsonUnmarshaller
	 * @return 
	 */
	private String getTemplateOfType(Type type, boolean jsonUnmarshaller) {
		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			return getTemplateOfParameterizedType((ParameterizedType) type);
		}
		return getTemplateOfClass((Class) type, jsonUnmarshaller);
	}

	/**
	 * Get template for classic class
	 * @param cls
	 * @param jsonUnmarshaller
	 * @return 
	 */
	private String getTemplateOfClass(Class cls, boolean jsonUnmarshaller) {
		if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
			return "false";
		} else if (Integer.TYPE.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls) || Long.class.isAssignableFrom(cls)) {
			return "0";
		} else if (Float.TYPE.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)) {
			return "0.0";
		} else if (cls.isArray()) {
			return "[" + getTemplateOfClass(cls.getComponentType(), false) + "]";
		} else if (jsonUnmarshaller) {
			System.out.println("Object jsonUnmarshaller");
		} else {
			try {
				return objectMapper.writeValueAsString(cls.newInstance());
			} catch (InstantiationException | IllegalAccessException | JsonProcessingException ex) {
			}
		}
		return cls.getSimpleName().toLowerCase();
	}

	/**
	 * Get template from parameterizedType
	 * @param parameterizedType
	 * @return 
	 */
	private String getTemplateOfParameterizedType(ParameterizedType parameterizedType) {
		Class cls = (Class) parameterizedType.getRawType();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		System.out.println("Iterable.class.isAssignableFrom("+cls+") : "+Iterable.class.isAssignableFrom(cls));
		System.out.println("Map.class.isAssignableFrom("+cls+") : "+Map.class.isAssignableFrom(cls));
		if (Iterable.class.isAssignableFrom(cls)) {
			return getTemplateOfIterable(actualTypeArguments);
		} else if (Map.class.isAssignableFrom(cls)) {
			return getTemplateOfMap(actualTypeArguments);
		}
		return cls.toString();
	}

	/**
	 * Get template of iterable class from generic type
	 * @param actualTypeArguments
	 * @return 
	 */
	private String getTemplateOfIterable(Type[] actualTypeArguments) {
		String res = "[";
		for (Type actualTypeArgument : actualTypeArguments) {
			res += getTemplateOfType(actualTypeArgument, false);
		}
		return res + "]";
	}

	/**
	 * Get template of map class from generic type
	 * @param actualTypeArguments
	 * @return 
	 */
	private String getTemplateOfMap(Type[] actualTypeArguments) {
		String res = "{";
		boolean first = true;
		for (Type actualTypeArgument : actualTypeArguments) {
			if (!first) {
				res += ":";
			}
			res += getTemplateOfType(actualTypeArgument, false);
			first = false;
		}
		return res + "}";
	}

	/**
	 * Get instancename from Dataservice Class
	 * @param cls
	 * @return 
	 */
	private String getInstanceNameFromDataservice(Class cls) {
		DataService dataService = (DataService) cls.getAnnotation(DataService.class);
		String clsName = dataService.name();
		if (clsName.isEmpty()) {
			clsName = cls.getSimpleName();
		}
		return getInstanceName(clsName);
	}

	/**
	 * Get instancename from clasname
	 * @param clsName
	 * @return 
	 */
	private String getInstanceName(String clsName) {
		return clsName.substring(0, 1).toLowerCase() + clsName.substring(1);
	}

	/**
	 * Get class without proxy CDI
	 * based on $ separator
	 * @param proxy
	 * @return 
	 */
	private Class getRealClass(Class proxy) {
		String proxyname = proxy.getName();
		int index = proxyname.indexOf("$");
		if (index != -1) {
			try {
				return Class.forName(proxyname.substring(0, index));
			} catch (ClassNotFoundException ex) {
			}
		}
		return proxy;
	}

	/**
	 * Method is expose to frontend ?
	 * @param method
	 * @return 
	 */
	private boolean isConsiderateMethod(Method method) {
		if (method.isAnnotationPresent(TransientDataService.class)) {
			return false;
		}
		int modifiers = method.getModifiers();
		return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
	}
}
