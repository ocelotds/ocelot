/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard.decorators;

import java.util.Collection;
import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.annotations.JsTopicEvent;
import org.ocelotds.dashboard.objects.SessionInfo;
import org.ocelotds.topic.SessionManager;

/**
 *
 * @author hhfrancois
 */
@Decorator
@Priority(0)
public abstract class SessionManagerMonitor implements SessionManager {

	@Inject
	@Delegate
	@Any
	SessionManager sessionManager;

	@Inject
	@JsTopicEvent("sessioninfo-add")
	Event<SessionInfo> addSessionInfo;

	@Inject
	@JsTopicEvent("sessioninfo-remove")
	Event<SessionInfo> removeSessionInfo;

	@Inject
	@JsTopicEvent("sessioninfo-update")
	Event<SessionInfo> updateSessionInfo;

	public void addSession(String httpid, Session session) {
		sessionManager.addSession(httpid, session);
		addSessionInfo.fire(getSessionInfo(httpid, session));
	}

	public Session closeOldSessionForHttp(String httpid) {
		Session s = sessionManager.closeOldSessionForHttp(httpid);
		if(null != s) {
			updateSessionInfo.fire(getSessionInfo(httpid, s));
		}
		return s;
	}

	public Collection<String> removeSession(Session session) {
		Collection<String> ids = sessionManager.removeSession(session);
		for (String id : ids) {
			removeSessionInfo.fire(getSessionInfo(id, session));
		}
		return ids;
	}
	
	SessionInfo getSessionInfo(String httpid, Session session) {
		return new SessionInfo(httpid, getUsername(session), session.isOpen());
	}

}
