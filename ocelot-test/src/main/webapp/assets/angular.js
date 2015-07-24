angular.module('todoApp', []).controller('TodoListController', 
['$scope', function ($scope) {
//		todoList.todos = [{text:'learn angular', done:true}, {text:'build an angular app', done:false}];
	var todoList = this;
	todoList.todos = [];
	var todoServices = new TodoServices();
	todoList.addTodo = function () {
		todoServices.addTodo(todoList.todoText).then(function (todo) {
			$scope.$apply(function () {
				todoList.todos.push(todo);
				todoList.todoText = '';
			});
		}).catch(onFault);
	};
	todoList.remaining = function () {
		var count = 0;
		angular.forEach(todoList.todos, function (todo) {
			count += todo.done ? 0 : 1;
		});
		return count;
	};
	todoList.archive = function () {
		todoServices.archive().then(function (todos) {
			$scope.$apply(function () {
				todoList.todos = todos;
        });
		}).catch(onFault);
	};
	todoList.update = function (todo) {
		todoServices.updateTodo(todo).then(function (todo) {
		}).catch(onFault);
	};
	todoList.refresh = function () {
		todoServices.getTodos().then(function (todos) {
			$scope.$apply(function () {
				todoList.todos = todos;
        });
		}).catch(onFault);
	};
	var onFault = function (fault) {
		alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
	};
	$scope.init = function() {
		setTimeout(todoList.refresh, 400);
	};
}]);
