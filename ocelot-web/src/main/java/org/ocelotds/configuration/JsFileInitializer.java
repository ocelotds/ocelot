/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.ServiceProvider;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
public class JsFileInitializer extends AbstractFileInitializer {
	String OCELOT_CORE_RESOURCE = Constants.SLASH + Constants.OCELOT_CORE + Constants.JS;

	@Inject
	@OcelotLogger
	private Logger logger;

	@Any
	@Inject
	@ServiceProvider(Constants.Provider.JAVASCRIPT)
	private Instance<IServicesProvider> jsServicesProviders;

	public void initOcelotJsFile(@Observes @Initialized(ApplicationScoped.class) ServletContext sc) {
		logger.info("ocelot.js generation...");
		try {
			// create tmp/ocelot.js
			File jsFile = createOcelotJsFile(sc.getContextPath(), getWSProtocol(sc));
			setInitParameterAnMinifyJs(sc, jsFile);
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.js.", ex);
		}
	}

	public void deleteJsFile(@Observes @Destroyed(ApplicationScoped.class) ServletContext sc) {
		deleteFile(sc.getInitParameter(Constants.OCELOT));
		deleteFile(sc.getInitParameter(Constants.OCELOT_MIN));
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
	 * Set js name as an init-parameter on ServletContext,<br>
	 * Minify it and set the minify name as init-parameter too.
	 *
	 * @param sc
	 * @param file
	 * @param normalName
	 * @param minifyName
	 */
	void setInitParameterAnMinifyJs(ServletContext sc, File file) {
		// create tmp/ocelot-xxx.js
		String filePath = file.getAbsolutePath();
		sc.setInitParameter(Constants.OCELOT, filePath);
		logger.debug("Generate '{}' : '{}'.", Constants.OCELOT, filePath);
		try {
			// create tmp/ocelot-xxx-min.js
			file = minifyJs(filePath, Constants.OCELOT_MIN);
			filePath = file.getAbsolutePath();
		} catch (Exception ex) {
			logger.error("Minification from " + Constants.OCELOT + " to " + Constants.OCELOT_MIN + " failed. minify version will be equals to normal version.");
		}
		if (filePath != null) {
			sc.setInitParameter(Constants.OCELOT_MIN, filePath);
			logger.debug("Generate '{}' : '{}'.", Constants.OCELOT_MIN, filePath);
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
}
