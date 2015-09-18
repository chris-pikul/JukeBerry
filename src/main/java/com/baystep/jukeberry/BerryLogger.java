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

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.ErrorManager;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Global logger for file output.
 * @author Baystep/Chris Pikul
 */
public class BerryLogger {
    public static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static final String LOG_FILE = "berrymusic.log";
    
    private static FileHandler logFile;
    private static LogFormatter logFormatter;
    private static final Handler cnsHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if(getFormatter() == null)
                    setFormatter(new SimpleFormatter());
                
                try {
                    String msg = getFormatter().format(record);
                    if(record.getLevel().intValue() >= Level.WARNING.intValue())
                        System.err.write(msg.getBytes());
                    else
                        System.out.write(msg.getBytes());
                } catch(Exception e) {
                    reportError(null, e, ErrorManager.FORMAT_FAILURE);
                }
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
        };
    
    public static boolean supressConsole = false;
    
    public static void init() throws IOException {
        logFormatter = new LogFormatter();
        
        LOG.setLevel(Level.INFO);
        
        // Log to the file as well
        logFile = new FileHandler(LOG_FILE);
        logFile.setFormatter(logFormatter);
        LOG.addHandler(logFile);
        
        // Remove the default console handler
        Logger root = Logger.getLogger("");
        Handler[] handlers = root.getHandlers();
        if(handlers[0] instanceof ConsoleHandler)
            root.removeHandler(handlers[0]);
        
        // Override the console handler with a custom one.
        cnsHandler.setFormatter(logFormatter);
        LOG.addHandler(cnsHandler);
    }
    
    public static void supressConsole() {
        if(supressConsole == true) return;
        
        supressConsole = true;
        Logger root = Logger.getLogger("");
        root.removeHandler(cnsHandler);
    }
}
