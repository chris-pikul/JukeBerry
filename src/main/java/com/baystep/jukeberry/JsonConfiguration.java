/*
 * Copyright (C) 2015 Baystep
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.baystep.jukeberry;

import com.baystep.jukeberry.musicplayer.SourceDescription;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 * Loads the Json configuration file specified in the constant.
 * THIS IS HARD CODED, SO IT MUST BE MODIFIED TO ADD NEW OPTIONS.
 * @author Baystep/Chris Pikul
 */
public class JsonConfiguration {
    final String FILE_NAME = "config.cfg";
    
    // PUBLIC VARIABLES
    public int httpPort = 8080;
    public int websocketPort = 9090;
    public boolean disableHTTP = false;
    public boolean disableWebSocket = false;
    
    public String[] mpDirectories;
    public SourceDescription[] mpSources;
    public String mpStartService;
    // END PUBLIC VARIABLES
    
    private JSONObject jsonConfig;
    
    public void load() { load(FILE_NAME); }
    public void load(String fileName) {
        BerryLogger.LOG.info("Loading configuration file...");
        
        File configFile = new File(fileName);
        if(configFile.exists()) {
            JSONParser jparser = new JSONParser();
            try {
                Object obj = jparser.parse(new FileReader(configFile));
                jsonConfig = (JSONObject) obj;
                
                if(jsonConfig.containsKey("disable")) {
                    String dOpt = (String) jsonConfig.get("disable");
                    setDisableOption(dOpt);
                }
                
                if(jsonConfig.containsKey("http server")) {
                    JSONObject httpObj = (JSONObject) jsonConfig.get("http server");
                    if(httpObj.containsKey("port"))
                        httpPort = getInt(httpObj, "port");
                }
                
                if(jsonConfig.containsKey("websocket server")) {
                    JSONObject wsObj = (JSONObject) jsonConfig.get("websocket server");
                    if(wsObj.containsKey("port"))
                        websocketPort = getInt(wsObj, "port");
                }
                
                if(jsonConfig.containsKey("music player")) {
                    JSONObject mpObj = (JSONObject) jsonConfig.get("music player");
                    
                    if(mpObj.containsKey("start service")) {
                        mpStartService = (String) mpObj.get("start service");
                    }
                    
                    if(mpObj.containsKey("local directories")) {
                        mpDirectories = getStringArray(mpObj, "local directories");
                    }
                    
                    if(mpObj.containsKey("sources")) {
                        mpSources = getSourceDescriptionArray(mpObj, "sources");
                    }
                }
                
            } catch (IOException | org.json.simple.parser.ParseException ex) {
                BerryLogger.LOG.log(Level.WARNING, "Failed to parse configuration file: {0}", ex.getMessage());
            }
        } else {
            BerryLogger.LOG.warning("Failed to load the configuration file!");
        }
    }
    
    public void setDisableOption(String val) {
        switch(val.trim().toLowerCase()) {
            case "all":
                disableHTTP = disableWebSocket = true;
                break;
            case "http":
                disableHTTP = true;
                break;
            case "websocket":
                disableWebSocket = true;
                break;
            case "none":
                disableHTTP = disableWebSocket = false;
                break;
        }
    }
    
    public static int getInt(JSONObject obj, String key) {
        return castLongToInt((long) obj.get(key));
    }
    
    public static String[] getStringArray(JSONObject obj, String key) {
        ArrayList<String> list = new ArrayList<>();
        JSONArray array = (JSONArray) obj.get(key);
        if(array != null) {
            for(int i=0; i<array.size(); i++) {
                list.add(array.get(i).toString());
            }
        }
        String[] output = new String[list.size()];
        list.toArray(output);
        return output;
    }
    
    public static SourceDescription[] getSourceDescriptionArray(JSONObject obj, String key) {
        ArrayList<SourceDescription> list = new ArrayList<>();
        JSONArray array = (JSONArray) obj.get(key);
        if(array != null) {
            Iterator it = array.iterator();
            while(it.hasNext()) {
                JSONObject element = (JSONObject) it.next();
                
                SourceDescription sd = new SourceDescription();
                sd.name = element.get("name").toString();
                sd.fqn = element.get("fqn").toString();
                
                list.add(sd);
            }
        }
        SourceDescription[] output = new SourceDescription[list.size()];
        list.toArray(output);
        return output;
    }
    
    public static int castLongToInt(long l) {
        if(l < Integer.MIN_VALUE || l>Integer.MAX_VALUE)
            throw new IllegalArgumentException(l + " cannot be cast to integer safely.");
        return (int) l;
    }
}
