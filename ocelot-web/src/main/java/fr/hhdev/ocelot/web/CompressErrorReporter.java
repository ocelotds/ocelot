/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.web;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
public class CompressErrorReporter implements ErrorReporter {
	
	private final String filename;
	
	public CompressErrorReporter(String filename) {
		this.filename = filename;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(CompressErrorReporter.class);

	@Override
	public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
//		if (line < 0) {
//			logger.warn("[WARNING] in " + filename+"  " + message);
//		} else {
//			logger.warn("[WARNING] in " + filename+"  " + line + ':' + lineOffset + ':' + message);
//		}
	}

	@Override
	public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
		if (line < 0) {
			logger.error("[ERROR] in " + filename+"  " + message);
		} else {
			logger.error("[ERROR] in " + filename+"  " + line + ':' + lineOffset + ':' + message);
		}
	}

	@Override
	public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset);
		return new EvaluatorException(message);
	}
}
