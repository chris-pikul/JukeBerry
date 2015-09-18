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

import com.baystep.webservices.SocketServer;
import com.baystep.webservices.WebServer;
import com.baystep.jukeberry.musicplayer.MusicPlayer;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * RasberryPi intended media player.
 * Starts a web server, web socket server, and the MusicPlayer thread.
 * @author Baystep/Chris Pikul
 */
public class BerryMusic {
    private static final String help_Header = "Turn your RaspberryPi into a virtual jukebox of amazingness!\n\n";
    private static final String help_Footer = "\nCopyright 2015 Baystep and Chris Pikul, all rights reserved."
            +"\nIf any issues are present, please contact me at BaystepMedia@gmail.com";
    
    private static Options cmdOptions;
    private static HelpFormatter cmdHelp;
    
    public JsonConfiguration config;
    
    public BerryMusic() {
        // Setup LOGGING
        try {
            BerryLogger.init();
        } catch (IOException ex) {
            throw new RuntimeException("Could not initiate logging system!");
        }
        
        // Configuration loading
        config = new JsonConfiguration();
        config.load();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Berry Music\n");
        
        BerryMusic bm = new BerryMusic();
        
        buildOptions();
        CommandLineParser cmdParser = new DefaultParser();
        try {
            CommandLine cmd = cmdParser.parse(cmdOptions, args);
            
            if(cmd.hasOption("h")) {
                cmdHelp.printHelp("BerryMusic", help_Header, cmdOptions, help_Footer, true);
                System.exit(0);
            }
            
            if(cmd.hasOption("pw"))
                bm.config.httpPort = Integer.parseInt(cmd.getOptionValue("pw"));
            
            if(cmd.hasOption("ps"))
                bm.config.websocketPort = Integer.parseInt(cmd.getOptionValue("ps"));
            
            if(cmd.hasOption("d")) {
                String dOpt = cmd.getOptionValue("d");
                bm.config.setDisableOption(dOpt);
            }
            
            if(cmd.hasOption("ss"))
                bm.config.mpStartService = cmd.getOptionValue("ss");
            
            if(cmd.hasOption("sc")) {
                BerryLogger.supressConsole();
            }
            
        } catch(ParseException pe) {
            System.err.println("Command line parsing failed, reason: "+pe.getMessage());
        }
        
        bm.init();
    }
    
    private static void buildOptions() {
        cmdOptions = new Options();
        
        Option port = Option.builder("pw").argName("http-port").longOpt("port-server")
                .hasArg().type(int.class)
                .desc("sets HTTP listening port (default: 8080)")
                .build();
        cmdOptions.addOption(port);
        
        cmdOptions.addOption(Option.builder("ps").longOpt("port-socket").argName("port-socket")
                .desc("sets WebSocket listening port (default: 9090)").build());
        
        cmdOptions.addOption(Option.builder("d").longOpt("disable").argName("disable")
                .hasArg().type(String.class)
                .desc("disables services (options: all, websocket, http)").build());
        
        cmdOptions.addOption(Option.builder("ss").longOpt("starting-service").argName("starting-service")
                .hasArg().type(String.class).desc("set starting service (defaults to local)").build());
        
        cmdOptions.addOption(Option.builder("sc").longOpt("supress-console")
            .desc("Stop the logger from printing to the console.").build());
        
        cmdOptions.addOption(Option.builder("h").longOpt("help").desc("display the help info").build());
        
        cmdHelp = new HelpFormatter();
    }
    
    private void init() {
        if(!config.disableHTTP) {
            WebServer ws = new WebServer(config.httpPort);
            ws.init();
        } else
            BerryLogger.LOG.info("HTTP web server has been disabled.");
        
        if(!config.disableWebSocket) {
            SocketServer ss = new SocketServer(config.websocketPort);
            ss.start();
        } else
            BerryLogger.LOG.info("WebSocket server has been disabled.");
        
        MusicPlayer mp = new MusicPlayer(this);
        mp.localSearchDirectories = config.mpDirectories;
        mp.sourceDescriptions = config.mpSources;
        mp.init();
    }
    
    /**
     * Post a message to the console (? and log).
     * @param msg String containing a message.
     */
    public void message(String msg) {
        System.out.println(msg);
    }
}
