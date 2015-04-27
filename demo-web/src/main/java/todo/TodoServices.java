package todo;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

/**
 *
 * @author hhfrancois
 */
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
		for (Todo t : todos) {
			if (t.equals(todo)) {
				t.setDone(todo.isDone());
			}
		}
		return todo;
	}
	
	public List<Todo> archive() {
		List<Todo> saved = new ArrayList<>();
		saved.addAll(todos);
		todos.clear();
		for (Todo t : saved) {
			if (!t.isDone()) {
				todos.add(t);
			}
		}
		return todos;
	}
}
