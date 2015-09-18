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

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats the logs for single line entry.
 *
 * @author Baystep/Chris Pikul
 */
public class LogFormatter extends Formatter {
    /**
     * Argument list: 
     *  0 - Log Level 
     *  1 - Date/Time 
     *  2 - Class name 
     *  3 - Method name
     *  4 - Message
     */
    private final static String format = "{0} - {1,date} {1,time} {2} {3}: {4}";
          //"%1$s: %2$tF %2$tr %3$s %4$s - %5$s";
          //%2$tb %2$te, %2$tY %2$tk:%2$tM:%2$tS %2tp

    private final MessageFormat formatter;
    private Date date = new Date();
    private Object args[] = new Object[5];
    
    public LogFormatter() {
        formatter = new MessageFormat(format);
        args = new Object[5];
        date = new Date();
    }
    
    public synchronized String format(LogRecord record) {
        String message = formatMessage(record);
        
        args[0] = record.getLevel().getLocalizedName();
        
        date.setTime(record.getMillis());
        args[1] = date;
        
        if(record.getSourceClassName() != null)
            args[2] = record.getSourceClassName();
        else
            args[2] = "";
        
        if(record.getSourceMethodName() != null)
            args[3] = record.getSourceMethodName();
        else
            args[3] = "";
        
        args[4] = message;
        
        String formattedString = formatter.format(args);
        formattedString += System.lineSeparator();
        return formattedString;
    }

    /*
     public synchronized String format(LogRecord record) {
     StringBuilder sb = new StringBuilder();

     date.setTime(record.getMillis());    
     args[0] = date;

     StringBuffer text = new StringBuffer();
     if (formatter == null) {
     formatter = new MessageFormat(dateFormat);
     }
     formatter.format(args, text, null);
     sb.append(text);
     sb.append(" ");


     // Class name 
     if (record.getSourceClassName() != null) {
     sb.append(record.getSourceClassName());
     } else {
     sb.append(record.getLoggerName());
     }

     // Method name 
     if (record.getSourceMethodName() != null) {
     sb.append(" ");
     sb.append(record.getSourceMethodName());
     }
     sb.append(" - "); // lineSeparator



     String message = formatMessage(record);

     // Level
     sb.append(record.getLevel().getLocalizedName());
     sb.append(": ");

     // Indent - the more serious, the more indented.
     //sb.append( String.format("% ""s") );
     int iOffset = (1000 - record.getLevel().intValue()) / 100;
     for( int i = 0; i < iOffset;  i++ ){
     sb.append(" ");
     }


     sb.append(message);
     sb.append(lineSeparator);
     if (record.getThrown() != null) {
     try {
     StringWriter sw = new StringWriter();
     PrintWriter pw = new PrintWriter(sw);
     record.getThrown().printStackTrace(pw);
     pw.close();
     sb.append(sw.toString());
     } catch (Exception ex) {
     }
     }
     return sb.toString();
     }
     */
}
