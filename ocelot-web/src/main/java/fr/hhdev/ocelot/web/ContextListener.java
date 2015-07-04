/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.web;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.IServicesProvider;
import fr.hhdev.ocelot.configuration.OcelotConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
public class ContextListener implements ServletContextListener {

	private final static Logger logger = LoggerFactory.getLogger(ContextListener.class);
	/**
	 * This string will be replaced by the contextPath in ocelot-core.js
	 */
	private static final String CTXPATH = "%CTXPATH%";
	/**
	 * Minify option, breakline
	 */
	private static final int LINEBREAK = 80;
	/**
	 * Default size of stacktrace include in messageToClient fault
	 */
	private static final String DEFAULTSTACKTRACE = "50";

	/**
	 * For the moment, minification doesn't work, so it's disabled
	 */
	private static final boolean MINIFY = false;

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
			// create tmp/ocelot-services.js
			File file = createOcelotServicesJsFile();
			String filePath = file.getAbsolutePath();
			sc.setInitParameter(Constants.OCELOT_SERVICES, filePath);
			logger.debug("Generate '{}' : '{}'.", Constants.OCELOT_SERVICES, filePath);
			// create tmp/ocelot-services-min.js
			if(MINIFY) {
				file = minifyJs(filePath, Constants.OCELOT_SERVICES_MIN);
			}
			filePath = file.getAbsolutePath();
			sc.setInitParameter(Constants.OCELOT_SERVICES_MIN, filePath);
			logger.debug("Generate '{}' : '{}'.", Constants.OCELOT_SERVICES_MIN, filePath);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			// create tmp/ocelot-core.js
			File file = createOcelotCoreJsFile(sce.getServletContext().getContextPath());
			String filePath = file.getAbsolutePath();
			sc.setInitParameter(Constants.OCELOT_CORE, filePath);
			logger.debug("Generate '{}' : '{}'.", Constants.OCELOT_CORE, filePath);
			// create tmp/ocelot-core-min.js
			if(MINIFY) {
				file = minifyJs(filePath, Constants.OCELOT_CORE_MIN);
			}
			filePath = file.getAbsolutePath();
			sc.setInitParameter(Constants.OCELOT_CORE_MIN, filePath);
			logger.debug("Generate '{}' : '{}'.", Constants.OCELOT_CORE_MIN, filePath);
		} catch (IOException ex) {
			ex.printStackTrace();
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
		deleteFile(servletContext.getInitParameter(Constants.OCELOT_SERVICES));
		deleteFile(servletContext.getInitParameter(Constants.OCELOT_SERVICES_MIN));
		deleteFile(servletContext.getInitParameter(Constants.OCELOT_CORE));
		deleteFile(servletContext.getInitParameter(Constants.OCELOT_CORE_MIN));
	}

	/**
	 * Remove file on undeploy application
	 *
	 * @param filename
	 */
	public void deleteFile(String filename) {
		if (!filename.isEmpty()) {
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
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
	protected File minifyJs(String filename, String prefix) throws IOException {
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
	 * Create ocelot-core.js from original file and replace contextpath token
	 *
	 * @param ctxPath
	 * @return
	 * @throws IOException
	 */
	private File createOcelotCoreJsFile(String ctxPath) throws IOException {
		URL js = this.getClass().getResource(Constants.SLASH + Constants.OCELOT_CORE + Constants.JS);
		File file = File.createTempFile(Constants.OCELOT_CORE, Constants.JS);
		try (Writer writer = new FileWriter(file)) {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(js.openStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					writer.write(inputLine.replaceAll(CTXPATH, ctxPath));
					writer.write(Constants.BACKSLASH_N);
				}
			}
		}
		return file;
	}

	/**
	 * Create ocelot-services.js from the concatenation of all services available from all modules
	 *
	 * @return
	 * @throws IOException
	 */
	private File createOcelotServicesJsFile() throws IOException {
		File file = File.createTempFile(Constants.OCELOT_SERVICES, Constants.JS);
		try (FileOutputStream out = new FileOutputStream(file)) {
			createLicenceComment(out);
			for (IServicesProvider servicesProvider : servicesProviders) {
				logger.debug("Find javascript services provider : '{}'", servicesProvider.getClass().getName());
				servicesProvider.streamJavascriptServices(out);
			}
		}
		return file;
	}

	/**
	 * Add MPL 2.0 License
	 *
	 * @param out
	 */
	private void createLicenceComment(OutputStream out) {
		try {
			out.write("/* This Source Code Form is subject to the terms of the Mozilla Public\n".getBytes());
			out.write(" * License, v. 2.0. If a copy of the MPL was not distributed with this\n".getBytes());
			out.write(" * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n".getBytes());
			out.write(" * Classes generated by Ocelot Framework.\n".getBytes());
			out.write(" */\n".getBytes());
		} catch (IOException ioe) {
		}
	}

}
