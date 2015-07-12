ocelotController.addOpenEventListener(function () {
	var canvas, context, pencil, 
	mdb = new TopicConsumer("eventCanvas"), 
	drawing = false,
	drawingServices = new DrawingServices();
	// The drawing pencil.
	pencil = {
		mousedown: function (ev) {
			context.beginPath();
			context.moveTo(ev.x, ev.y);
			drawing = true;
		},
		mousemove: function (ev) {
			if (drawing) {
				context.lineTo(ev.x, ev.y);
				context.stroke();
			}
		},
		mouseup: function (ev) {
			if (drawing) {
				this.mousemove(ev);
				drawing = false;
			}
		}
	};
	// Get the 2D canvas context.
	canvas = document.getElementById('imageView');
	if (!canvas || !canvas.getContext) {
		alert('Error: Canvas element or Context not accessible!');
		return;
	}
	context = canvas.getContext('2d');
	// Attach mouse event listeners.
	canvas.addEventListener('mousedown', function (ev) {
//		pencil[ev.type]({"x": ev.layerX, "y": ev.layerY});
		drawingServices.pushCanvasEvent(ev.layerX, ev.layerY, ev.type);
	}, false);
	canvas.addEventListener('mousemove', function (ev) {
		if (drawing) {
//			pencil[ev.type]({"x": ev.layerX, "y": ev.layerY});
			drawingServices.pushCanvasEvent(ev.layerX, ev.layerY, ev.type);
		}
	}, false);
	canvas.addEventListener('mouseup', function (ev) {
		if (drawing) {
//			pencil[ev.type]({"x": ev.layerX, "y": ev.layerY});
			drawingServices.pushCanvasEvent(ev.layerX, ev.layerY, ev.type);
		}
	}, false);
	// Subscribe MDB
	mdb.onMessage = function (evt) {
		pencil[evt.type](evt);
	};
	mdb.subscribe();
});


