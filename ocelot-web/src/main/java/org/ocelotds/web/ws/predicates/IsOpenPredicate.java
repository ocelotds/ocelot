/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.ws.predicates;

import java.util.function.Predicate;
import javax.websocket.Session;

/**
 *
 * @author hhfrancois
 */
public class IsOpenPredicate implements Predicate<Session>{

	@Override
	public boolean test(Session t) {
		return t.isOpen();
	}
	
}
