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
package com.baystep.jukeberry.musicplayer;

import com.baystep.jukeberry.BerryLogger;
import com.baystep.jukeberry.BerryMusic;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Thread starts the music playing and manages the sources.
 * @author Baystep/Chris Pikul
 */
public final class MusicPlayer extends Thread {
    public String[] localSearchDirectories;
    public SourceDescription[] sourceDescriptions;
    
    private final HashMap<String, Source> sources;
    private Source currentSource;
    
    private final BerryMusic parentApp;
    public MusicPlayer(BerryMusic parent) {
        parentApp = parent;
        sources = new HashMap<>();
    }
    
    public void init() {
        //Always start with at LEAST the local source
        LocalSource localSrc = new LocalSource(localSearchDirectories);
        sources.put(localSrc.sd.name, localSrc);
        
        //Load the rest dynamically.
        if(sourceDescriptions!=null && sourceDescriptions.length>0) {
            ClassLoader classLoader = MusicPlayer.class.getClassLoader();
            for(SourceDescription sd : sourceDescriptions) {
                if(!sd.isValid() || sources.containsKey(sd.name)) continue;
                try {
                    Class srcClass = classLoader.loadClass(sd.fqn);
                    if(Source.class.isAssignableFrom(srcClass)) {
                        Source newSrc = (Source) srcClass.getDeclaredConstructor(BerryMusic.class, SourceDescription.class).newInstance(parentApp, sd);
                        if(newSrc !=null) {
                            sources.put(sd.name, newSrc);
                        }
                    }
                } catch(ClassNotFoundException | 
                        NoSuchMethodException | 
                        SecurityException | 
                        InstantiationException | 
                        IllegalAccessException | 
                        IllegalArgumentException | 
                        InvocationTargetException ex) {
                    BerryLogger.LOG.log(Level.SEVERE, "Failed to load requested class: {0} ", ex);
                }
            }
        } else 
            BerryLogger.LOG.warning("Invalid, or no sources declared. Only local will be available!");
        
        //Find the first service to use (default to local)
        if(parentApp.config.mpStartService == null)
            parentApp.config.mpStartService = "local";
        
        if(sources.containsKey(parentApp.config.mpStartService))
            currentSource = sources.get(parentApp.config.mpStartService);
        else
            currentSource = localSrc;
        
        if(currentSource != null && currentSource.isValid()) {
            BerryLogger.LOG.info("Starting music player...");
            this.start();
        } else {
            BerryLogger.LOG.severe("No viable source was found! MusicPlayer cannot start.");
        }
    }
    
    @Override
    public void run() {
        
    }
}
