package com.jwetherell.augmented_reality.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;

/**
 * This class extends DataSource to fetch data from Google Places.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class FreifunkDataSource extends NetworkDataSource {

    private static final String URL = "http://openwifimap.net/api/view_nodes_spatial?bbox=13.329334259033201%2C52.48664809680302%2C13.473529815673826%2C52.54932292485185";
    private static String key = null;
    private static Bitmap icon = null;

    public FreifunkDataSource(Resources res) {
        if (res == null)
            throw new NullPointerException();

        key = res.getString(R.string.google_places_api_key);

        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null)
            throw new NullPointerException();

        icon = BitmapFactory.decodeResource(res, R.drawable.buzz);
    }

    @Override
    public String createRequestURL(double lat, double lon, double alt, float radius, String locale) {
        try {
            return URL; // + "location=" + lat + "," + lon + "&radius="
            // + (radius * 1000.0f) + "&types=" + TYPES
            // + "&sensor=true&key=" + key;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<Marker> parse(String URL) {
        if (URL == null)
            throw new NullPointerException();

        InputStream stream = null;
        stream = getHttpGETInputStream(URL);
        if (stream == null)
            throw new NullPointerException();

        String string = null;
        string = getHttpInputString(stream);
        if (string == null)
            throw new NullPointerException();

        JSONObject json = null;
        try {
            json = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json == null)
            throw new NullPointerException();

        return parse(json);
    }

    @Override
    public List<Marker> parse(JSONObject root) {
        if (root == null)
            throw new NullPointerException();

        JSONObject jo = null;
        JSONArray dataArray = null;
        List<Marker> markers = new ArrayList<Marker>();

        try {
            if (root.has("rows"))
                dataArray = root.getJSONArray("rows");
            if (dataArray == null)
                return markers;
            int top = Math.min(MAX, dataArray.length());
            for (int i = 0; i < top; i++) {
                jo = dataArray.getJSONObject(i);
                Marker ma = processJSONObject(jo);
                if (ma != null)
                    markers.add(ma);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return markers;
    }

    private Marker processJSONObject(JSONObject jo) {
        if (jo == null)
            throw new NullPointerException();

        if (!jo.has("geometry"))
            throw new NullPointerException();

        Marker ma = null;
        try {
            Double lat = null, lon = null;

            if (!jo.isNull("geometry")) {
                JSONObject geo = jo.getJSONObject("geometry");
                JSONArray coordinates = geo.getJSONArray("coordinates");
                lon = coordinates.getDouble(0);
                lat = coordinates.getDouble(1);
            }
            if (lat != null) {
                String user = jo.getString("id");

                ma = new IconMarker(user, lat, lon, 0, Color.RED, icon);
            }
            Log.i("point", lon + ", " + lat + ": " + ma.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ma;
    }
}