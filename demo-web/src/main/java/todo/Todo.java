package todo;

import java.util.Objects;

/**
 *
 * @author hhfrancois
 */
public class Todo {

	private String text;
	private boolean done = false;

	public Todo() {
		
	}
	public Todo(String text, boolean done) {
		this.text = text;
		this.done = done;
	}

	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + Objects.hashCode(this.text);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Todo other = (Todo) obj;
		if (!Objects.equals(this.text, other.text)) {
			return false;
		}
		return true;
	}
	
	

}
