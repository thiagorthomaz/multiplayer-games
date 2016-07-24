var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);


server.listen(8080, function(){
	console.log("Server is now running...");
});


//Accept new client's connections
io.on('connection', function(socket){
	console.log('Player Connected. ID: ' + socket.id);


    //Send a ID for the client connected
    socket.emit('socketID', {id:socket.id});

    //Tell to other sockets that this one is now connected
    socket.broadcast.emit("newPlayer", {id:socket.id});

    //Disconnect client
	socket.on('disconnect', function(){
		console.log('Player Disconnected. ID: ' + socket.id);

		//Tell to other sockets that this one is now disconnected
		socket.broadcast.emit("disconnectedPlayer", {id:socket.id});
	});
});