/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.ocelotds.frameworks.angularjs.BodyWriter;
import org.ocelotds.processors.OcelotProcessor;

/**
 *
 * @author hhfrancois
 */
public class FileWriterServices {
	
	final private Messager messager;
	final private Filer filer;
	BodyWriter bodyWriter;
	
	public FileWriterServices(Messager messager, Filer filer) {
		this.messager = messager;
		this.filer = filer;
		this.bodyWriter = new BodyWriter();
	}

	/**
	 * Copy path/filename, from jar ocelot-processor in classes directory of current project/module
	 * @param path
	 * @param filename 
	 */
	public void copyFileToClassesOutput(String path, String filename) {
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " javascript copy js : " + path + File.separatorChar + filename + " to : class dir");
		try (Writer writer = getFileObjectWriterInClassOutput("", filename)) {
			bodyWriter.write(writer, OcelotProcessor.class.getResourceAsStream(path + File.separatorChar + filename));
		} catch (IOException ex) {
			messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " FAILED TO CREATE : " + path + File.separatorChar + filename + " : " + ex.getMessage());
		}
	}
	
	/**
	 * copy path/filename in dir
	 * @param path
	 * @param filename
	 * @param dir 
	 */
	public void copyFileToDir(String path, String filename, String dir) {
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " javascript copy js : " + path + File.separatorChar + filename + " to : "+dir);
		try (Writer writer = getFileObjectWriter(dir, "org.ocelotds."+filename)) {
			bodyWriter.write(writer, OcelotProcessor.class.getResourceAsStream(path + File.separatorChar + filename));
		} catch (IOException ex) {
			messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, " FAILED TO CREATE : " + filename);
		}
	}

	/**
	 * Create writer for write a/b/name file if pkg = a.b
	 * this in classoutput directory
	 * @param pkg
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public Writer getFileObjectWriterInClassOutput(String pkg, String name) throws IOException {
		FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, pkg, name);
		return resource.openWriter();
	}

	/**
	 * Create writer from file path/filename
	 *
	 * @param path
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public Writer getFileObjectWriter(String path, String filename) throws IOException {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new FileWriter(new File(dir, filename));
	}
}
