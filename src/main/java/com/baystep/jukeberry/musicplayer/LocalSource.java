/*
 * Copyright (C) 2015 Bayst
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Baystep/Chris Pikul
 */
public class LocalSource extends Source {
    public String[] searchDirectories;

    public LocalSource(String[] directories) {
        sd = new SourceDescription();
        sd.name = "local";
        sd.fqn = LocalSource.class.getName();
        
        searchDirectories = directories;
        
        try {
            refreshDatabase();
            System.out.println("Database: "+musicListing.getDb().toString());
        } catch (InterruptedException ex) {
            Logger.getLogger(LocalSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void refreshDatabase() throws InterruptedException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        
        RefreshLocalListings refresh = new RefreshLocalListings(searchDirectories);
        
        Future<MusicListing> futureDb = es.submit(refresh);
        
        MusicListing newDb;
        try {
            newDb = futureDb.get();
            if(newDb != null)
                musicListing = newDb;
        } catch (ExecutionException ex) {
            BerryLogger.LOG.log(Level.WARNING, "ExecutionException occured during database refresh! {0}", ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
