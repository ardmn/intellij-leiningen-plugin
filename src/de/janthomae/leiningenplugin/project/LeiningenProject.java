package de.janthomae.leiningenplugin.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.janthomae.leiningenplugin.module.ModuleCreationUtils;
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
    private final VirtualFile projectFile;
    private Map projectMap;
    private String name;
    private String group;
    private String version;

    public static LeiningenProject create(VirtualFile projectFile) {
        return new LeiningenProject(projectFile);
    }

    private LeiningenProject(VirtualFile projectFile) {
        this.projectFile = projectFile;
        reload();
    }

    private void reload() {
        projectMap = Interop.loadProject(projectFile.getPath());
        name = (String) projectMap.get(ModuleCreationUtils.LEIN_PROJECT_NAME);
        group = (String) projectMap.get(ModuleCreationUtils.LEIN_PROJECT_GROUP);
        version = (String) projectMap.get(ModuleCreationUtils.LEIN_PROJECT_VERSION);
    }

    public VirtualFile getWorkingDir() {
        return projectFile.getParent();
    }


    public VirtualFile getVirtualFile() {
        return projectFile;
    }

    public String getDisplayName() {
        return (group != null && !group.equals(name) ? group + "/" : "") + name + (version != null ? ":" + version : "");
    }

    public Map getProjectMap() {
        return projectMap;
    }

    public VirtualFile getProjectFile() {
        return projectFile;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof LeiningenProject &&
                ((LeiningenProject) obj).projectFile.equals(projectFile);
    }

    @Override
    public int hashCode() {
        return projectFile.getPath().hashCode();
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
        reload();
        mcu.importModule(ideaProject, this);
    }
}
