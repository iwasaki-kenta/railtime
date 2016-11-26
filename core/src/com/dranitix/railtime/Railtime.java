package com.dranitix.railtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.kiwi.util.gdx.AbstractApplicationListener;

public class Railtime extends AbstractApplicationListener {
    public static final float UNIT_SCALE = 1 / 16f;
    private float width, height;

    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera, viewport;
    private Texture tileset, crateTexture;
    private int mapWidth, mapHeight;
    SpriteBatch batch;

    private TextureRegion[] tiles;

    Stage game;
    Stage stage;
    Skin skin;

    Player player;
    Array<Crate> crates = new Array<Crate>();

    BitmapFont titleFont;
    BitmapFont uiFont;

    GlyphLayout glyphLayout;

    private Table questTable;

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

                game.addActor(t);
            }
        }

        game.addActor(player = new Player());
        loadUI();
    }

    private void loadUI() {
        questTable = new Table();
        questTable.setColor(Color.BLACK);
        questTable.getColor().a = 0.7f;
        questTable.setFillParent(true);
        stage.addActor(questTable);

        final TextButton button = new TextButton("Click me!", skin);
        questTable.add(button);

        questTable.setTransform(true);
        questTable.setOrigin(questTable.getPrefWidth() / 2, questTable.getPrefHeight() / 2);

        questTable.setVisible(false);

    }

    private long lastQuestOpen = 0;

    @Override
    protected void render(float deltaTime) {
        camera.position.set(MathUtils.clamp(player.getX(), 240, mapWidth * 16), MathUtils.clamp(player.getY(), 400, (mapHeight - 25) * 16), 0);
        camera.update();

        game.act(deltaTime);
        game.draw();

        stage.act(deltaTime);
        stage.draw();

        Actor hit;
        if ((hit = game.hit(player.getX(), player.getY(), true)) != null && hit instanceof Crate) {
            batch.setProjectionMatrix(game.getCamera().combined);
            batch.begin();
            uiFont.getData().setScale(0.85f, 0.85f);
            uiFont.setColor(Color.YELLOW);
            uiFont.draw(batch, "QUEST", player.getX() - 25, player.getY() - 20);
            batch.end();

            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && TimeUtils.timeSinceMillis(lastQuestOpen) >= 300) {
                if (!questTable.isVisible()) {
                    questTable.setScale(0, 0);
                    questTable.addAction(Actions.sequence(Actions.show(), Actions.scaleTo(1, 1, 1, Interpolation.bounceIn)));
                } else {
                    questTable.addAction(Actions.sequence(Actions.scaleTo(0, 0, 1, Interpolation.bounceOut), Actions.hide()));
                }
                lastQuestOpen = TimeUtils.millis();
            }
        }

        if (TimeUtils.timeSinceMillis(lastCrateUpdate) >= 30000) {
            for (Crate crate : crates) {
                crate.remove();
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
        titleFont.getData().setScale(1, 1);
        String text = "You're now at: WHAMPOA";
        titleFont.setColor(Color.YELLOW);
        titleFont.draw(batch, "You're now at: ", 18, 30);
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(2, 2);
        titleFont.draw(batch, "WHAMPOA", getTextWidth("You're now at: ") / 2 + 28, 35);
        batch.end();
    }

    float getTextWidth(String txt) {
        glyphLayout.setText(titleFont, txt);
        return glyphLayout.width;
    }

    @Override
    public void dispose() {
        tileset.dispose();
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
}
