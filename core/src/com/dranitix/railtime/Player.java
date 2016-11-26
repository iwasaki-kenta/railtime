package com.dranitix.railtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import net.dermetfan.gdx.utils.ArrayUtils;

/**
 * Created by Kenta Iwasaki on 11/26/2016.
 */
public class Player extends Actor {
    public static final int TOTAL_FRAMES = 15;
    private Animation leftAnim, rightAnim, upAnim, downAnim;
    private Array<Animation> animations;
    private Texture spriteSheet;
    private Array<TextureRegion> frames;

    private float stateTime = 0;
    private int state = 2;
    private boolean moving = false;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Player() {
        setBounds(48, 48, 8, 8);
        setOrigin(8, 8);
        setTouchable(Touchable.disabled);

        spriteSheet = new Texture(Gdx.files.internal("spaceman.png"));
        frames = new Array<TextureRegion>(TextureRegion.split(spriteSheet, 16, 16)[0]);

        animations = new Array<Animation>();
        animations.add(leftAnim = new Animation(0.1f, ArrayUtils.select(frames, new int[]{8, 9}).toArray()));
        animations.add(rightAnim = new Animation(0.1f, ArrayUtils.select(frames, new int[]{1, 2}).toArray()));
        animations.add(upAnim = new Animation(0.1f, ArrayUtils.select(frames, new int[]{11, 12, 13}).toArray()));
        animations.add(downAnim = new Animation(0.1f, ArrayUtils.select(frames, new int[]{4, 5, 6}).toArray()));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        batch.draw(moving ? animations.get(state).getKeyFrame(stateTime, true) : animations.get(state).getKeyFrame(0, true), getX(), getY(), getOriginX(), getOriginY(),
                16, 16, getScaleX(), getScaleY(), getRotation());
    }

    private boolean collide(float x, float y) {
        Actor actor = getStage().hit(x, y, true);
        if (actor instanceof Tile) {
            Tile t = (Tile) actor;
            return true;
        } else if (actor instanceof Crate) {
            actor.remove();
        }
        return false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        int speed = 5;

        stateTime += delta;

        moving = false;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            setState(2);
            if (!collide(getX(), getY() - speed)) {
                moving = true;
                setPosition(getX(), getY() - speed);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            setState(0);
            if (!collide(getX() - speed, getY())) {
                moving = true;
                setPosition(getX() - speed, getY());
            };
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            setState(3);
            if (!collide(getX(), getY() + speed)) {
                moving = true;
                setPosition(getX(), getY() + speed);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            setState(1);
            if (!collide(getX() + speed, getY())) {
                moving = true;
                setPosition(getX() + speed, getY());
            }
        }
    }
}
