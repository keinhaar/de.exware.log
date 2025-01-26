package de.exware.log;

public class Example
{
    private static final Log LOG = Log.getLogger(Example.class);
    
    public static void main(String[] args)
    {
        LOG.debug("Without configuration the simple ConsoleLogListener is used, and all Levels are active."
            + " This is like setting setLevel(Log.All)");
        LOG.setLevel("DEBUG");
        LOG.warn("This will not print, because Level is disabled");
        LOG.setLevel(Log.DEBUG | Log.WARNING);
        LOG.warn("This will print again");
        LOG.setLevel(Log.ALL);
        LOG.log(Log.CUSTOM_1, "My custom level");
        System.out.println("HELLO");
        Log.setRedirectSystemOut(true);
        System.out.println("WORLD");
    }
}
