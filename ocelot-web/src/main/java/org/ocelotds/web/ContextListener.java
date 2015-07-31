/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.configuration.OcelotConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 *
 * @author hhfrancois
 */
@WebListener
public final class ContextListener implements ServletContextListener {

	private final static Logger logger = LoggerFactory.getLogger(ContextListener.class);
	/**
	 * Minify option, breakline
	 */
	private static final int LINEBREAK = 80;
	/**
	 * Default size of stacktrace include in messageToClient fault
	 */
	private static final String DEFAULTSTACKTRACE = "50";

	@Inject
	@Any
	private Instance<IServicesProvider> servicesProviders;

	@Inject
	private OcelotConfiguration configuration;

	/**
	 * Init options from InitParameter in web.xml Generate ocelot-services.js and ocelot-services-min.js Generate ocelot-core-min.js
	 *
	 * @param sce
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.debug("Context initialisation...");
		ServletContext sc = sce.getServletContext();
		String stacktrace = sc.getInitParameter(Constants.Options.STACKTRACE);
		if (stacktrace == null) {
			stacktrace = DEFAULTSTACKTRACE;
		}
		int stacktracelenght = Integer.parseInt(stacktrace);
		logger.debug("Read stacktracedeep option in web.xml '{}' = {}.", Constants.Options.STACKTRACE, stacktracelenght);
		configuration.setStacktracelength(stacktracelenght);
		try {
			// create tmp/ocelot.js
			File file = createOcelotJsFile(sc.getContextPath());
			setInitParameterAnMinifyJs(sc, file, Constants.OCELOT, Constants.OCELOT_MIN);
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.js.", ex);
		}
	}

	/**
	 * Event context destroyed
	 *
	 * @param sce
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		deleteFile(servletContext.getInitParameter(Constants.OCELOT));
		deleteFile(servletContext.getInitParameter(Constants.OCELOT_MIN));
	}

	/**
	 * Remove file on undeploy application
	 *
	 * @param filename
	 */
	private void deleteFile(String filename) {
		if (null != filename && !filename.isEmpty()) {
			File file = new File(filename);
			if (file.exists()) {
				boolean deleted = file.delete();
				if(!deleted) {
					logger.warn("Fail to delete "+filename);
				}
			}
		}
	}

	/**
	 * Set js name as an init-parameter on ServletContext,<br>
	 * Minify it and set the minify name as init-parameter too.
	 *
	 * @param sc
	 * @param file
	 * @param normalName
	 * @param minifyName
	 */
	private void setInitParameterAnMinifyJs(ServletContext sc, File file, String normalName, String minifyName) {
		// create tmp/ocelot-xxx.js
		String filePath = file.getAbsolutePath();
		sc.setInitParameter(normalName, filePath);
		logger.debug("Generate '{}' : '{}'.", normalName, filePath);
		try {
			// create tmp/ocelot-xxx-min.js
			file = minifyJs(filePath, minifyName);
			filePath = file.getAbsolutePath();
		} catch (Exception ex) {
			logger.error("Minification from " + normalName + " to " + minifyName + " failed. minify version will be equals to normal version.");
		}
		if (filePath != null) {
			sc.setInitParameter(minifyName, filePath);
			logger.debug("Generate '{}' : '{}'.", minifyName, filePath);
		}

	}

	/**
	 * Minify javascriptFile
	 *
	 * @param filename
	 * @param prefix
	 * @return
	 * @throws IOException
	 */
	private File minifyJs(String filename, String prefix) throws IOException {
		File minifiedFile = File.createTempFile(prefix, Constants.JS);
		try (Reader reader = new FileReader(filename)) {
			JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new CompressErrorReporter(filename));
			try (Writer writer = new FileWriter(minifiedFile)) {
				//						  writer, linebreak, obfusc, verbose, preserve '{', disableOptimization
				compressor.compress(writer, LINEBREAK, false, true, true, true);
			}
		}
		return minifiedFile;
	}

	/**
	 * Create ocelot-services.js from the concatenation of all services available from all modules
	 *
	 * @return
	 * @throws IOException
	 */
	private File createOcelotJsFile(String ctxPath) throws IOException {
		File file = File.createTempFile(Constants.OCELOT, Constants.JS);
		try (Writer writer = new FileWriter(file)) {
			createLicenceComment(writer);
			for (IServicesProvider servicesProvider : servicesProviders) {
				servicesProvider.streamJavascriptServices(writer);
			}
			writeOcelotCoreJsFile(writer, ctxPath);
		}
		return file;
	}

	/**
	 * Write ocelot.js from ocelot-core and x ocelot-services.js files and replace contextpath token
	 *
	 * @param writer
	 * @param ctxPath
	 * @return
	 * @throws IOException
	 */
	private void writeOcelotCoreJsFile(Writer writer, String ctxPath) throws IOException {
		URL js = this.getClass().getResource(Constants.SLASH + Constants.OCELOT_CORE + Constants.JS);
		if(null==js) {
			throw new IOException("File " + Constants.SLASH + Constants.OCELOT_CORE + Constants.JS + " not found in classpath.");
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(js.openStream(), Constants.UTF_8))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				writer.write(inputLine.replaceAll(Constants.CTXPATH, ctxPath));
				writer.write(Constants.BACKSLASH_N);
			}
		}
	}

	/**
	 * Add MPL 2.0 License
	 *
	 * @param out
	 */
	private void createLicenceComment(Writer writer) {
		try {
			writer.write("'use strict';\n");
			writer.write("/* This Source Code Form is subject to the terms of the Mozilla Public\n");
			writer.write(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n");
			writer.write(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n");
			writer.write(" * Classes generated by Ocelot Framework.\n");
			writer.write(" */\n");
		} catch (IOException ioe) {
		}
	}
}
