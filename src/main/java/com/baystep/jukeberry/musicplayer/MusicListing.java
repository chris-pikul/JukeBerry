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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Acts as a database for the file listings we find.
 * @author Baystep/Chris Pikul
 */
public class MusicListing {
    private final ArrayList<Path> db;
    
    public MusicListing() {
        db = new ArrayList<>();
    }
    
    public void addFile(Path path) {
        db.add(path.toAbsolutePath());
    }
    
    public ArrayList<Path> findFile(String query) {
        ArrayList<Path> results = new ArrayList<>();
        
        Iterator it = db.iterator();
        Path tmp;
        while(it.hasNext()) {
            tmp = (Path) it.next();
            if(tmp.toString().contains(query))
                results.add(tmp);
        }
        
        return results;
    }
    
    public ArrayList<Path> getDb() {
        return db;
    }
    
    /*
    protected HashMap<String, ArrayList<String>> db;
    protected HashMap<String, String> locationRoots;
    
    public ArrayList<String> searchResults;
    
    public MusicListing() {
        db = new HashMap<>();
        
        locationRoots = new HashMap<>();
        searchResults = new ArrayList<>();
    }
    
    public void addFile(String category, String path) {
        if(db.containsKey(category))
            db.get(category).add(path);
    }
    
    public void addCategory(String name, String root) {
        locationRoots.put(name, root);
        db.put(name, new ArrayList<>());
    }
    
    public ArrayList<String> getCategoryFilesList(String name) {
        if(db.containsKey(name))
            return db.get(name);
        else
            return null;
    }
    
    public String[] getCategoryFiles(String name) {
        if(db.containsKey(name))
            return (String[]) db.get(name).toArray();
        else
            return null;
    }
    
    public String getCategoryDirectory(String name) {
        if(locationRoots.containsKey(name))
            return locationRoots.get(name);
        else
            return null;
    }
    
    public String translatePath(String category, String file) throws NullPointerException {
        if(db.containsKey(category)) {
            if(db.get(category).contains(file)) {
                return locationRoots.get(category) + file;
            } else throw new NullPointerException();
        } else throw new NullPointerException();
    }
    
    public String[] findFiles(String query) {
        searchResults.clear();
        
        Iterator it = db.entrySet().iterator();
        Iterator it2;
        String temp;
        while(it.hasNext()) {
            it2 = ((ArrayList<String>) it.next()).iterator();
            while(it2.hasNext()) {
                temp = (String) it2.next();
                if(temp.contains(query))
                    searchResults.add(temp);
            }
        }
        
        return (String[]) searchResults.toArray();
    }
    */
}
