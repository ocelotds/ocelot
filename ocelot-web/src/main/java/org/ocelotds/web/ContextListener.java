/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.configuration.OcelotConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.ocelotds.annotations.ServiceProvider;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.security.SecurityServices;
import org.slf4j.Logger;

/**
 * Web application lifecycle listener.
 *
 * @author hhfrancois
 */
@WebListener
class ContextListener implements ServletContextListener {
	
	String OCELOT_CORE_RESOURCE = Constants.SLASH + Constants.OCELOT_CORE + Constants.JS;

	@Inject
	@OcelotLogger
	private Logger logger;
	/**
	 * Default size of stacktrace include in messageToClient fault
	 */
	private static final String DEFAULTSTACKTRACE = "50";

	@Any
	@Inject
	@ServiceProvider(Constants.Provider.JAVASCRIPT)
	private Instance<IServicesProvider> jsServicesProviders;

	@Any
	@Inject
	@ServiceProvider(Constants.Provider.HTML)
	private Instance<IServicesProvider> htmlServicesProviders;

	@Inject
	private OcelotConfiguration configuration;
	
	@Inject 
	private SecurityServices subjectServices;

	/**
	 * Init options from InitParameter in web.xml Generate ocelot-services.js and ocelot-services-min.js Generate ocelot-core-min.js
	 *
	 * @param sce
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		String serverInfo = sc.getServerInfo();
		logger.debug("Context '{}' initialisation...", serverInfo);
		subjectServices.setServerInfo(serverInfo);
		defineStacktraceConfig(sc);
		createJsFile(sc);
		createHtmlFile(sc);
	}
	
	void createJsFile(ServletContext sc) {
		try {
			// create tmp/ocelot.js
			File jsFile = createOcelotJsFile(sc.getContextPath(), getWSProtocol(sc));
			setInitParameterAnMinifyJs(sc, jsFile, Constants.OCELOT, Constants.OCELOT_MIN);
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.js.", ex);
		}
	}
	
	void createHtmlFile(ServletContext sc) {
		try {
			// create tmp/ocelot.html
			File htmlFile = createOcelotHtmlFile(sc.getContextPath());
			sc.setInitParameter(Constants.OCELOT_HTML, htmlFile.getAbsolutePath());
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.html.", ex);
		}
	}

	/**
	 * Return protocol for webscoket, ws(default) or wss
	 * setting ocelot.websocket.secure option in web.xml for change protocol 
	 * @param sc
	 * @return 
	 */
	String getWSProtocol(ServletContext sc) {
		String secure = sc.getInitParameter(Constants.Options.SECURE);
		String protocol = Constants.WS;
		if (Constants.TRUE.equals(secure)) {
			protocol = Constants.WSS;
		}
		return protocol;
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
		deleteFile(servletContext.getInitParameter(Constants.OCELOT_HTML));
	}

	/**
	 * Remove file on undeploy application
	 *
	 * @param filename
	 */
	boolean deleteFile(String filename) {
		if (null != filename && !filename.isEmpty()) {
			File file = new File(filename);
			if (file.exists()) {
				return file.delete();
			}
		}
		return false;
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Constants.UTF_8))) {
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
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(minifiedFile), Constants.UTF_8)) {
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
	File createOcelotJsFile(String ctxPath, String protocol) throws IOException {
		File file = File.createTempFile(Constants.OCELOT, Constants.JS);
		try (OutputStream out = new FileOutputStream(file)) {
			createLicenceComment(out);
			for (IServicesProvider servicesProvider : jsServicesProviders) {
				servicesProvider.streamJavascriptServices(out);
			}
			writeOcelotCoreJsFile(out, ctxPath, protocol);
		}
		return file;
	}

	/**
	 * Create ocelot-services.js from the concatenation of all services available from all modules
	 *
	 * @return
	 * @throws IOException
	 */
	File createOcelotHtmlFile(String ctxPath) throws IOException {
		File file = File.createTempFile(Constants.OCELOT, Constants.HTML);
		try (OutputStream out = new FileOutputStream(file)) {
			writeHtmlHeaders(out, ctxPath);
			for (IServicesProvider servicesProvider : htmlServicesProviders) {
				servicesProvider.streamJavascriptServices(out);
			}
			writeHtmlFooter(out);
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
	void writeOcelotCoreJsFile(OutputStream out, String ctxPath, String protocol) throws IOException {
		URL js = this.getClass().getResource(OCELOT_CORE_RESOURCE);
		if (null == js) {
			throw new IOException("File " + OCELOT_CORE_RESOURCE + " not found in classpath.");
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(js.openStream(), Constants.UTF_8))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				out.write(inputLine.replaceAll(Constants.CTXPATH, ctxPath).replaceAll(Constants.PROTOCOL, protocol).getBytes(Constants.UTF_8));
				out.write(Constants.BACKSLASH_N.getBytes(Constants.UTF_8));
			}
		}
	}

	/**
	 * Add MPL 2.0 License
	 *
	 * @param out
	 * @throws IOException
	 */
	private void createLicenceComment(OutputStream out) throws IOException {
		out.write("'use strict';\n".getBytes(Constants.UTF_8));
		out.write("/* This Source Code Form is subject to the terms of the Mozilla Public\n".getBytes(Constants.UTF_8));
		out.write(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n".getBytes(Constants.UTF_8));
		out.write(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n".getBytes(Constants.UTF_8));
		out.write(" * Javascript file generated by Ocelotds Framework.\n".getBytes(Constants.UTF_8));
		out.write(" */\n".getBytes(Constants.UTF_8));
	}

	void writeHtmlHeaders(OutputStream out, String ctxPath) throws IOException {
		out.write("<!DOCTYPE html>\n".getBytes(Constants.UTF_8));
		out.write("<html>\n".getBytes(Constants.UTF_8));
		out.write("  <head>\n".getBytes(Constants.UTF_8));
		out.write("      <title></title>\n".getBytes(Constants.UTF_8));
		out.write("  </head>\n".getBytes(Constants.UTF_8));
		out.write("  <script src=\"".getBytes(Constants.UTF_8));
		out.write(ctxPath.getBytes(Constants.UTF_8));
		out.write("/ocelot.js?minify=false\" type=\"text/javascript\"></script>\n".getBytes(Constants.UTF_8));
		out.write("  <body>\n".getBytes(Constants.UTF_8));
		out.write("     <script>\n".getBytes(Constants.UTF_8));
		out.write("        function processCall(event) {\n".getBytes(Constants.UTF_8));
		out.write("           var first = true, toexec, classname, methodname, args = [], child, children = event.target.parentNode.childNodes;\n".getBytes(Constants.UTF_8));
		out.write("           classname = event.target.attributes[\"classname\"].value;\n".getBytes(Constants.UTF_8));
		out.write("           methodname = event.target.attributes[\"methodname\"].value;\n".getBytes(Constants.UTF_8));
		out.write("           toexec = \"new \"+classname+\"().\"+methodname+\"(\";\n".getBytes(Constants.UTF_8));
		out.write("           for(var i in children) {\n".getBytes(Constants.UTF_8));
		out.write("              child = children[i];\n".getBytes(Constants.UTF_8));
		out.write("              if(child.nodeName === \"INPUT\") {\n".getBytes(Constants.UTF_8));
		out.write("                 if(first) first = false;\n".getBytes(Constants.UTF_8));
		out.write("                 else toexec += \",\";\n".getBytes(Constants.UTF_8));
		out.write("                 toexec += (child.value)?child.value:\"null\";\n".getBytes(Constants.UTF_8));
		out.write("              }\n".getBytes(Constants.UTF_8));
		out.write("           }\n".getBytes(Constants.UTF_8));
		out.write("           toexec += \").event(function(evt) {alert(JSON.stringify(evt.response));});\";\n".getBytes(Constants.UTF_8));
		out.write("           eval(toexec);\n".getBytes(Constants.UTF_8));
		out.write("        }\n".getBytes(Constants.UTF_8));
		out.write("     </script>\n".getBytes(Constants.UTF_8));
	}
	void writeHtmlFooter(OutputStream out) throws IOException {
		out.write("  </body>\n".getBytes(Constants.UTF_8));
		out.write("</html>\n".getBytes(Constants.UTF_8));
	}
}
