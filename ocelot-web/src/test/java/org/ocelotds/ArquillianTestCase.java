/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.File;
import java.io.FileFilter;
import java.util.Random;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 *
 * @author hhfrancois
 */
public abstract class ArquillianTestCase {
	/**
	 * Build ocelot-web.jar
	 *
	 * @return
	 */
	public static JavaArchive createOcelotWebJar() {
		File bean = new File("src/main/resources/META-INF/beans.xml");
		File core = new File("src/main/resources/ocelot-core.js");
		JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, String.format("ocelot-web-%s.jar", new Random().nextInt(10)))
				  .addClass(OcelotServices.class)
				  .addPackages(true, "org.ocelotds.encoders")
				  .addPackages(true, "org.ocelotds.exceptions")
				  .addPackages(true, "org.ocelotds.resolvers")
				  .addPackages(true, "org.ocelotds.web")
				  .addPackages(true, "org.ocelotds.core")
				  .addPackages(true, "org.ocelotds.configuration")
				  .addAsManifestResource(new FileAsset(bean), "beans.xml")
				  .addAsResource(new FileAsset(core), "ocelot-core2.js");
		addJSAndProvider("target/classes", javaArchive, javaArchive);
		return javaArchive;
	}

	/**
	 * Add srv_xxxx.js and srv_xxxx.ServiceProvider.class
	 *
	 * @param root
	 * @param resourceContainer
	 * @param classContainer
	 */
	public static void addJSAndProvider(final String root, ResourceContainer resourceContainer, ClassContainer classContainer) {
		File classes = new File(root);
		File[] jsFiles = classes.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				return file.isFile() && name.startsWith("srv_") && name.endsWith(".js");
			}
		});
		for (File file : jsFiles) {
			String jsName = file.getName();
			String providerPackage = jsName.replaceAll(".js$", "");
			classContainer.addPackage(providerPackage);
			resourceContainer.addAsResource(new FileAsset(file), file.getName());
		}
	}
	
}
