package todo;

import fr.hhdev.ocelot.Constants;
import fr.hhdev.ocelot.annotations.DataService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 *
 * @author hhfrancois
 */
@DataService(resolverid = Constants.Resolver.CDI)
@Default
@Singleton
public class TodoServices {
	private static TodoServices instance = null;
	
	private List<Todo> todos;
	
	public synchronized static TodoServices getInstance() {
		if(instance==null) {
			instance = new TodoServices();
			instance.init();
		}
		return instance;
	}
	public TodoServices() {
	}
//	private TodoServices() {
//		todos = new ArrayList<>();
//		todos.add(new Todo("learn angular", true));
//		todos.add(new Todo("build an angular app", false));
//	}
	
	@PostConstruct
	protected void init() {
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
