package com.dranitix.railtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Kenta Iwasaki on 11/27/2016.
 */
public class WebTexture {
    private final String url;
    private Texture texture;
    private volatile byte[] textureBytes;

    public WebTexture(String url, Texture tempTexture) {
        this.url = url;
        texture = tempTexture;
        downloadTextureAsync();
    }

    private void downloadTextureAsync() {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                textureBytes = downloadTextureBytes();
            }
        });
    }

    private byte[] downloadTextureBytes() {
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
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Texture getTexture() {
        if (textureBytes != null)
            processTextureBytes();
        return texture;
    }

    private void processTextureBytes() {
        try {
            Pixmap pixmap = new Pixmap(textureBytes, 0, textureBytes.length);
            Texture gdxTexture = new Texture(pixmap);
            gdxTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texture = gdxTexture;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            textureBytes = null;
        }
    }
}