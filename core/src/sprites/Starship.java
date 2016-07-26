package sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by thiago on 24/07/16.
 */
public class Starship extends Sprite {

    Vector2 previsousPosition;

    public Starship(Texture texture) {
        super(texture);
        this.previsousPosition = new Vector2(getX(), getY());


    }

    public boolean hasMoved(){
        if (this.previsousPosition.x != getX() || this.previsousPosition.y != getY()){
            this.previsousPosition.x = getX();
            this.previsousPosition.y = getY();
            return true;
        }
        return false;
    }

}
