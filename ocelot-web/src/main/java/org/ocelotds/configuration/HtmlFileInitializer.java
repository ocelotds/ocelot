/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
public class HtmlFileInitializer extends AbstractFileInitializer {
	@Any
	@Inject
	@ServiceProvider(Constants.Provider.HTML)
	private Instance<IServicesProvider> htmlServicesProviders;
	
	@Inject
	@OcelotLogger
	private Logger logger;

	public void initHtmlFile(@Observes @Initialized(ApplicationScoped.class) ServletContext sc) {
		logger.info("ocelot.htm generation...");
		try {
			// create tmp/ocelot.html
			File htmlFile = createOcelotHtmlFile(sc.getContextPath());
			sc.setInitParameter(Constants.OCELOT_HTML, htmlFile.getAbsolutePath());
		} catch (IOException ex) {
			logger.error("Fail to create ocelot.html.", ex);
		}
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
			writeHtmlHeaders(out, ctxPath);
			for (IServicesProvider servicesProvider : htmlServicesProviders) {
				servicesProvider.streamJavascriptServices(out);
			}
			writeHtmlFooter(out);
		}
		return file;
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
