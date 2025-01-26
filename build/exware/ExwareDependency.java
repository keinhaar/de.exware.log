package exware;
import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import de.exware.nobuto.java.JavaBuilder;
import de.exware.nobuto.java.JavaDependency;
import de.exware.nobuto.utils.Utilities;

public class ExwareDependency extends JavaDependency
{
    public ExwareDependency(String projectname, String version)
    {
        super(projectname, version);
    }
    
    @Override
    public String getVersion()
    {
        String version = super.getVersion();
        if(version == null)
        {
            File dir = new File(AbstractExwareBuilder.LOCAL_REPOSITORY + "/" + getProjectname());
            if(dir.exists())
            {
                String[] versions = dir.list();
                Arrays.sort(versions, new Comparator<String>()
                {

                    @Override
                    public int compare(String o1, String o2)
                    {
                        return Utilities.compareVersions(o1, o2);
                    }
                });
                version = versions[0];
            }
            else
            {
                verbosePrint(0, "Project '" + getProjectname() + "' does not exist");
            }
        }
        return version;
    }
    
    @Override
    public void addToClasspath(JavaBuilder builder) throws Exception
    {
        AbstractExwareBuilder ebuilder = (AbstractExwareBuilder) builder;
        String version = getVersion();
        File file = new File(ebuilder.LOCAL_REPOSITORY + "/" + getProjectname() + "/" + version);
        if(file.exists())
        {
            File[] files = file.listFiles();
            for(int i=0;i<files.length;i++)
            {
                File f = files[i];
                if(f.getName().endsWith(".jar"))
                {
                    builder.addClasspathItem(f.getPath());
                    verbosePrint(1, "Added JAR " + f.getAbsolutePath());
                }
            }
            file = new File(file, "libs");
            if(file.exists())
            {
                files = file.listFiles();
                for(int i=0;i<files.length;i++)
                {
                    File f = files[i];
                    if(f.getName().endsWith(".jar"))
                    {
                        builder.addClasspathItem(f.getPath());
                        verbosePrint(1, "Added JAR " + f.getAbsolutePath());
                    }
                }
            }
        }
        verbosePrint(1, "Added Project " + getProjectname() + " in version " + version);
    }
}
