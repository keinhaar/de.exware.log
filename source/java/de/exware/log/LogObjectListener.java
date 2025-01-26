/*
 * LogObjectHandler.java
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

import java.util.Date;

/**
 * This interface describes the methods that must be implemented by classes who are interested
 * in Objects beeing logged.
 * The implementing Object should be set with Log.setLogObjectHandler() to receive the
 * logging events.
 */
public interface LogObjectListener
{
/**
 * This method will be called any time the programmer called explicitly Log.getRootLogger().log(level,Object) and
 * also for any Exception or Error.
 * @param log The Log that was used to log the message
 * @param level the loglevel specified for this message. 
 * @param obj the object that has been logged
 * @param msg a string message describing the logging event.
 * @param date the time at that the logging event occured.
 */
    public void objectLogged(Log log, long level,Date date, String msg, Object obj);
    
    public void setParameter(String name,String param);
}