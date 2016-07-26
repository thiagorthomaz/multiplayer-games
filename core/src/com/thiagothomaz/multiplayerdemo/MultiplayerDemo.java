package com.thiagothomaz.multiplayerdemo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import sprites.Starship;

public class MultiplayerDemo extends ApplicationAdapter {


	SpriteBatch batch;
	private Socket socket;
    Starship player;
    Texture playerShip;
    Texture friendlyShip;
    HashMap<String, Starship> friendlyPlayers;

    //How often the server will update the players movement.
    private final float UPDATE_TIME = 1/60f; //6 time a second
    float time;

	@Override
	public void create () {
		batch = new SpriteBatch();

        this.playerShip = new Texture("playerShip2.png");
        this.friendlyShip = new Texture("playerShip.png");
        this.friendlyPlayers = new HashMap<String, Starship>();

		connectSocket();
        configSocketEvents();

	}

    public void configSocketEvents() {
        this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
                player = new Starship(playerShip);

            }
        }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                try {
                    JSONObject data = (JSONObject) args[0];
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "My id is: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting ID");
                }

            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String playerId = data.getString("id");
                    Gdx.app.log("SocketIO", "New player connected: " + playerId);
                    friendlyPlayers.put(playerId, new Starship(friendlyShip));
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting new player ID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "Player disconnected: " + id);
                    friendlyPlayers.remove(id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting player disconnected ID");
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray objects = (JSONArray) args[0];

                    for (int i = 0; i<objects.length(); i++) {
                        Starship coopPlayer = new Starship(friendlyShip);
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        coopPlayer.setPosition(position.x, position.y);
                        friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
                    }


                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting players");
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {

                    String playerId = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Starship friendlyShip = friendlyPlayers.get(playerId);
                    if (friendlyShip != null) {
                        friendlyShip.setPosition(x.floatValue(), y.floatValue());
                    }


                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting players");
                }
            }
        });

    }

    public void connectSocket() {
		try{

			this.socket = IO.socket("http://localhost:8080");
			this.socket.connect();


		}catch (Exception e) {
			System.out.println(e);

		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.handleInput(Gdx.graphics.getDeltaTime());
        this.updateServer(Gdx.graphics.getDeltaTime());
		batch.begin();

        if (this.player != null) {
            this.player.draw(batch);
        }

        for (HashMap.Entry<String, Starship> entry : this.friendlyPlayers.entrySet()) {
            entry.getValue().draw(batch);
        }

		batch.end();
	}

    public void handleInput(float deltaTime) {

        if (player != null) {

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                player.setPosition(player.getX() + (-200*deltaTime), player.getY());
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.setPosition(player.getX() + (+200*deltaTime), player.getY());
            }

        }
    }

    public void updateServer(float dt) {
        this.time += dt;
        if (this.time >= this.UPDATE_TIME && this.player != null && this.player.hasMoved()) {

            JSONObject data = new JSONObject();
            try {

                data.put("x", this.player.getX());
                data.put("y", this.player.getY());
                this.socket.emit("playerMoved", data);

            } catch (JSONException e) {
                Gdx.app.log("SocketIO", "Error while was sending update data");
            }


        }


    }

    @Override
	public void dispose () {
        super.dispose();
		batch.dispose();
        playerShip.dispose();
        friendlyShip.dispose();

	}
}
