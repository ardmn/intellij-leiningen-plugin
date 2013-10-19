package de.janthomae.leiningenplugin.navigator;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import de.janthomae.leiningenplugin.LeiningenDataKeys;
import de.janthomae.leiningenplugin.LeiningenUtil;
import de.janthomae.leiningenplugin.module.ModuleCreationUtils;
import de.janthomae.leiningenplugin.project.LeiningenProject;
import de.janthomae.leiningenplugin.project.LeiningenProjectsManager;

/**
 * Action to add a leiningen project file to the IDEA project.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class RemoveManagedFilesAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final DataContext context = e.getDataContext();
        final Project ideaProject = PlatformDataKeys.PROJECT.getData(context);
        final LeiningenProjectsManager manager = LeiningenProjectsManager.getInstance(ideaProject);
        LeiningenProject leiningenProject = e.getData(LeiningenDataKeys.LEININGEN_PROJECT);
        if (leiningenProject != null) {
            manager.removeLeiningenProject(leiningenProject);

            Module module = ModuleCreationUtils.findModule(ideaProject, leiningenProject.getProjectFile());
            if (module != null) {
                final int[] result = new int[1];
                LeiningenUtil.invokeAndWait(ideaProject, ModalityState.defaultModalityState(), new Runnable() {
                    public void run() {
                        result[0] = Messages.showYesNoDialog(ideaProject,
                                "Would you also like to remove the module from the project?",
                                "Delete module?",
                                Messages.getQuestionIcon());
                    }
                });

                if (result[0] == DialogWrapper.CANCEL_EXIT_CODE) return;

                final ModifiableModuleModel moduleManager = ModuleCreationUtils.createModuleManager(ideaProject);
                moduleManager.disposeModule(module);
                new WriteAction() {
                    @Override
                    protected void run(Result result) throws Throwable {
                        moduleManager.commit();
                    }
                }.execute();
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation p = e.getPresentation();
        p.setEnabled(e.getData(LeiningenDataKeys.LEININGEN_PROJECT) != null);
    }
}
