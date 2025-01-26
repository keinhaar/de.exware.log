
# de.exware.log
A minimalistic logging API, which handles log levels in antother way.

## What
This is ***NOT just another logging API***.
This API was developed with focus on size and useablility. The JAR file is just 11kb in size, which is great for use on very limited devices.

The main difference to all the other logging APIs out there is, that you can switch every LogLevel on and off separately. That means, you don't have to log warnings if you just 
want to see debug messages. This is very useful in combination with custom log levels, because you can define feature based logging.

This Base API only contains the bare minimum of classes needed to start with logging. Because of this, there isn't much chance for security issues in this API.
Look at the compatibility section for more infos on additional features.

## Usage
This Base API only provides one default handler, which is switched on by default. So to start logging, you do not need any configuration. Just create yout first Log instance 
and start using it.
```
Log LOG = Log.getLogger("hello");
LOG.debug("Hello World");
```
### Log Level
To change the log level, you can call setLevel(String) or setLevel(long). Both of them allow to set the Level to the given level. To add another level to the already enabled levels, you can use addLevel(long). For example
```
LOG.setLevel("DEBUG:TRACE:CUSTOM_1");
```
will enable DEBUG, TRACE and CUSTOM_1, but will disable all other Levels if they where enabled before.

### System.out
This API allows to redirect the output of System.out to the logging API. This way no message is lost. To enable this feature, just call **Log.setRedirectSystemOut(true);**

### Configuration file
This project does not support configuration by a configuration file, but you may want to use **de.exware.log.rcp ** to add this feature. That project will also contain a RollingFileLogListener.

### Example
Running this:
```
public  class  Example
{
	private  static  final  Log  LOG  =  Log.getLogger(Example.class);
	public  static  void  main(String[]  args)
	{
		LOG.debug("Without configuration the simple ConsoleLogListener is used, and all Levels are 		active."
			+  " This is like setting setLevel(Log.All)");
		LOG.setLevel("DEBUG");
		LOG.warn("This will not print, because Level is disabled");
		LOG.setLevel(Log.DEBUG  |  Log.WARNING);
		LOG.warn("This will print again");
		LOG.setLevel(Log.ALL);
		LOG.log(Log.CUSTOM_1,  "My custom level");
		System.out.println("HELLO");
		Log.setRedirectSystemOut(true);
		System.out.println("WORLD");
	}
}
```
results in this output:
```
Sun Jan 26 14:21:37 CET 2025 DEBUG [de.exware.log.Example] Without configuration the simple ConsoleLogListener is used, and all Levels are active. This is like setting setLevel(Log.All)
Sun Jan 26 14:21:37 CET 2025 WARN [de.exware.log.Example] This will print again
Sun Jan 26 14:21:37 CET 2025 LEVEL_1024 [de.exware.log.Example] My custom level
HELLO
Sun Jan 26 14:21:37 CET 2025 DEBUG [RootLogger] WORLD
```
## Build
The API is build by the nobuto build System. Just call **sh nobuto.sh -t dist**. The jar file will be in the main folder.

## Compatibility
There are some extensions to this API to allow to simulate other Logging APIs such as commons-logging, SLF4J and other. Each of them has it's own project, to keep the required 
JARs as small as possible.
Look for projects like de.exware.log.slf4j.

