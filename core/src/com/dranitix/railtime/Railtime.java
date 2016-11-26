package com.dranitix.railtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.czyzby.kiwi.util.gdx.AbstractApplicationListener;

public class Railtime extends AbstractApplicationListener {
    public static final float UNIT_SCALE = 1 / 16f;
    private float width, height;

    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Texture tileset, crateTexture;
    private int mapWidth, mapHeight;
    SpriteBatch batch;

    private TextureRegion[] tiles;

    Stage stage;
    Player player;
    Array<Crate> crates = new Array<Crate>();

    BitmapFont titleFont;

    private long lastCrateUpdate = 0;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight());
        camera.update();

        stage = new Stage(new FitViewport(480, 800));
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();
        crateTexture = new Texture("crate.png");
        titleFont = new BitmapFont(Gdx.files.internal("carrier_command.xml"));
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

                stage.addActor(t);
            }
        }

        stage.addActor(player = new Player());
    }

    @Override
    protected void render(float deltaTime) {
        camera.position.set(MathUtils.clamp(player.getX(), 240, mapWidth * 16), MathUtils.clamp(player.getY(), 400, (mapHeight - 25) * 16), 0);
        camera.update();

        stage.act(deltaTime);
        stage.draw();

        if (TimeUtils.timeSinceMillis(lastCrateUpdate) >= 30000) {
            for (Crate crate : crates) {
                crate.remove();
            }
            crates.clear();

            int totalCrates = 0;
            while (totalCrates < 50) {
                int x = MathUtils.random(0, mapWidth * 16), y = MathUtils.random(0, mapHeight * 16);
                Actor hit = stage.hit(x, y, false);
                if (hit instanceof Tile && ((Tile) hit).getIndex() >= 25 && ((Tile) hit).getIndex() <= 50) {
                    Crate crate = new Crate(crateTexture, x, y);
                    stage.addActor(crate);

                    crate.setSize(0, 0);
                    crate.addAction(Actions.sizeBy(16, 16, 1f, Interpolation.exp5In));

                    totalCrates++;
                }
            }
            lastCrateUpdate = TimeUtils.millis();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.end();
    }

    @Override
    public void dispose() {
        tileset.dispose();
        stage.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
