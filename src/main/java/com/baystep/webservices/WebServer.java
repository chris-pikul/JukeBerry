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
import com.baystep.jukeberry.BerryMusic;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple HTTP Web Server
 * @author Baystep/Chris Pikul
 */
public class WebServer extends Thread {
    /*  DEFAULTS FOR THE SYSTEM! CHANGE THIS IF YOU WANT  */
    public int listenPort = 80;
    public String documentRoot = "./www";
    public String indexPage = "index.html";
    
    //String that is sent with the responses for ALL requests
    public String responseTemplate = 
            "Connection: close\r\n"
            + "Server: BerryMusicServer v1\r\n"
            + "Cache-Control: no-cache\r\n";
    
    /*   ==============================================   */
    
    private enum HTTPMethod {
        NOT_SUPPORTED, GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH
    };
    
    private enum SimpleMIMEType {
        NOT_SUPPORTED, HTML, CSS, JS, 
        TEXT_PLAIN, TEXT_XML, TEXT_PDF, TEXT_DOC,
        IMAGE_BMP, IMAGE_JPG, IMAGE_GIF, IMAGE_PNG, IMAGE_SVG,
        AUDIO_MP3, AUDIO_BASIC, AUDIO_MIDI, AUDIO_OGG, AUDIO_WAV,
        VIDEO_AVI, VIDEO_MPEG, 
        FILE_ZIP, FILE_GZ, FILE_BZ2, FILE_TAR
    }
    
    private boolean continueLoop = true;
            public void requestStop() { continueLoop = false; }
            
    private final HashMap<String, String> cachedFiles;

    public WebServer(int port) {
        listenPort = port;
        
        cachedFiles = new HashMap<>();
        
        cachedFiles.put("404", "<!DOCTYPE html><html><head><title>404 Not Found</title></head>"
                + "<body><h1>404 Not Found</h1><h2>The Page requested could not be found on this server.</h2></body></html>");
        
        cachedFiles.put("500", "<!DOCTYPE html><html><head><title>404 Not Found</title></head>"
                + "<body><h1>500 Internal Server Error</h1><h2>Something went wrong on the server side.</h2></body></html>");
    }
    
    public void init() {
        BerryLogger.LOG.info("Starting web server thread...");
        this.start();
    }
    
    @Override
    public void run() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(listenPort);
        } catch (IOException ex) {
            BerryLogger.LOG.severe(ex.getMessage());
        }
        
        if(socket == null) {
            BerryLogger.LOG.severe("Failed to start ServerSocket object!");
            return;
        }
        
        BerryLogger.LOG.log(Level.INFO, "Socket listening to port {0}", listenPort);
        
        while(continueLoop) {
            try {
                Socket connectionSocket = socket.accept();
                
                InetAddress clientIP = connectionSocket.getInetAddress();
                BerryLogger.LOG.log(Level.INFO, "Connection request from {0}", clientIP.toString());
                
                BufferedReader clientInput = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream clientOutput = new DataOutputStream(connectionSocket.getOutputStream());
                
                processHTTPRequest(clientInput, clientOutput, clientIP.toString());
            } catch (IOException | NullPointerException ex) {
                BerryLogger.LOG.severe(ex.getMessage());
            }
        }
    }
    
    private void processHTTPRequest(BufferedReader clientInput, DataOutputStream clientOutput, String clientIP) {
        HTTPMethod method = HTTPMethod.NOT_SUPPORTED;
        
        try {
            String inp = clientInput.readLine();
            /*
                Request example (when standard)
                    GET /index.html HTTP/1.0
                Tokens will read:
                    [0] GET
                    [1] /index.html
                    [2] HTTP/1.0
            */
            String[] requestTokens = inp.split("\\s");
            
            // Process the request type!
            String requestMethod = requestTokens[0].toUpperCase();
            if(requestMethod.startsWith("GET"))
                method = HTTPMethod.GET;
            else if(requestMethod.startsWith("HEAD"))
                method = HTTPMethod.HEAD;
            
            // Fall back to not supported
            if(method == HTTPMethod.NOT_SUPPORTED) {
                try {
                    clientOutput.writeBytes(generateHTTPHeader(501));
                    clientOutput.close();
                    return;
                } catch(Exception e) {
                    BerryLogger.LOG.log(Level.WARNING, "Method not supported, error in responding: {0}", e.getMessage());
                }
            }
            
            String path = requestTokens[1].trim();
            BerryLogger.LOG.log(Level.INFO, "Request: {0} GET: {1}", new Object[]{clientIP, path});
            
            //Clean up the path for common "rewriting" aliases
            if(!path.startsWith("/")) //Make sure it starts with
                path = "/"+path;
            
            if(path.endsWith("/")) //look for a index instead
                path += indexPage;
            
            path = documentRoot + path; //prefix with the document root (could help trick the OS into not allowing folder traversing)
            
            BerryLogger.LOG.log(Level.INFO, "Requested file: {0}", path);
            
            File requestedFile = new File(path);
            if(requestedFile.exists() && 
                    requestedFile.canRead() && 
                    !requestedFile.isDirectory() && 
                    !requestedFile.isHidden()) {
                
                //We should have a valid input stream now...
                SimpleMIMEType fileType = findMIMEType(path);
                String response = generateHTTPHeader(200, fileType);
                clientOutput.writeBytes(response);
                
                //Respond with file contents if the method is GET
                if(method == HTTPMethod.GET) {
                    FileInputStream reqFileStream = null;
                    try {
                        reqFileStream = new FileInputStream(requestedFile);
                    } catch(Exception e) { //Failed to open the file for some reason.
                        try {
                            clientOutput.writeBytes(generateHTTPHeader(500));
                            clientOutput.close();
                            BerryLogger.LOG.log(Level.SEVERE, "Failed to open requested file, even after checks.");
                        } catch(Exception e2) { //Failed to notify the client
                            BerryLogger.LOG.log(Level.SEVERE, "Failed to notify the client of the failure to read the file!");
                        }
                    }
                    
                    if(reqFileStream != null) {
                        int readByte;
                        while(true) {
                            readByte = reqFileStream.read();
                            if(readByte == -1)
                                break;
                            else
                                clientOutput.write(readByte);
                        }
                        reqFileStream.close();
                    }
                }
            } else {
                try {
                    if(cachedFiles.containsKey("404")) {
                        clientOutput.writeBytes(generateHTTPHeader(404, SimpleMIMEType.HTML));
                        clientOutput.writeBytes(cachedFiles.get("404"));
                    } else {
                        clientOutput.writeBytes(generateHTTPHeader(404));
                    }
                    clientOutput.close();
                    BerryLogger.LOG.log(Level.WARNING, "[404] The requested file \'{0}\'could not be found or is not accessable.", path);
                } catch(Exception e) {
                    BerryLogger.LOG.log(Level.SEVERE, "Failed to notify the client of the failure to read the file!");
                }
            }
            
            clientOutput.close();
        } catch (IOException ex) {
            BerryLogger.LOG.log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            try {
                clientOutput.writeBytes(generateHTTPHeader(500));
                clientOutput.close();
                BerryLogger.LOG.log(Level.SEVERE, "Unknown error occured while trying to process the clients request!");
            } catch(Exception e2) {
                BerryLogger.LOG.log(Level.SEVERE, "Error occured trying to notify about unknown error. Everythings screwed.");
            }
        }
        BerryLogger.LOG.info("Closing stream...");
    }
    
    private String generateHTTPHeader(int code) {
        String out = "HTTP/1.0 ";
        
        switch(code) {
            case 200:
                out += "200 OK";
                break;
            case 201:
                out += "201 Created";
                break;
            case 204:
                out += "204 No Content";
                break;
            case 301:
                out += "301 Moved Permanently";
                break;
            case 304:
                out += "304 Not Modified";
                break;
            case 400:
                out += "400 Bad Request";
                break;
            case 401:
                out += "401 Unauthorized";
                break;
            case 403:
                out += "403 Forbidden";
                break;
            case 404:
                out += "404 Not Found";
                break;
            case 409:
                out += "409 Conflict";
                break;
            case 500:
                out += "500 Internal Server Error";
                break;
        }
        out += "\r\n";
        
        out += responseTemplate;
        
        return out;
    }
    private String generateHTTPHeader(int code, SimpleMIMEType fileType) {
        String s = generateHTTPHeader(code);
        s += getMIME(fileType);
        s += "\r\n";
        return s;
    }
    
    /**
     * Gets the official MIME string from the enumeration
     * TODO: Complete the MIME types.
     * @param mime SimpleMIMEType enum
     * @return String MIME
     */
    private String getMIME(SimpleMIMEType mime) {
        String out = "Content-Type: ";
        switch(mime) {
            case HTML:
                out += "text/html";    break;
            case CSS:
                out += "text/css";    break;
            case JS:
                out += "application/x-javascript";    break;
            case TEXT_PLAIN:
                out += "text/plain";    break;
            case TEXT_XML:
                out += "application/xml";    break;
            case TEXT_PDF:
                out += "application/pdf";    break;
            case TEXT_DOC:
                throw new AssertionError(mime.name());
            case IMAGE_BMP:
                out += "image/bmp";    break;
            case IMAGE_JPG:
                out += "image/jpeg";    break;
            case IMAGE_GIF:
                out += "image/gif";    break;
            case IMAGE_PNG:
                out += "image/png";    break;
            case IMAGE_SVG:
                out += "image/svg+xml";    break;
            case AUDIO_MP3:
                out += "audio/mpeg";    break;
            case AUDIO_BASIC:
                out += "audio/basic";    break;
            case AUDIO_MIDI:
                out += "audio/x-midi";    break;
            case AUDIO_OGG:
                out += "audio/vorbis";    break;
            case AUDIO_WAV:
                out += "audio/wav";    break;
            case VIDEO_AVI:
                throw new AssertionError(mime.name());
            case VIDEO_MPEG:
                out += "video/mpeg";    break;
            case FILE_ZIP:
                out += "application/zip";    break;
            case FILE_GZ:
                out += "application/x-gzip";    break;
            case FILE_BZ2:
                out += "application/x-bzip2";    break;
            case FILE_TAR:
                out += "application/x-tar";    break;
            default:
                throw new AssertionError(mime.name());
        }
        out += "\r\n";
        return out;
    }
    
    /**
     * Finds the SimpleMIMEType from the path name.
     * TODO: FINISH SUPPORTING MORE EXTENSIONS!
     * @param filePath String file name/path
     * @return SimpleMIMEType enum
     */
    private SimpleMIMEType findMIMEType(String filePath) {
        String p = filePath.toLowerCase();
        
        if(p.endsWith(".htm") || p.endsWith(".html") || p.endsWith(".dhtml") || p.endsWith(".xhtml"))
            return SimpleMIMEType.HTML;
        else if(p.endsWith(".css"))
            return SimpleMIMEType.CSS;
        else if(p.endsWith(".js"))
            return SimpleMIMEType.JS;
        else if(p.endsWith(".jpg") || p.endsWith(".jpeg"))
            return SimpleMIMEType.IMAGE_JPG;
        else if(p.endsWith(".gif"))
            return SimpleMIMEType.IMAGE_GIF;
        else if(p.endsWith(".bmp"))
            return SimpleMIMEType.IMAGE_BMP;
        else if(p.endsWith(".png"))
            return SimpleMIMEType.IMAGE_PNG;
        else if(p.endsWith(".svg"))
            return SimpleMIMEType.IMAGE_SVG;
        
        return SimpleMIMEType.NOT_SUPPORTED;
    }
    
    public void addFileToCache(String friendlyPath, String fqn) {
        try {
            String content;
            File file = new File(fqn);
            if(file.exists() && file.canRead() && !file.isDirectory()) {
                content = new String( Files.readAllBytes( Paths.get(fqn) ) );
                
                cachedFiles.put(friendlyPath, content);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void addFileToCache(String path) {
        addFileToCache(path, path);
    }
}
