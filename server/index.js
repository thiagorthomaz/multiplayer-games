var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = new Array();


server.listen(8080, function(){
	console.log("Server is now running...");
});


//Accept new client's connections
io.on('connection', function(socket){
	console.log('Player Connected. ID: ' + socket.id);


    //Send a ID for the client connected
    socket.emit('socketID', {id:socket.id});

    //Send all the players to others clients
    socket.emit('getPlayers', players);

    //Tell to other sockets that this one is now connected
    socket.broadcast.emit("newPlayer", {id:socket.id});

	socket.on('playerMoved', function(data){

		console.log('Player moved. ID: ' + socket.id + " X:" + data.x + " Y:" + data.y);

		data.id = socket.id;
        socket.broadcast.emit("playerMoved", data);
        for (var i in players) {
            var p = players[i];
            if (p.id == socket.id) {
                p = players[i].x = data.x;
                p = players[i].y = data.y;
            }
        }

	});

    //Disconnect client
	socket.on('disconnect', function(){
		console.log('Player Disconnected. ID: ' + socket.id);

        for (var i in players) {
            var p = players[i];
            if (p.id == socket.id) {
                players.splice(i, 1);
            }
        }

		//Tell to other sockets that this one is now disconnected
		socket.broadcast.emit("playerDisconnected", {id:socket.id});
	});

	players.push(new player(socket.id,0,0));

});


//Container with the information that is needed to pass between the clients
function player(id, x, y){
    this.id = id;
    this.x = x;
    this.y = y;
}