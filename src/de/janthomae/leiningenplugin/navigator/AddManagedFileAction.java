package de.janthomae.leiningenplugin.navigator;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.janthomae.leiningenplugin.project.LeiningenProjectsManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * @author Colin Fleming
 */
public class AddManagedFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final DataContext context = e.getDataContext();
        final Project ideaProject = PlatformDataKeys.PROJECT.getData(context);
        VirtualFile selectedFile = getSelectedFile(context);
        final LeiningenProjectsManager manager = LeiningenProjectsManager.getInstance(ideaProject);
        manager.importLeiningenProjects(Collections.singleton(selectedFile), ideaProject);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation p = e.getPresentation();
        boolean available = isAvailable(e);
        p.setEnabled(available);
        p.setVisible(available);
    }

    protected boolean isAvailable(AnActionEvent e) {
        final DataContext context = e.getDataContext();
        VirtualFile file = getSelectedFile(context);
        return  PlatformDataKeys.PROJECT.getData(context) != null
                && LeiningenProjectsManager.isProjectFile(file)
                && !isExistingProjectFile(context, file);
    }

    private static boolean isExistingProjectFile(DataContext context, VirtualFile file) {
        final Project ideaProject = PlatformDataKeys.PROJECT.getData(context);
        final LeiningenProjectsManager manager = LeiningenProjectsManager.getInstance(ideaProject);
        return manager.getProjectByProjectFile(file) != null;
    }

    @Nullable
    private static VirtualFile getSelectedFile(DataContext context) {
        return PlatformDataKeys.VIRTUAL_FILE.getData(context);
    }
}
