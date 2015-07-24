function TicketsViewModel() {
	var ticketServices = new TicketServices();
	var model = this;
	model.tickets = ko.observableArray([]);
	model.chosenTicket = ko.observable();
	model.resetTicket = function () {
		this.chosenTicket(null);
	};
	var onFault = function (fault) {
		alert(fault.message + "\n" + fault.classname + "\n" + fault.stacktrace.join('\n'));
	};
	model.loadTickets = function () {
		ticketServices.getTickets().then(function (tickets) {
			model.tickets.remove(function(item) { return true;});
			for (var key in tickets){
				model.tickets.push(tickets[key]);
			}
		}).catch(onFault);
	};
	model.loadTickets();
}
ocelotController.addOpenListener(function (event) {
	ko.applyBindings(new TicketsViewModel(), document.getElementById("liveExample"));
});
