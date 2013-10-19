package de.janthomae.leiningenplugin.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.janthomae.leiningenplugin.LeiningenConstants;
import de.janthomae.leiningenplugin.LeiningenUtil;
import de.janthomae.leiningenplugin.SimpleProjectComponent;
import de.janthomae.leiningenplugin.module.ModuleCreationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
@State(name = "LeiningenProjectsManager", storages = {@Storage(id = "default", file = "$PROJECT_FILE$")})
public class LeiningenProjectsManager extends  SimpleProjectComponent implements PersistentStateComponent<LeiningenProjectsManagerState> {
    private List<LeiningenProject> leiningenProjects = new ArrayList<LeiningenProject>();
    private LeiningenProjectsManagerWatcher watcher;
    private List<LeiningenProjectsManagerListener> listeners = new ArrayList<LeiningenProjectsManagerListener>();

    public static final String LEIN_PROPERTY_NAME =
            "de.janthomae.leiningenplugin.project.LeiningenProjectsManager.isLeinModule";

    /**
     * Don't want to add a dependency on Maven just for this
     * @see MavenProjectsManager.isMavenizedModule
     */
    public static final String MAVEN_PROPERTY_NAME =
            "org.jetbrains.idea.maven.project.MavenProjectsManager.isMavenModule";

    public static LeiningenProjectsManager getInstance(Project p) {
        return p.getComponent(LeiningenProjectsManager.class);
    }

    /**
     * Determine if this file is a leiningen project file.
     *
     * @param file The virtual file.
     * @return True if it is a leiningen project file.
     */
    public static boolean isProjectFile(VirtualFile file) {
        return file != null && !file.isDirectory() && file.exists() &&
                file.getName().equals(LeiningenConstants.PROJECT_CLJ);
    }

    protected LeiningenProjectsManager(Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
        LeiningenUtil.runWhenInitialized(myProject, new Runnable() {
            public void run() {
                watcher = new LeiningenProjectsManagerWatcher(myProject, LeiningenProjectsManager.this);
                watcher.start();
            }
        });
    }

    public void addProjectsManagerListener(LeiningenProjectsManagerListener listener) {
        listeners.add(listener);
    }

    public LeiningenProject byPath(String path) {
        for (LeiningenProject leiningenProject : leiningenProjects) {
            if (leiningenProject.getVirtualFile().getPath().equals(path)) {
                return leiningenProject;
            }
        }
        return null;
    }

    public boolean hasProjects() {
        return !leiningenProjects.isEmpty();
    }

    /**
     * Import a new Leiningen project. This is called from the two Add Project actions, and from
     * the import project wizard.
     * @param projectFile the project.clj
     * @param project the IntelliJ project
     * @return
     */
    public List<Module> importLeiningenProjects(final Collection<VirtualFile> projectFiles, final Project project) {
        final List<Module> result = new ArrayList<Module>();

        LeiningenUtil.runInBackground(project, new Runnable() {
            @Override
            public void run() {
                try {
                    for (VirtualFile projectFile : projectFiles) {
                        if (ModuleCreationUtils.validateModule(project, projectFile)) {
                            LeiningenProject leiningenProject = getProjectByProjectFile(projectFile);
                            if (leiningenProject == null) {
                                leiningenProject = LeiningenProject.create(projectFile);
                                addLeiningenProject(leiningenProject);
                            }

                            /** Side effect - adds to the project's module list */
                            leiningenProject.reimport(project);

                            Module newModule = ModuleCreationUtils.findModule(project, projectFile);
                            if (newModule != null) {
                                result.add(newModule);
                            }
                        }
                    }
                } catch (LeiningenProjectException ignore) {
                    // Just do nothing for now
                }
            }
        });

        return result;
    }

    public boolean hasProject(LeiningenProject project) {
        return leiningenProjects.contains(project);
    }

    private void addLeiningenProject(LeiningenProject leiningenProject) {
        leiningenProjects.add(leiningenProject);
        notifyListeners();
    }

    /**
     * Finds the LeiningenProject whose project file is the passed file.
     *
     * @param file The virtual file to check.
     * @return the project if we're managing this file already, null if not.
     */
    public LeiningenProject getProjectByProjectFile(VirtualFile file) {
        for (LeiningenProject project : leiningenProjects) {
            if (project.getVirtualFile().equals(file)) {
                return project;
            }
        }
        return null;
    }

    public void removeProjectsManagerListener(LeiningenProjectsManagerListener listener) {
        listeners.remove(listener);
    }

    public List<LeiningenProject> getLeiningenProjects() {
        return new ArrayList<LeiningenProject>(leiningenProjects);
    }

//    private void findProjectFiles() {
//        leiningenProjects.clear();
//        VirtualFile projectFile = myProject.getBaseDir().findChild(LeiningenConstants.PROJECT_CLJ);
//        if (projectFile != null) {
//            addLeiningenProject(new LeiningenProject(projectFile, myProject));
//        }
//    }

    public void removeLeiningenProject(LeiningenProject leiningenProject) {
        leiningenProjects.remove(leiningenProject);
        notifyListeners();
    }

    private void notifyListeners() {
        for (LeiningenProjectsManagerListener listener : listeners) {
            listener.projectsChanged();
        }
    }

    public LeiningenProjectsManagerState getState() {
        LeiningenProjectsManagerState state = new LeiningenProjectsManagerState();
        for (LeiningenProject leiningenProject : leiningenProjects) {
            state.projectFiles.add(leiningenProject.getVirtualFile().getUrl());
        }
        return state;
    }

    public void loadState(final LeiningenProjectsManagerState leiningenProjectsManagerState) {
        LeiningenUtil.runWhenInitialized(myProject, new Runnable() {
            public void run() {
                LeiningenUtil.runInBackground(myProject, new Runnable() {
                    @Override
                    public void run() {
                        for (String projectFile : leiningenProjectsManagerState.projectFiles) {
                            try {
                                VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(projectFile);
                                Collection<LeiningenProject> toImport = new ArrayList<LeiningenProject>();
                                if (ModuleCreationUtils.validateModule(myProject, vf)) {
                                    LeiningenProject leiningenProject = LeiningenProject.create(vf);
                                    toImport.add(leiningenProject);
                                    addLeiningenProject(leiningenProject);
                                } else {
                                    ModuleCreationUtils.tidyDependencies(myProject, vf, false);
                                }
                                for (LeiningenProject leiningenProject : toImport) {
                                    leiningenProject.reimport(myProject);
                                }
                            } catch (LeiningenProjectException ignore) {
                                // Do nothing for now
                            }
                        }
                    }
                });
            }
        });
    }
}
