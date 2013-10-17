package de.janthomae.leiningenplugin.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.janthomae.leiningenplugin.module.ModuleCreationUtils;
import de.janthomae.leiningenplugin.utils.ClassPathUtils;
import de.janthomae.leiningenplugin.utils.Interop;

import java.util.Map;

/**
 * Representation of Leiningen project in this plugin.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Vladimir Matveev
 * @version $Id:$
 */
public class LeiningenProject {
    private final VirtualFile leinProjectFile;

    private String name;
    private String namespace;
    private String version;

    public static LeiningenProject create(VirtualFile leinProjectFile) {
        return new LeiningenProject(leinProjectFile);
    }

    private LeiningenProject(VirtualFile leinProjectFile) {
        this.leinProjectFile = leinProjectFile;
    }

    public String getWorkingDir() {
        return leinProjectFile.getParent().getPath();
    }


    public VirtualFile getVirtualFile() {
        return leinProjectFile;
    }

    public static String[] nameAndVersionFromProjectFile(VirtualFile projectFile) {
        ClassPathUtils.getInstance().switchToPluginClassLoader();
        Map map = Interop.loadProject(projectFile.getPath());
        return new String[]{(String) map.get(ModuleCreationUtils.LEIN_PROJECT_NAME),
                (String) map.get(ModuleCreationUtils.LEIN_PROJECT_VERSION)};
    }

    public String getDisplayName() {
        return (namespace != null ? namespace + "/" : "") + name + (version != null ? ":" + version : "");
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof LeiningenProject &&
                ((LeiningenProject) obj).leinProjectFile.equals(leinProjectFile);
    }

    @Override
    public int hashCode() {
        return leinProjectFile.getPath().hashCode();
    }

    /**
     * Re-import the leiningen project.
     * <p/>
     * This will refresh the leiningen module associated with this project.
     * This should be run in the background - caller's responsibility.
     *
     * @param ideaProject The idea project
     * @throws LeiningenProjectException
     */
    public void reimport(final Project ideaProject) throws LeiningenProjectException {
        //Reload the lein project file
        ModuleCreationUtils mcu = new ModuleCreationUtils();

        //Update the module - eventually we can have multiple modules here that the project maintains.
        Map result = mcu.importModule(ideaProject, leinProjectFile);

        name = (String) result.get(ModuleCreationUtils.LEIN_PROJECT_NAME);
        namespace = (String) result.get(ModuleCreationUtils.LEIN_PROJECT_GROUP);
        version = (String) result.get(ModuleCreationUtils.LEIN_PROJECT_VERSION);
    }
}
