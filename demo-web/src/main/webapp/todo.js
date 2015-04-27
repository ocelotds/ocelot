angular.module('todoApp', []).controller('TodoListController', function () {
	var todoServices = new TodoServices();
	var todoList = this;
	todoList.todos = [];

	todoList.addTodo = function () {
		var token = todoServices.addTodo(todoList.todoText);
		token.onResult = function (todo) {
			todoList.todos.push(todo);
			todoList.todoText = '';
		};
		token.onFault = function (fault) {
			alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
		};
	};

	todoList.remaining = function () {
		var count = 0;
		angular.forEach(todoList.todos, function (todo) {
			count += todo.done ? 0 : 1;
		});
		return count;
	};

	todoList.archive = function () {
		var oldTodos = todoList.todos;
		todoList.todos = [];
		angular.forEach(oldTodos, function (todo) {
			if (!todo.done)
				todoList.todos.push(todo);
		});
	};
	todoList.refresh = function () {
		var token = todoServices.getTodos();
		token.onResult = function (todos) {
			todoList.todos = todos;
		};
		token.onFault = function (fault) {
			alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
		};
	};
});
