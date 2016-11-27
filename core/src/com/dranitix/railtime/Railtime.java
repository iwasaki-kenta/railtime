package com.dranitix.railtime;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.kiwi.util.gdx.AbstractApplicationListener;

import java.util.Arrays;

public class Railtime extends AbstractApplicationListener implements InputProcessor {
    public static final float UNIT_SCALE = 1 / 16f;
    private float width, height;

    private OrthographicCamera camera, viewport;
    private Texture tileset, crateTexture;
    private int mapWidth, mapHeight;
    SpriteBatch batch;

    private TextureRegion[] tiles;
    Array<Tile> tileActors = new Array<Tile>();

    Stage game;
    Stage stage;
    Skin skin;

    Player player;
    Array<Crate> crates = new Array<Crate>();

    BitmapFont titleFont;
    BitmapFont uiFont;

    GlyphLayout glyphLayout;

    private QuestWindow questWindow;
    private Music backgroundMusic;

    private long lastCrateUpdate = 0;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight());
        camera.update();

        viewport = new OrthographicCamera();
        viewport.setToOrtho(false, width, height);
        viewport.update();

        game = new Stage(new FitViewport(480, 800, camera));
        stage = new Stage(new FitViewport(480, 800, viewport));
        skin = new Skin(Gdx.files.internal("craftacular-ui.json"));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(game);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        batch = new SpriteBatch();
        crateTexture = new Texture("crate.png");
        titleFont = new BitmapFont(Gdx.files.internal("gamer.fnt"), false);
        uiFont = new BitmapFont(Gdx.files.internal("gamer.fnt"), true);
        glyphLayout = new GlyphLayout();
        tileset = new Texture("catastrophi_tiles_16.png");

        TextureRegion[][] tiles = TextureRegion.split(tileset, 16, 16);

        int totalTiles = 0;
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) totalTiles++;
        }

        this.tiles = new TextureRegion[totalTiles];
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) this.tiles[x * tiles[0].length + y] = tiles[x][y];
        }

        FileHandle mapHandle = Gdx.files.internal("catastrophi_level2.csv");
        String[] lines = mapHandle.readString().split("\n");

        mapHeight = lines.length;
        mapWidth = lines[0].split(",").length;

        for (int y = 0; y < mapHeight; y++) {
            String[] contents = lines[y].split(",");
            for (int x = 0; x < mapWidth; x++) {
                int index = Integer.decode(contents[x]);
                Tile t = new Tile(index, this.tiles[index]);
                t.setPosition(x * 16, y * 16);
                t.setSize(16, 16);
                tileActors.add(t);
                game.addActor(t);
            }
        }

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("town.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.05f);
        backgroundMusic.play();
        game.addActor(player = new Player());
        loadUI();
    }

    private void loadUI() {
        questWindow = new QuestWindow(stage, skin);
        stage.addActor(questWindow);
    }

    private long lastQuestOpen = 0;

    private void loadQuestion(int id, final Crate crate) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Gdx.net.sendHttpRequest(builder.newRequest().url("http://163.47.11.80:3000/api/jobs/" + Integer.toString(id)).method("GET").build(), new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(final Net.HttpResponse httpResponse) {
                Json json = new Json();
                final Quest quest = json.fromJson(Quest.class, httpResponse.getResultAsString());
                if (quest != null) {
                    System.out.println(quest.getType());
                    System.out.println(quest.getContent());
                    System.out.println(Arrays.toString(quest.getImages()));

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            questWindow.loadQuest(quest, crate);
                            questWindow.addAction(Actions.sequence(Actions.show(), Actions.scaleTo(1, 1, 0.2f, Interpolation.bounceIn)));
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void cancelled() {

            }
        });
    }

    @Override
    protected void render(float deltaTime) {

        camera.position.set(MathUtils.clamp(player.getX(), 240, mapWidth * 16), MathUtils.clamp(player.getY(), 400, (mapHeight - 25) * 16), 0);
        camera.update();

        game.act(deltaTime);
        game.draw();

        Actor hit;
        if ((hit = game.hit(player.getX(), player.getY(), true)) != null && hit instanceof Crate) {
            batch.setProjectionMatrix(game.getCamera().combined);
            batch.begin();
            uiFont.getData().setScale(0.85f, 0.85f);
            uiFont.setColor(Color.YELLOW);
            uiFont.draw(batch, "QUEST", player.getX() - 25, player.getY() - 20);
            batch.end();
        }

        if (TimeUtils.timeSinceMillis(lastCrateUpdate) >= 15000) {
            for (Crate crate : crates) {
                crate.addAction(Actions.sequence(Actions.fadeOut(1), Actions.removeActor()));
            }
            crates.clear();

            int totalCrates = 0;
            while (totalCrates < 50) {
                int x = MathUtils.random(0, mapWidth * 16), y = MathUtils.random(0, mapHeight * 16);
                Actor tile = game.hit(x, y, false);
                if (tile instanceof Tile && ((Tile) tile).getIndex() >= 25 && ((Tile) tile).getIndex() <= 50) {
                    Crate crate = new Crate(crateTexture, x, y);
                    crates.add(crate);
                    game.addActor(crate);

                    crate.setScale(0, 0);
                    crate.addAction(Actions.scaleTo(1, 1, 1f, Interpolation.exp5In));

                    totalCrates++;
                }
            }
            lastCrateUpdate = TimeUtils.millis();
        }

        uiFont.setColor(Color.WHITE);

        viewport.update();
        batch.setProjectionMatrix(viewport.combined);
        batch.begin();
        titleFont.getData().setScale(2, 2);
        titleFont.draw(batch, "Railtime", Gdx.graphics.getWidth() / 2 - 75, Gdx.graphics.getHeight() - 150);
        titleFont.getData().setScale(0.85f, 0.85f);
        titleFont.setColor(Color.YELLOW);
        titleFont.getColor().a = 0.75f;
        if (Storage.MULTIPLIER != 0f) {
            titleFont.draw(batch, "Travel before 6:00PM", Gdx.graphics.getWidth() / 2 - getTextWidth("Travel before 6:00PM") / 2, Gdx.graphics.getHeight() - 185);
            titleFont.draw(batch, "to get a 2X more points!", Gdx.graphics.getWidth() / 2 - getTextWidth("to get a 2X more points!") / 2, Gdx.graphics.getHeight() - 200);
        }
        titleFont.setColor(Color.WHITE);
        titleFont.getColor().a = 0.75f;
        titleFont.getData().setScale(1, 1);

        titleFont.setColor(Color.YELLOW);
        titleFont.getColor().a = 0.75f;
        titleFont.draw(batch, "You're now at: ", 18, 30);
        titleFont.setColor(Color.WHITE);
        titleFont.getColor().a = 0.75f;
        titleFont.getData().setScale(1.5f, 1.5f);
        titleFont.draw(batch, Storage.getStation().toUpperCase(), getTextWidth("You're now at: ") / 2 + 65, 32);
        titleFont.getData().setScale(1, 1);
        titleFont.draw(batch, String.valueOf((int) ((Math.round(Storage.MONEY * Math.pow(10, 2)) / Math.pow(10, 2)) * 100)) + "PT", Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 15);
        titleFont.setColor(Color.RED);
        titleFont.getColor().a = 0.75f;
        titleFont.draw(batch, " +" + Integer.toString((int) (Storage.MULTIPLIER * 100)) + "%", Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 15);
        titleFont.setColor(Color.WHITE);
        titleFont.getColor().a = 0.75f;
        batch.end();

        stage.act(deltaTime);
        stage.draw();
    }

    float getTextWidth(String txt) {
        glyphLayout.setText(titleFont, txt);
        return glyphLayout.width;
    }

    @Override
    public void dispose() {
        tileset.dispose();
        backgroundMusic.dispose();
        game.dispose();
        stage.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        game.getViewport().update(width, height, true);
        stage.getViewport().update(width, height, true);
        viewport.setToOrtho(false, width, height);
    }

    @Override
    public boolean keyDown(int keycode) {
        Actor hit;
        switch (keycode) {
            case Input.Keys.NUM_1: {
                if (!questWindow.isVisible() && (hit = game.hit(player.getX(), player.getY(), true)) != null && hit instanceof Crate) {
                    loadQuestion(6, (Crate) hit);
                }
                break;
            }
            case Input.Keys.NUM_2: {
                if (!questWindow.isVisible() && (hit = game.hit(player.getX(), player.getY(), true)) != null && hit instanceof Crate) {
                    loadQuestion(0, (Crate) hit);
                }
                break;
            }
            case Input.Keys.NUM_3: {
                if (!questWindow.isVisible() && (hit = game.hit(player.getX(), player.getY(), true)) != null && hit instanceof Crate) {
                    loadQuestion(5, (Crate) hit);
                }
                break;
            }
            case Input.Keys.NUM_4: {
                if (!questWindow.isVisible() && (hit = game.hit(player.getX(), player.getY(), true)) != null && hit instanceof Crate) {
                    loadQuestion(4, (Crate) hit);
                }
                break;
            }
            case Input.Keys.TAB: {
                Storage.incrementStation();
                break;
            }
            case Input.Keys.Q: {
                Storage.MULTIPLIER = Storage.MULTIPLIER == 0f ? 1 : 0;
                if (Storage.MULTIPLIER == 1f) {
                    Table table = new Table();
                    table.setFillParent(true);
                    Dialog dialog = new Dialog("Notice", skin);
                    Label label = new Label("Travel before 6:00PM\nto get 2x more points!", skin);
                    label.setWrap(true);
                    label.setAlignment(Align.center);
                    dialog.text(label);
                    dialog.button("OK");
                    table.add(dialog).pad(16).width(stage.getWidth() - 64).height(250).row();
                    table.setTransform(true);
                    table.setOrigin(table.getPrefWidth() / 2, table.getPrefHeight() / 2);

                    stage.addActor(table);
                    table.setScale(0);
                    table.addAction(Actions.sequence(Actions.scaleTo(1, 1, 0.5f, Interpolation.circleIn), Actions.show()));

                }
                break;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
