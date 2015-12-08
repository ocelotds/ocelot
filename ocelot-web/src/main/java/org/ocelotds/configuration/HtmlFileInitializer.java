/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.slf4j.Logger;

/**
 * Create ocelot.htm
 * @author hhfrancois
 */
public class HtmlFileInitializer extends AbstractFileInitializer {
	final static private String CONTENT_RESOURCE = Constants.SLASH + Constants.CONTENT + Constants.HTML;

	@Inject
	@OcelotLogger
	private Logger logger;

	public void initHtmlFile(@Observes @Initialized(ApplicationScoped.class) ServletContext sc) {
		logger.debug("ocelot.htm generation...");
		try {
			// create tmp/ocelot.html
			File htmlFile = createOcelotHtmlFile(sc.getContextPath());
			sc.setInitParameter(Constants.OCELOT_HTML, htmlFile.getAbsolutePath());
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.html.", ex);
		}
		logger.info("ocelot.htm generated : {}", sc.getInitParameter(Constants.OCELOT_HTML));
	}

	public void deleteHtmlFile(@Observes @Destroyed(ApplicationScoped.class) ServletContext sc) {
		deleteFile(sc.getInitParameter(Constants.OCELOT_HTML));
	}

	/**
	 * Create ocelot-services.html from the concatenation of all services available from all modules
	 *
	 * @return
	 * @throws IOException
	 */
	File createOcelotHtmlFile(String ctxPath) throws IOException {
		File file = File.createTempFile(Constants.OCELOT, Constants.HTML);
		try (OutputStream out = new FileOutputStream(file)) {
			writeOcelotContentHTMLFile(out, ctxPath);
		}
		return file;
	}

	/**
	 * Write content.htm part and replace contextpath token
	 *
	 * @param writer
	 * @param ctxPath
	 * @return
	 * @throws IOException
	 */
	void writeOcelotContentHTMLFile(OutputStream out, String ctxPath) throws IOException {
		URL content = this.getClass().getResource(CONTENT_RESOURCE);
		if (null == content) {
			throw new IOException("File " + CONTENT_RESOURCE + " not found in classpath.");
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(content.openStream(), Constants.UTF_8))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				out.write(inputLine.replaceAll(Constants.CTXPATH, ctxPath).getBytes(Constants.UTF_8));
				out.write(Constants.BACKSLASH_N.getBytes(Constants.UTF_8));
			}
		}
	}
}
