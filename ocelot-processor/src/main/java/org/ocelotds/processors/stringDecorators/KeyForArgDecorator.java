/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.processors.stringDecorators;

/**
 *
 * @author hhfrancois
 */
public class KeyForArgDecorator implements StringDecorator {

	/**
	 * Transform arg to valid key. protect js NPE <br>
	 * considers if arg or subfield is null <br>
	 * example : if arg == c return c <br>
	 * if arg == c.user return (c)?c.user:null <br>
	 * if arg == c.user.u_id return (c&amp;&amp;c.user)?c.user.u_id:null <br>
	 *
	 * @param str
	 * @return
	 */
	@Override
	public String decorate(String str) {
		String[] objs = str.split("\\.");
		StringBuilder result = new StringBuilder();
		if (objs.length > 1) {
			StringBuilder obj = new StringBuilder();
			obj.append(objs[0]);
			result.append("(").append(obj);
			for (int i = 1; i < objs.length - 1; i++) {
				result.append("&&");
				obj.append(".").append(objs[i]);
				result.append(obj);
			}
			result.append(")?").append(str).append(":null");
		} else {
			result.append(str);
		}
		return result.toString();
	}
	
}
