angular.module('todoApp', []).controller('TodoListController', 
['$scope', function ($scope) {
//		todoList.todos = [{text:'learn angular', done:true}, {text:'build an angular app', done:false}];
	var todoList = this;
	todoList.todos = [];
	var todoServices = new TodoServices();
	todoList.addTodo = function () {
		var token = todoServices.addTodo(todoList.todoText);
		token.success = function (todo) {
			$scope.$apply(function () {
				todoList.todos.push(todo);
				todoList.todoText = '';
			});
		};
		token.fail = onFault;
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
		token.success = function (todos) {
			$scope.$apply(function () {
				todoList.todos = todos;
        });
		};
		token.fail = onFault;
	};
	todoList.update = function (todo) {
		var token = todoServices.updateTodo(todo);
		token.success = function (todo) {
		};
		token.fail = onFault;
	};
	todoList.refresh = function () {
		var token = todoServices.getTodos();
		token.success = function (todos) {
			$scope.$apply(function () {
				todoList.todos = todos;
        });
		};
		token.fail = onFault;
	};
	var onFault = function (fault) {
		alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
	};
	$scope.init = function() {
		setTimeout(todoList.refresh, 400);
	};
}]);
