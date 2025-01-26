package de.exware.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

abstract public class AbstractLogListener implements LogObjectListener
{
    private String format = "%d %l [%n] %m %o";
    
    protected String formatMessage(Log log, long level, Date date, String msg, Object obj)
    {
        String message = format;
        if(msg == null)
        {
            msg = "";
        }
        message = message.replace("%d", "" + date);
        message = message.replace("%n", log.getName());
        message = message.replace("%m", msg);
        message = message.replace("%l", getLevelName(level));
        String sobj = "";
        if(obj != null)
        {
            if(obj instanceof Throwable)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream pstream = new PrintStream(out);
                Throwable t = (Throwable) obj;
                t.printStackTrace(pstream);
                try
                {
                    pstream.close();
                }
                catch(Exception ex)
                {
                    //In GWT an IOException can be thrown by close().
                }
                sobj = out.toString();
            }
            else
            {
                sobj = " " + obj;
            }
        }
        message = message.replace("%o", sobj);
        return message;
    }
    
    private String getLevelName(long level)
    {
        String name = "unknown";
        if(level == Log.TRACE)
        {
            name = "TRACE";
        }
        else if(level == Log.DEBUG)
        {
            name = "DEBUG";
        }
        else if(level == Log.WARNING)
        {
            name = "WARN ";
        }
        else if(level == Log.ERROR)
        {
            name = "ERROR";
        }
        else if(level == Log.INFO)
        {
            name = "INFO ";
        }
        else if(level == Log.FATAL_ERROR)
        {
            name = "FATAL";
        }
        else
        {
            name = "LEVEL_" + level; 
        }
        return name;
    }

    protected String formatThread()
    {
        return "";
    }
    
    @Override
    public void setParameter(String name, String param)
    {
        if("format".equals(name))
        {
            format = param;
        }
    }
}
