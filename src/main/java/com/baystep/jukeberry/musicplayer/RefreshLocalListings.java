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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the refreshing and directory searching for the local filesystem.
 * @author Baystep/Chris Pikul
 */
public class RefreshLocalListings implements Callable<MusicListing> {
    private String[] searchDirs;
    
    public RefreshLocalListings(String[] dirs) {
        searchDirs = dirs;
    }
    
    @Override
    public MusicListing call() throws Exception {
        if(searchDirs == null) {
            BerryLogger.LOG.warning("No directories where supplied! Only the current working directory will be available.");
            searchDirs = new String[] {""};
        }
        
        Path startPath;
        Finder finder = new Finder("*.{txt}");
        for(String directory: searchDirs) {
            startPath = Paths.get(directory).normalize().toAbsolutePath();
            try {
                Files.walkFileTree(startPath, finder);
                finder.done();
            } catch (IOException ex) {
                Logger.getLogger(RefreshLocalListings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return finder.getDB();
    }
    
    public static class Finder extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher;
        private final MusicListing db;
        private int numMatches = 0;
        
        Finder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:"+pattern);
            db = new MusicListing();
        }
        
        void find(Path file) {
            Path name = file.getFileName();
            if(name != null && matcher.matches(name)) {
                numMatches++;
                db.addFile(file);
            }
        }
        
        void done() {
            System.out.println("Found matches = "+numMatches);
        }
        
        MusicListing getDB() {
            return db;
        }
        
        //CHECK EACH FILE
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            find(file);
            return FileVisitResult.CONTINUE;
        }
    }
    
}
