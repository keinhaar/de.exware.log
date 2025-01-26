package de.exware.log;

public interface LogFilter
{
    boolean isFiltered(String msg, Object obj);
    
    void setParameter(String name,String value);
}
