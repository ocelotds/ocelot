/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.configuration.OcelotConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.ocelotds.logger.OcelotLogger;
import org.slf4j.Logger;

/**
 * Web application lifecycle listener.
 *
 * @author hhfrancois
 */
@WebListener
public final class ContextListener implements ServletContextListener {

	@Inject
	@OcelotLogger
	private Logger logger;
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
		defineStacktraceConfig(sc);
		try {
			// create tmp/ocelot.js
			File file = createOcelotJsFile(sc.getContextPath());
			setInitParameterAnMinifyJs(sc, file, Constants.OCELOT, Constants.OCELOT_MIN);
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.js.", ex);
		}
	}

	/**
	 * Read in web.xml the optional STACKTRACE_LENGTH config and set it in OcelotConfiguration
	 * @param sc 
	 */
	void defineStacktraceConfig(ServletContext sc) {
		String stacktrace = sc.getInitParameter(Constants.Options.STACKTRACE_LENGTH);
		if (stacktrace == null) {
			stacktrace = DEFAULTSTACKTRACE;
		} else {
			logger.debug("Read '{}' option in web.xml : '{}'.", Constants.Options.STACKTRACE_LENGTH, stacktrace);
		}
		int stacktracelenght = Integer.parseInt(stacktrace);
		logger.debug("'{}' value : '{}'.", Constants.Options.STACKTRACE_LENGTH, stacktracelenght);
		configuration.setStacktracelength(stacktracelenght);
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
	void deleteFile(String filename) {
		if (null != filename && !filename.isEmpty()) {
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
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
	void setInitParameterAnMinifyJs(ServletContext sc, File file, String normalName, String minifyName) {
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
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			StringBuilder buffer = new StringBuilder();
			while((line = reader.readLine())!=null) {
				// on pourrait supprimer les espaces supperflus, mais il faut tenir compte de : function_, return_, var_, _in_, _new_, else_if, delete_, get_, set_
				line = line.replaceAll("^\\s*", "") // remplace tous les espace en debut de ligne
						  .replaceAll("^//.*", "") // supprime les lignes en commentaire
						  .replaceAll("\\s+", " ") // tous les x espaces = 1 espace
						  .replaceAll("\\s?([\\W])\\s?", "$1") // supprime les espace autour des operateur binaire 
						  .replaceAll("([;{])\\s*//.*", "$1"); // supprime les commentaire en fin de ligne
				if(line.isEmpty()) {
				} else {
					buffer.append(line);
				}
			}
			try (Writer writer = new FileWriter(minifiedFile)) {
				writer.write(buffer.toString().replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", ""));
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
	File createOcelotJsFile(String ctxPath) throws IOException {
		File file = File.createTempFile(Constants.OCELOT, Constants.JS);
		try (OutputStream out = new FileOutputStream(file)) {
			createLicenceComment(out);
			for (IServicesProvider servicesProvider : servicesProviders) {
				servicesProvider.streamJavascriptServices(out);
			}
			writeOcelotCoreJsFile(out, ctxPath);
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
	private void writeOcelotCoreJsFile(OutputStream out, String ctxPath) throws IOException {
		URL js = this.getClass().getResource(Constants.SLASH + Constants.OCELOT_CORE + Constants.JS);
		if (null == js) {
			throw new IOException("File " + Constants.SLASH + Constants.OCELOT_CORE + Constants.JS + " not found in classpath.");
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(js.openStream(), Constants.UTF_8))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				out.write(inputLine.replaceAll(Constants.CTXPATH, ctxPath).getBytes(Constants.UTF_8));
				out.write(Constants.BACKSLASH_N.getBytes(Constants.UTF_8));
			}
		}
	}

	/**
	 * Add MPL 2.0 License
	 *
	 * @param out
	 */
	private void createLicenceComment(OutputStream out) {
		try {
			out.write("'use strict';\n".getBytes("UTF-8"));
			out.write("/* This Source Code Form is subject to the terms of the Mozilla Public\n".getBytes("UTF-8"));
			out.write(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n".getBytes("UTF-8"));
			out.write(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n".getBytes("UTF-8"));
			out.write(" * Javascript file generated by Ocelotds Framework.\n".getBytes("UTF-8"));
			out.write(" */\n".getBytes("UTF-8"));
		} catch (IOException ioe) {
		}
	}
}
