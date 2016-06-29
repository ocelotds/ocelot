/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.marshallers;

import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonMarshallingException;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;

/**
 *
 * @author hhfrancois
 */
public class SetSessionMarshaller implements IJsonMarshaller<Set<Session>>{

	@Override
	public String toJson(Set<Session> objs) throws JsonMarshallingException {
		StringBuilder result = new StringBuilder("[");
		boolean first = true;
		for (Session obj : objs) {
			if(!first) {
				result.append(",");
			}
			result.append("\"").append(obj.getId()).append("\"");
			first = false;
		}
		result.append("]");
		return result.toString();
	}

	@Override
	public Set<Session> toJava(String json) throws JsonUnmarshallingException {
		return new HashSet<>();
	}
	
}
