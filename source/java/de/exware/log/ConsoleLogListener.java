package de.exware.log;

import java.io.PrintStream;
import java.util.Date;

public class ConsoleLogListener extends AbstractLogListener
{
    @Override
    public void objectLogged(Log log,long level, Date date, String msg, Object obj)
    {
        if(obj == null) obj = "";
        PrintStream outstream = log.systemOut;
        outstream.println(formatMessage(log, level, date, msg, obj));
    }
}
