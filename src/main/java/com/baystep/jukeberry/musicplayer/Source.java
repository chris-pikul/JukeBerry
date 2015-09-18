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

/**
 * Base Source class. Extend for which ever sources you need.
 * The main config will help setup it's name.
 * @author Baystep/Chris Pikul
 */
public abstract class Source {
    protected SourceDescription sd;
        
    protected MusicListing musicListing;
    
    protected Source() {
    }
    public Source(SourceDescription setSD) {
        sd = setSD;
    }
    
    public abstract boolean isValid();
    
    public abstract void refreshDatabase() throws InterruptedException;
    
    /*
    public void refreshDatabase() throws InterruptedException {
        
    }*/
}
