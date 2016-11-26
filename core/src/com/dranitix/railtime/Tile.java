package com.dranitix.railtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/**
 * Created by Kenta Iwasaki on 11/26/2016.
 */
public class Tile extends Actor {
    int index;
    TextureRegion region;

    public Tile(int index, TextureRegion region) {
        this.index = index;
        this.region = region;

        setTouchable(!(index >= 54 && index <= 83) ? Touchable.disabled : Touchable.enabled);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}
