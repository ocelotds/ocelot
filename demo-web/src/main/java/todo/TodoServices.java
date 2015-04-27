/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Stateless;

/**
 *
 * @author hhfrancois
 */
@Stateless
@Singleton
@DataService(resolverid = Constants.Resolver.EJB)
public class TodoServices {
	
	private List<Todo> todos;

	@PostConstruct
	private void init() {
		todos = new ArrayList<>();
		todos.add(new Todo("learn angular", true));
		todos.add(new Todo("build an angular app", false));
	}

	public List<Todo> getTodos() {
		return todos;
	}
	
	public Todo addTodo(String text) {
		Todo todo = new Todo(text, false);
		todos.add(todo);
		return todo;
	}

	public Todo updateTodo(Todo todo) {
		todos.remove(todo);
		todos.add(todo);
		return todo;
	}
}
