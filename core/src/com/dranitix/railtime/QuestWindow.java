package com.dranitix.railtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kenta Iwasaki on 11/27/2016.
 */
public class QuestWindow extends Window {
    Object[] listEntries = {"This is a list entry1", "And another one1", "The meaning of life1", "Is hard to come by1",
            "This is a list entry2", "And another one2", "The meaning of life2", "Is hard to come by2", "This is a list entry3",
            "And another one3", "The meaning of life3", "Is hard to come by3", "This is a list entry4", "And another one4",
            "The meaning of life4", "Is hard to come by4", "This is a list entry5", "And another one5", "The meaning of life5",
            "Is hard to come by5"};

    private Crate crate;

    public QuestWindow(Stage stage, Skin skin) {
        super("Quest", skin);

        setColor(Color.BLACK);


        setBounds(16, 128, stage.getWidth() - 32, stage.getHeight() / 1.5f + 50);

        setTransform(true);
        setOrigin(stage.getWidth() / 2, stage.getHeight() / 4);

        setVisible(false);
        setScale(0, 0);
    }

    private Pixmap syncDownloadPixmapFromURL(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);
            conn.connect();
            int length = conn.getContentLength();
            if (length <= 0) return null;
            InputStream is = conn.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            byte[] data = new byte[length];
            dis.readFully(data);
            Pixmap pixmap = new Pixmap(data, 0, data.length);
            return pixmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadQuest(Quest quest, final Crate crate) {
        clearChildren();
        add().row();

        switch (quest.getType()) {
            case "validate": {
                if (quest.getImages().length > 0) {
                    final Image image = new Image(new Texture(syncDownloadPixmapFromURL(quest.getImages()[0])));
                    add(image).pad(12).expandX().height(getStage().getHeight() / 3.5f).center().row();
                }

                final Label question = new Label(quest.getQuestion(), getSkin());
                question.setWrap(true);
                question.setAlignment(Align.center);
                question.getStyle().font.getData().setScale(0.5f);
                add(question).pad(12).width(getStage().getWidth() - 32).center().row();

                if (!quest.getContent().isEmpty()) {
                    final TextArea content = new TextArea(quest.getContent(), getSkin());
                    content.getStyle().font.getData().setScale(0.9f);
                    content.setDisabled(true);

                    final ScrollPane pane = new ScrollPane(content, getSkin());

                    add(pane).width(getStage().getWidth() - 64).height(200).row();
                }

                final TextButton yes = new TextButton("Yes", getSkin());
                final TextButton no = new TextButton("No", getSkin());

                final QuestWindow self = this;
                yes.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Storage.addCash();
                        self.addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.2f, Interpolation.bounceOut), Actions.hide()));
                        crate.addAction(Actions.sequence(Actions.scaleTo(0, 0, 3f, Interpolation.circleOut), Actions.removeActor()));
                    }
                });
                no.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Storage.addCash();
                        self.addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.2f, Interpolation.bounceOut), Actions.hide()));
                        crate.addAction(Actions.sequence(Actions.scaleTo(0, 0, 3f, Interpolation.circleOut), Actions.removeActor()));
                    }
                });

                add(yes).pad(12).expandX().row();
                add(no).pad(12).expandX().row();

                break;
            }
            case "normal": {

                final Image image = new Image(new Texture(syncDownloadPixmapFromURL(quest.getImages()[0])));
                add(image).pad(12).expandX().height(getStage().getHeight() / 3.5f).center().row();

                final Label question = new Label(quest.getQuestion(), getSkin());
                question.setWrap(true);
                question.setAlignment(Align.center);
                add(question).pad(12).width(getStage().getWidth() - 32).center().row();

                final TextField answer = new TextField(quest.getContent(), getSkin());
                answer.getStyle().font.getData().setScale(1f);
                add(answer).width(getStage().getWidth() - 80).row();

                final TextButton submit = new TextButton("Submit", getSkin());
                add(submit).pad(12).expandX().row();

                final QuestWindow self = this;
                submit.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Storage.addCash();
                        self.addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.2f, Interpolation.bounceOut), Actions.hide()));
                        crate.addAction(Actions.sequence(Actions.scaleTo(0, 0, 3f, Interpolation.circleOut), Actions.removeActor()));
                    }
                });

                break;
            }
            case "spam": {
                final Label label = new Label(quest.getQuestion(), getSkin());
                label.setWrap(true);
                label.setAlignment(Align.center);
                label.getStyle().font.getData().setScale(0.5f);
                add(label).pad(12).width(getStage().getWidth() - 32).center().row();

                Table table = new Table();
                table.setFillParent(true);

                final TextArea question = new TextArea(quest.getContent(), getSkin());
                question.getStyle().font.getData().setScale(0.5f);
                question.setDisabled(true);

                final ScrollPane pane = new ScrollPane(question, getSkin());

                add(pane).width(getStage().getWidth() - 64).height(200).row();

                final TextButton yes = new TextButton("Yes", getSkin());
                final TextButton no = new TextButton("No", getSkin());

                final QuestWindow self = this;
                yes.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        self.addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.2f, Interpolation.bounceOut), Actions.hide()));
                        crate.addAction(Actions.sequence(Actions.scaleTo(0, 0, 3f, Interpolation.circleOut), Actions.removeActor()));
                        Storage.addCash();
                    }
                });
                no.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Storage.addCash();
                        self.addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.2f, Interpolation.bounceOut), Actions.hide()));
                        crate.addAction(Actions.sequence(Actions.scaleTo(0, 0, 3f, Interpolation.circleOut), Actions.removeActor()));
                    }
                });

                add(yes).pad(12).expandX().row();
                add(no).pad(12).expandX().row();
                break;
            }
            case "selection": {
                final Label question = new Label(quest.getQuestion(), getSkin());
                question.setWrap(true);
                question.setAlignment(Align.center);
                question.getStyle().font.getData().setScale(0.5f);
                add(question).pad(12).width(getStage().getWidth() - 32).center().row();

                Table list = new Table();
                for (int i = 0; i < quest.getImages().length; i++) {
                    if (i % 3 == 0) list.add().row();
                    final ImageButton image = new ImageButton(new TextureRegionDrawable(
                            new TextureRegion(new Texture(syncDownloadPixmapFromURL(quest.getImages()[i])))));
                    final int finalI = i;
                    image.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            image.getImage().setColor(image.getImage().getColor().equals(Color.YELLOW) ? Color.WHITE : Color.YELLOW);
                        }
                    });
                    list.add(image).pad(12).width(128).height(128);
                }

                ScrollPane scrollPane = new ScrollPane(list, getSkin());
                add(scrollPane).expandX().row();

                final TextButton submit = new TextButton("Submit", getSkin());
                add(submit).pad(12).expandX().row();

                final QuestWindow self = this;
                submit.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Storage.addCash();
                        self.addAction(Actions.sequence(Actions.scaleTo(0, 0, 0.2f, Interpolation.bounceOut), Actions.hide()));
                        crate.addAction(Actions.sequence(Actions.scaleTo(0, 0, 3f, Interpolation.circleOut), Actions.removeActor()));
                    }
                });

                break;
            }
            default: {
                List list = new List(getSkin());
                list.setItems(listEntries);
                list.getSelection().setMultiple(true);
                list.getSelection().setRequired(false);
                ScrollPane scrollPane = new ScrollPane(list, getSkin());
                add(scrollPane).expandX().row();

                final TextButton button = new TextButton("Unsupported quest type!", getSkin());
                add(button).pad(12).expandX().row();

                System.out.println(quest.getType());
                break;
            }
        }

    }
}
