package com.calgen.udacity.lego.remote;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;

    static {
        URL url = null;
        try {
            /** Using my own alternate url which has some extra goodies
             * Default address by udacity
             * https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json*/
            url = new URL("https://raw.githubusercontent.com/Protino/dump/master/data.json");
        } catch (MalformedURLException ignored) {
        }
        BASE_URL = url;
    }
}
