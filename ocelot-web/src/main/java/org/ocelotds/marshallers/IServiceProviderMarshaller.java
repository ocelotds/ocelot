/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.marshallers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.enterprise.inject.Instance;
import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.marshalling.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;

/**
 *
 * @author hhfrancois
 */
public class IServiceProviderMarshaller implements JsonMarshaller<Instance<IServicesProvider>> {

	/**
	 *
	 * @param jsonServicesProviders
	 * @return
	 * @throws JsonMarshallingException
	 */
	@Override
	public String toJson(Instance<IServicesProvider> jsonServicesProviders) throws JsonMarshallingException {
		if (jsonServicesProviders != null) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				out.write("[".getBytes(Constants.UTF_8));
				boolean first = true;
				for (IServicesProvider servicesProvider : jsonServicesProviders) {
					if (!first) {
						out.write(",\n".getBytes(Constants.UTF_8));
					}
					if(servicesProvider.streamJavascriptServices(out)) {
						first = false;
					}
				}
				out.write("]".getBytes(Constants.UTF_8));
				return out.toString(Constants.UTF_8);
			} catch (IOException e) {
			}
		}
		return "[]";
	}
}
