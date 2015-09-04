ocelotController.addOpenListener(function () {
   new Subscriber("ocelot-status").message(function (msg) {
      var statusText = document.getElementById("status-text");
      var statusColor = document.getElementById("status-color");
      var statusTextShadow = document.getElementById("status-text-shadow");
      var color = "#97CA00";
      var status = "opened";
      if (msg !== "OPEN") {
         status = "closed";
         color = "#e05d44";
      }
      statusColor.setAttribute("fill", color);
      statusText.innerHTML = status;
      statusTextShadow.innerHTML = status;
   });
});


