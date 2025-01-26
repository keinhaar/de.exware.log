package exware;
import static de.exware.nobuto.utils.Utilities.verbosePrint;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import de.exware.nobuto.Dependency;
import de.exware.nobuto.subversion.SubVersion;
import de.exware.nobuto.utils.Utilities;

public class AbstractExwareBuilder extends de.exware.nobuto.java.JavaBuilder
{
    protected final String CLASSES_DIR = "out";
    protected final String TMP = "tmp";
    protected final String SOURCES_DIR = "source/java";
    protected final String RESOURCES_DIR = "resources";
    protected final String DISTRIBUTION_DIR = "dist";
    protected static final String LOCAL_REPOSITORY = "../repository";
    protected final String SUBVERSION_REPOSITORY = "file:///daten/technik/svn/java";
    protected final String REMOTE_REPOSITORY = SUBVERSION_REPOSITORY + "/repository/trunk";
    protected String JAR_NAME;
    protected String shortProjectName = "generic";
    private boolean commitLocalRepoOnMakeVersion = true;
    
    public AbstractExwareBuilder(String projectName)
    {
        super(projectName);
        JAR_NAME = getProjectname() + ".jar";
    }

    public AbstractExwareBuilder(String projectName, boolean commitLocalRepoOnMakeVersion)
    {
        super(projectName);
        JAR_NAME = getProjectname() + ".jar";
        this.commitLocalRepoOnMakeVersion = commitLocalRepoOnMakeVersion;
    }

    @Override
    protected void checkTools() throws IOException, InterruptedException
    {
        super.checkTools();
        SubVersion.checkTools();
    }
    
    @Override
    public void clean() throws IOException
    {
        verbosePrint(1, "Cleaning up");
        Utilities.delete(CLASSES_DIR);
        Utilities.delete(DISTRIBUTION_DIR);
        Utilities.delete(TMP);
    }
    
    public void localRepo() throws Exception
    {
        verbosePrint(1, "Local Repo");
        if(new File(LOCAL_REPOSITORY).exists() == false)
        {
            throw new IllegalStateException("Local Repository does not exist: " + LOCAL_REPOSITORY);
        }
        compile();
        copyResources();
        String jarFile = makeJar();
        String localRepoPath = LOCAL_REPOSITORY + "/" + getProjectname() + "/" + getVersion() + "/";
        File dir = new File(localRepoPath);
        dir.mkdirs();
        File jarDir = new File(jarFile).getParentFile();
        Utilities.copy(jarDir, dir, true);
        writeVersion();
    }

    public void makeVersion() throws Exception
    {
        verbosePrint(1, "Make Version");
        checkTools();
        SubVersion svn = SubVersion.getDefaultinstance();
        String message = readInput("Commit Message for local changes.", "Version " + getVersion());
        if(message == null || message.length() == 0)
        {
            throw new IOException("No Message");
        }
        svn.commit(".", message);
        if(commitLocalRepoOnMakeVersion)
        {
            svn.addAll(LOCAL_REPOSITORY + "/" + getProjectname(), getVersion());
            svn.commit(LOCAL_REPOSITORY + "/" + getProjectname(), message);
        }
        svn.branch(SUBVERSION_REPOSITORY + "/" + getProjectname() + "/trunk"
            , SUBVERSION_REPOSITORY + "/" + getProjectname() + "/branches/V" + getVersion(), message);
    }
    
    protected String makeJar() throws IOException
    {
        verbosePrint(1, "Make Jar");
        File dir = new File(TMP, "make-jar");
        Utilities.delete(dir);
        dir.mkdirs();
        Utilities.copy(CLASSES_DIR, dir, true);
        Utilities.delete(dir + "/nobuto.jar");
        Utilities.delete(dir + "/Build.class");
        Utilities.delete(dir + "/AbstractExwareBuilder.class");
        Utilities.delete(dir + "/AbstractExwareBuilder$1.class");
        Utilities.delete(dir + "/AbstractExwareBuilder$2.class");
        Utilities.delete(dir + "/ExwareDependency.class");
        Utilities.delete(dir + "/ExwareDependency$1.class");
        String jarFile = dir + "/" + JAR_NAME;
        jar(jarFile, dir, null, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".class");
            }
        });
        Utilities.delete(dir, true, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".class");
            }
        });
        return jarFile;
    }
    
    public void copyResources() throws IOException
    {
        verbosePrint(1, "Copy Resources");
        Utilities.copy("resources/common", CLASSES_DIR, true);
        Utilities.replaceInFile(CLASSES_DIR + "/plugin.xml", "UTF-8", "\\$\\{VERSION\\}", getVersion());
        Utilities.replaceInFile(CLASSES_DIR + "/pluginx.xml", "UTF-8", "\\$\\{VERSION\\}", getVersion());
        Utilities.copy("version.txt", CLASSES_DIR, true);
    }
    
    @Override
    public void compile() throws Exception
    {
        checkVersion(); 
        addSources(SOURCES_DIR);
        List<Dependency> deps = readElipseDependencies();
        for(int i=0;i<deps.size();i++)
        {
            addDependency(deps.get(i));
        }
        super.compile();
    }
    
    protected void checkVersion() throws IOException, InterruptedException
    {
        SubVersion svn = SubVersion.getDefaultinstance();
        boolean exists = svn.checkPathExists(REMOTE_REPOSITORY + "/" + getProjectname() + "/" + getVersion());
        if(exists)
        {
            System.out.println("Version exists: " + REMOTE_REPOSITORY + "/" + getProjectname() + "/" + getVersion());
            String version = null;
            String message = "Please enter the Version number for this build of "
                + getProjectname() + " (Last Version was: " + getVersion() 
                + "). \r\nThe version number should be changed on any build, that you will distribute to the remote repository:";
            version = readInput(message);
            if(version.trim().length() > 0)
            {
                setVersion(version);
            }
        }
    }
    
    @Override
    protected Dependency createDependency(String kind, String path, String combineaccessrule)
    {
        if(path.startsWith("/") && "src".equals(kind))
        {
            return new ExwareDependency(path.substring(1), null);
        }
        return super.createDependency(kind, path, combineaccessrule);
    }
    
    protected void copyPlugins() throws IOException
    {
        File pluginFolder = new File(DISTRIBUTION_DIR + "/plugins");
        pluginFolder.mkdir();
        List<Dependency> deps = getDependencies();
        for(int i=0;i<deps.size();i++)
        {
            Dependency dep = deps.get(i);
            String version = dep.getVersion();
            File source = new File(LOCAL_REPOSITORY, dep.getProjectname() + "/" + version);
            if(source.exists())
            {
                File targetDir = new File(pluginFolder, dep.getProjectname() + "_" + version);
                targetDir.mkdir();
                Utilities.copy(source, targetDir, true);
            }
        }
    }

    public void installer() throws Exception
    {
        updateSite();
        File scandorDir = new File("install_zip/" + shortProjectName);
        scandorDir.mkdirs();
        Utilities.copy(DISTRIBUTION_DIR, scandorDir, true);
        Utilities.echo("file://" + new File(".").getAbsolutePath() + "/update-site/", scandorDir.getPath() + "/update/update.sites");
        Utilities.copy(RESOURCES_DIR + "/installer/libs", scandorDir.getPath() + "/update/libs");
        Utilities.runCommand(scandorDir.getPath() + "update", true, "sh", "recover.sh");
        Utilities.delete(new File(scandorDir.getPath() + "/update/libs"), true);
        Utilities.delete(new File(scandorDir.getPath() + "/update/update.log"), true);
        Utilities.zip(shortProjectName + "-" + getVersion() + ".zip", scandorDir.getParentFile());
    }
    
    public void updateSite() throws Exception
    {
        clean();
        dist();
        File updateSite = new File("update-site");
        Utilities.delete(updateSite, true);
        updateSite.mkdir();
        runJava("de.exware.update.SiteTool", DISTRIBUTION_DIR, updateSite.getName(), TMP);
    }

    protected void cleanupNobuto(File dir) throws IOException
    {
        Utilities.delete(dir + "/nobuto.jar");
        Utilities.delete(dir + "/Build.class");
        Utilities.delete(dir + "/exware/AbstractExwareBuilder.class");
        Utilities.delete(dir + "/exware/AbstractExwareBuilder$1.class");
        Utilities.delete(dir + "/exware/AbstractExwareBuilder$2.class");
        Utilities.delete(dir + "/exware/ExwareDependency.class");
        Utilities.delete(dir + "/exware/ExwareDependency$1.class");
    }

}
