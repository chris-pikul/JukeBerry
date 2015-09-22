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
package com.baystep.webservices;

import com.baystep.jukeberry.BerryLogger;
import com.baystep.jukeberry.JukeBerry;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * Simple WebSocket server
 * @author Baystep/Chris Pikul
 */
public class SocketServer extends WebSocketServer {
    private static int connectionsMade = 0;
    private static int currentConnections = 0;
    private final HashMap<InetSocketAddress, WebSocket> connections;
    
    public SocketServer(int port) {
        super(new InetSocketAddress(port));
        
        connections = new HashMap<>();
        
        BerryLogger.LOG.log(Level.INFO, "Started web socket server on port {0}", port);
    }
    
    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        connectionsMade++;
        currentConnections++;
        connections.put(ws.getRemoteSocketAddress(), ws);
        
        BerryLogger.LOG.log(Level.INFO, "New connection made ({0}): {1}", new Object[]{connectionsMade, ws.getRemoteSocketAddress()});
        
        broadcast("Welcome "+ws.getRemoteSocketAddress()+" to the party!");
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        BerryLogger.LOG.log(Level.INFO, "Connection closed: {0} - [{1}] {2}", new Object[]{ws.getRemoteSocketAddress(), code, reason});
        
        if(connections.containsKey(ws.getRemoteSocketAddress())) {
            connections.remove(ws.getRemoteSocketAddress());
            currentConnections--;
        }
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        BerryLogger.LOG.log(Level.INFO, "{0} - {1}", new Object[]{ws.getRemoteSocketAddress(), message});
        
        broadcast("Received a message: "+message);
    }

    @Override
    public void onError(WebSocket ws, Exception excptn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void broadcast(String msg) {
        msg = "SERVER: "+msg;
        for (Map.Entry<InetSocketAddress, WebSocket> keyval : connections.entrySet()) {
            if(keyval.getValue().isOpen()) {
                keyval.getValue().send(msg);
            } else {
                connections.remove(keyval.getKey());
                currentConnections--;
            }
        }
    }
}
