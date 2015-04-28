angular.module('todoApp', []).controller('TodoListController', 
['$scope', function ($scope) {
//		todoList.todos = [{text:'learn angular', done:true}, {text:'build an angular app', done:false}];
	var todoList = this;
	todoList.todos = [];
	var todoServices = new TodoServices();
	todoList.addTodo = function () {
		var token = todoServices.addTodo(todoList.todoText);
		token.onResult = function (todo) {
			$scope.$apply(function () {
				todoList.todos.push(todo);
				todoList.todoText = '';
			});
		};
		token.onFault = onFault;
	};
	todoList.remaining = function () {
		var count = 0;
		angular.forEach(todoList.todos, function (todo) {
			count += todo.done ? 0 : 1;
		});
		return count;
	};
	todoList.archive = function () {
		var token = todoServices.archive();
		token.onResult = function (todos) {
			$scope.$apply(function () {
				todoList.todos = todos;
        });
		};
		token.onFault = onFault;
	};
	todoList.update = function (todo) {
		delete todo["$$hashKey"];
		var token = todoServices.updateTodo(todo);
		token.onResult = function (todo) {
		};
		token.onFault = onFault;
	};
	todoList.refresh = function () {
		var token = todoServices.getTodos();
		token.onResult = function (todos) {
			$scope.$apply(function () {
				todoList.todos = todos;
        });
		};
		token.onFault = onFault;
	};
	var onFault = function (fault) {
		alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
	};
	$scope.init = function() {
		setTimeout(todoList.refresh, 400);
	};
}]);
