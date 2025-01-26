/*
 * Log.java
 *
 * Copyright (c) 2000 eXware
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * eXware ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with eXware.
 */
package de.exware.log;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse stellt einen Mechanismus zum loggen von Fehler- und Debug-Messages bereit. Die Ausgaben können optional
 * auf die Konsole und/oder in eine Datei geschrieben werden. Dabei können verschiedene Ebenenen von Log-Meldungen
 * ausgegeben werden. A class to Log Data into files and/or to Standard OutputStream. Features are: - Logging the
 * current Memory usage - Different Logging levels - unlimited or limited number of log files - optional Date - optional
 * file logging - optional system.out logging
 */
public class Log
{
    // Allgemeine Konstanten
    public static final long FATAL_ERROR = 1;
    public static final long ERROR = 1 << 1;
    public static final long WARNING = 1 << 2;
    public static final long DEBUG = 1 << 3;
    public static final long INFO = 1 << 4;
    public static final long TRACE = 1 << 5;
    public static final long CUSTOM_1 = 1 << 10;
    public static final long CUSTOM_2 = 1 << 11;
    public static final long CUSTOM_3 = 1 << 12;
    public static final long CUSTOM_4 = 1 << 13;
    public static final long CUSTOM_5 = 1 << 14;
    public static final long ALL = -1;
    protected static Log rootLogger;
    private static Map<String,Log> loggers = new HashMap<>();
    private Log parent;
    private List<Log> children;
    private long m_level = -1;
    private List<LogObjectListener> logListeners;
    private long startTime;
    private String loggerName;
    private List<LogFilter> filter = null;
    private static LogStream outStream = null;
    private static LogStream errorStream = null;
    private static boolean redirectSystemOut = true;
    public static PrintStream systemOut = System.out;
    public static PrintStream systemError = System.err;


    protected Log()
    {
    }

    /**
     * Set the redirection of System out and error Stream.
     * @param b
     *            if true, then the System out and error stream will be redirected to LogStream which will forward the
     *            stream data to the rootLogger. called with false will restore the original System out and error
     *            streams.
     */
    public static void setRedirectSystemOut(boolean b)
    {
        if (b && outStream == null)
        {
            outStream = new LogStream(System.out, false);
            errorStream = new LogStream(System.err, true);
        }
        else if (b == false && outStream != null)
        {
            outStream.close();
            errorStream.close();
            outStream = null;
            errorStream = null;
            System.setOut(systemOut);
            System.setErr(systemError);
        }
        redirectSystemOut = b;
    }

    /**
     * Returns true, if the Standard System.out is redirected to the logger.
     * @return
     */
    public static boolean isRedirectSystemOut()
    {
        return redirectSystemOut;
    }

    /**
     * Allows to add a Filter to the this logger.
     * @param lol
     */
    public void addLogFilter(LogFilter lol)
    {
        if(filter == null)
        {
            filter = new ArrayList<>();
        }
        filter.add(lol);
    }

    /**
     * Get the Logger for the given Class name
     * @param type
     * @return
     */
    public static Log getLogger(Class<?> type)
    {
        return getLogger(type.getName());
    }
    
    /**
     * Return a logger by name
     * @param name
     * @return
     */
    public static Log getLogger(String name)
    {
        Log log = loggers.get(name);
        if(log == null)
        {
            log = new Log();
            log.loggerName = name;
            loggers.put(name, log);
            String[] parents = name.split("\\.");
            String parentLoggerName = "";
            for(int i=0;i<parents.length-1;i++)
            {
                if(i>0)
                {
                    parentLoggerName += ".";
                }
                parentLoggerName += parents[i];
            }
            if(name.length() > 0)
            {
                Log parentLog = getLogger(parentLoggerName);
                log.parent = parentLog;
                if(parentLog.children == null)
                {
                    parentLog.children = new ArrayList<>();
                }
                parentLog.children.add(log);
            }
            else
            {
                rootLogger = log;
                rootLogger.loggerName = "RootLogger";
            }
        }
        return log;
    }
    
    /**
     * set the logging level to level.
     */
    public void setLevel(long level)
    {
        m_level = level | Log.FATAL_ERROR;
    }

    public void addLevel(long level)
    {
        m_level = m_level | level;
    }
    
    /**
     * set the logging level to level.
     */
    public void setLevel(String levels)
    {
        long lev = FATAL_ERROR;
        String[] tokens = levels.split("[:,;]");
        for(int i=0; i<tokens.length;i++)
        {
            String level = tokens[i];
            lev = lev | (level.equalsIgnoreCase("ERROR") ? ERROR : 0);
            lev = lev | (level.equalsIgnoreCase("WARNING") ? WARNING : 0);
            lev = lev | (level.equalsIgnoreCase("WARN") ? WARNING : 0);
            lev = lev | (level.equalsIgnoreCase("DEBUG") ? DEBUG : 0);
            lev = lev | (level.equalsIgnoreCase("INFO") ? INFO : 0);
            lev = lev | (level.equalsIgnoreCase("ALL") ? ALL : 0);
            lev = lev | (level.equalsIgnoreCase("CUSTOM_1") ? CUSTOM_1 : 0);
            lev = lev | (level.equalsIgnoreCase("CUSTOM_2") ? CUSTOM_2 : 0);
            lev = lev | (level.equalsIgnoreCase("CUSTOM_3") ? CUSTOM_3 : 0);
            lev = lev | (level.equalsIgnoreCase("CUSTOM_4") ? CUSTOM_4 : 0);
            lev = lev | (level.equalsIgnoreCase("CUSTOM_5") ? CUSTOM_5 : 0);
        }
        m_level = lev;
    }

    /**
     * Liefert true zurück, wenn der angegebene Log-Level aktiviert ist. Andernfalls wird false zurückgeliefert.
     * 
     * @param level
     *            Log-Level, dessen Aktivität geprüft werden soll.
     * @return true, wenn angegebener Log-Lovel aktiviert ist, false ansonsten.
     */
    public boolean isLevelActive(long level)
    {
        return ((level > 0) && ((getLevel() & level) > 0));
    }

    /**
     * Returns the log level used by this logger.
     * @return
     */
    public long getLevel()
    {
        long lev = m_level;
        if(lev < 0 && parent != null)
        {
            lev = parent.getLevel();
        }
        return lev;
    }
    
    /**
     * Add a LogObjectListener. Listeners may do logging to console, files or even displaying a dialog.
     */
    public void addLogObjectListener(LogObjectListener handler)
    {
        if(logListeners == null)
        {
            logListeners = new ArrayList<>();
        }
        logListeners.add(handler);
    }

    /**
     * Remove a LogObjectListener. Listeners may do logging to console, files or even displaying a dialog.
     */
    public void removeLogObjectListener(LogObjectListener handler)
    {
        logListeners.remove(handler);
    }

    private List<LogObjectListener> getLogListeners()
    {
        List<LogObjectListener> listeners = logListeners;
        if(listeners == null && parent != null)
        {
            listeners = parent.getLogListeners();
        }
        if(this == rootLogger && listeners == null)
        {
            listeners = new ArrayList<>();
            ConsoleLogListener cl = new ConsoleLogListener();
            listeners.add(cl);
        }
        return listeners;
    }
    
    private List<LogFilter> getFilter()
    {
        List<LogFilter> filters = filter;
        if(filters == null && parent != null)
        {
            filters = parent.getFilter();
        }
        return filters;
    }
    
    /**
     * log the given Object if the level is <= the current loglevel
     */
    public void log(long level, String obj)
    {
        log(level, obj, null);
    }

    /**
     * log the given Object if the level is <= the current loglevel
     */
    public void log(long level, Throwable obj)
    {
        log(level, "", obj);
    }

    /**
     * log the given Object if the level is <= the current loglevel
     */
    public void log(long level, String msg, Object obj)
    {
        if (isLevelActive(level) == false || isFiltered(msg,obj)) 
        {
            return;
        }
        java.util.Date date = new java.util.Date();
        List<LogObjectListener>  listeners = getLogListeners();
        int listenerCount = listeners.size();
        for (int i = 0; i < listenerCount; i++)
        {
            LogObjectListener l = listeners.get(i);
            l.objectLogged(this,level, date, msg, obj);
        }
    }

    private boolean isFiltered(String msg, Object obj)
    {
        boolean filtered = false;
        List<LogFilter> filters = getFilter();
        for(int i=0;filtered == false && filters != null && i<filters.size();i++)
        {
            LogFilter filter = filters.get(i);
            filtered = filter.isFiltered(msg,obj);
        }
        return filtered;
    }

    /**
     * log the given text if logging isn't turned of (log level 0 = off)
     */
    public void log(String text)
    {
        log(Log.FATAL_ERROR, text);
    }

    /**
     * log the given text if logging isn't turned of (log level 0 = off)
     */
    public void log(Throwable ex)
    {
        log(Log.FATAL_ERROR, ex);
    }

    /**
     * log the given text if logging isn't turned of (log level 0 = off)
     */
    public void log(String msg, Throwable ex)
    {
        log(Log.FATAL_ERROR, msg, ex);
    }

    /**
     * Same as Log.log(Log.WARNING,text) log the given text if logging isn't turned of (log level 0 =
     * off)
     * 
     * @param text
     *            The Text that will be logged
     */
    public void warn(String text)
    {
        log(Log.WARNING, text);
    }

    /**
     * Same as Log.log(Log.WARNING,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void warn(Throwable ex)
    {
        log(Log.WARNING, ex);
    }

    /**
     * Same as Log.log(Log.WARNING,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void warn(String msg,Throwable ex)
    {
        log(Log.WARNING,msg, ex);
    }

    /**
     * Same as Log.log(Log.DEBUG,text) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param text
     *            The Text that will be logged
     */
    public void debug(String text)
    {
        log(Log.DEBUG, text, null);
    }

    /**
     * Same as Log.log(Log.DEBUG,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void debug(Throwable ex)
    {
        log(Log.DEBUG, ex);
    }

    /**
     * Same as Log.log(Log.DEBUG,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void debug(String msg,Throwable ex)
    {
        log(Log.DEBUG,msg, ex);
    }

    /**
     * Same as Log.log(Log.DEBUG,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void fatal(String msg,Throwable ex)
    {
        log(Log.FATAL_ERROR,msg, ex);
    }

    /**
     * Same as Log.log(Log.FATAL_ERROR,text) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param text
     *            The Text that will be logged
     */
    public void fatal(String text)
    {
        log(Log.FATAL_ERROR, text,null);
    }

    /**
     * Same as Log.log(Log.ERROR,text) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param text
     *            The Text that will be logged
     */
    public void error(String text)
    {
        log(Log.ERROR, text,null);
    }

    /**
     * Same as Log.log(Log.ERROR,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void error(Throwable ex)
    {
        log(Log.ERROR, ex);
    }

    /**
     * Same as Log.log(Log.ERROR,ex) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param ex
     *            The Throwable who's StackTrace will be logged
     */
    public void error(String msg, Throwable ex)
    {
        log(Log.ERROR, msg, ex);
    }

    /**
     * Same as Log.log(Log.INFO,text) log the given text if logging isn't turned of (log level 0 = off)
     * 
     * @param text
     *            The Text that will be logged
     */
    public void info(String text)
    {
        log(Log.INFO, text);
    }

    /**
     * Same as Log.log(Log.INFO,ex) log the given text if logging isn't turned of (log level 0 = off)
     * @param ex The Throwable who's StackTrace will be logged
     */
    public void info(Throwable ex)
    {
        log(Log.INFO, ex);
    }

    /**
     * Same as Log.log(Log.INFO,ex) log the given text if logging isn't turned of (log level 0 = off)
     * @param ex The Throwable who's StackTrace will be logged
     */
    public void info(String msg,Throwable ex)
    {
        log(Log.INFO,msg, ex);
    }

    /**
     * Support for simple Time Stopping. Make sure you do not have a stop inside of another called method.
     */
    public void start(long level)
    {
        if ((level & m_level) == 0 || level <= 0) {
            return;
        }
        startTime = System.currentTimeMillis();
    }

    /**
     * Support for simple Time Stopping. Make sure you do not have a stop inside of another called method.
     */
    public void stop(long level)
    {
        if ((level & m_level) == 0 || level <= 0) {
            return;
        }
        long diff = System.currentTimeMillis() - startTime;
        int min = (int) (diff / 60000);
        int sec = (int) ((diff - min * 60000) / 1000);
        int msec = (int) (diff - min * 60000 - sec * 1000);
        String smsec = "" + msec;
        while (smsec.length() < 3)
        {
            smsec = "0" + smsec;
        }
        String str = "Time used: " + min + ":" + sec + "." + smsec;
        log(str);
        // Allow subsequent stops!
        startTime = System.currentTimeMillis();
    }

    public static Log getRootLogger()
    {
        if(rootLogger == null)
        {
            getLogger("");
        }
        return rootLogger;
    }

    public String getName()
    {
        return loggerName;
    }
}

/**
 * Diese Klasse wird als ersatz für System.out und System.err gesetzt und sorgt so dafür, dass jedes System.out.println
 * über die LogKlasse läuft. Es wird weiterhin zwischen Error und Out unterschieden.
 */
class LogStream extends PrintStream
{
    private boolean error;
    
    public LogStream(PrintStream out, boolean error)
    {
        super(out);
        this.error = error;
        if (error)
        {
            System.setErr(this);
        }
        else
        {
            System.setOut(this);
        }
    }

    @Override
    public void print(String str)
    {
        log(str);
    }

    @Override
    public void println(String str)
    {
        log(str);
    }

    private void log(String str)
    {
        Log.getRootLogger().log(error ? Log.FATAL_ERROR : Log.DEBUG, str);
    }
    
    @Override
    public void print(boolean b)
    {
        log(b ? "true" : "false");
    }
    
    @Override
    public void print(int v)
    {
        log(String.valueOf(v));
    }

    @Override
    public void print(double v)
    {
        log(String.valueOf(v));
    }

    @Override
    public void print(char v)
    {
        log(String.valueOf(v));
    }

    @Override
    public void print(float v)
    {
        log(String.valueOf(v));
    }

    @Override
    public void print(long v)
    {
        log(String.valueOf(v));
    }

    @Override
    public void print(Object object)
    {
        log("" + object);
    }

    @Override
    public void print(char[] ch)
    {
        log(String.valueOf(ch));
    }
    
    @Override
    public void close()
    {}
}