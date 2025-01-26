import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.File;

import de.exware.nobuto.utils.Utilities;
import exware.AbstractExwareBuilder;

/**
 * Version 1
 * @author martin
 */
public class Build extends AbstractExwareBuilder
{
    public Build()
    {
        super("de.exware.log");
    }
    
    @Override
    public void compile() throws Exception
    {
//        addDependency(new ExwareDependency("de.exware.log", null));
        super.compile();
        cleanupNobuto(new File(CLASSES_DIR));
    }
    
    @Override
    public void dist() throws Exception
    {
        compile();
        copyResources();
        String jarFile = makeJar();
        Utilities.copy(jarFile, ".");
        verbosePrint(0, "Jar File created: " + new File(jarFile).getName());
    }
    
    @Override
    public void localRepo() throws Exception
    {
        super.localRepo();
    }
}
