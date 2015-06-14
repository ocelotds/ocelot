/*
 * Copyright 2015 Arnaud Laye.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo;

import demo.controller.MessagePrinter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 * @author Arnaud Laye
 */
public class Launcher {

	public static void main(String[] args) {
		System.out.println("Début du main");
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		MessagePrinter printer = context.getBean(MessagePrinter.class);
		System.out.println(printer.getMessage());
		System.out.println(printer.getMessageFromDAO());
	}
}
