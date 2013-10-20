package de.janthomae.leiningenplugin.navigator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.janthomae.leiningenplugin.LeiningenUtil;
import de.janthomae.leiningenplugin.module.ModuleCreationUtils;
import de.janthomae.leiningenplugin.project.LeiningenProject;
import de.janthomae.leiningenplugin.project.LeiningenProjectException;
import de.janthomae.leiningenplugin.project.LeiningenProjectsManager;

import java.util.List;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class RefreshProjectsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project theProject = e.getData(PlatformDataKeys.PROJECT);
        final LeiningenProjectsManager manager =
                LeiningenProjectsManager.getInstance(theProject);

        final List<LeiningenProject> projects = manager.getLeiningenProjects();
        LeiningenUtil.runInBackground(theProject, new Runnable() {
            @Override
            public void run() {
                for (LeiningenProject project : projects) {
                    VirtualFile projectFile = project.getVirtualFile();
                    if (ModuleCreationUtils.validateModule(theProject, projectFile)) {
                        try {
                            project.reimport(theProject);
                        } catch (LeiningenProjectException ignore) {
                            // Just ignore it for now
                        }
                    } else {
                        ModuleCreationUtils.tidyDependencies(theProject, projectFile, false);
                        manager.removeLeiningenProject(project);
                    }
                }
            }
        });
    }
}