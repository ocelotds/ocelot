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
package demo.controller;

import demo.dao.MessageDAO;
import org.ocelotds.Constants;
import org.ocelotds.annotations.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A simple message printer
 *
 * @author Arnaud Laye
 */
@Component
@DataService(resolver = Constants.Resolver.SPRING)
//@RequestMapping("/welcome")
public class MessagePrinter {

	@Autowired
	private MessageDAO messageDAO;

	public MessagePrinter() {
	}

	public String getMessage() {
		System.out.println("Je suis passé dans la method getMessage");
		return "Hello from MessagePrinter !";
	}

	public String getMessageFromDAO() {
		System.out.println("Je suis passé dans la methode getMessageFromDAO");
		return messageDAO.getMessage();
	}
	
//	@RequestMapping(method = RequestMethod.GET)
//	public ModelAndView helloWorld(){
// 
//		ModelAndView model = new ModelAndView("HelloWorldPage");
//		model.addObject("msg", "hello world");
// 
//		return model;
//	}
}
