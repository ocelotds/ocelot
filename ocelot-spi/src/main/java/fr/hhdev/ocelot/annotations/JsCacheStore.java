/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.annotations;

/**
 * Define cache store
 * @author hhfrancois
 */
public enum JsCacheStore {
		/**
		 * The result will not be store in cache
		 */
		NONE, 
		/**
		 * The result should be store in browser storage (localStorage)
		 */
		BROWSER, 
		/**
		 * The result should be store in session storage (sessionStorage)
		 */
		SESSION;
}
