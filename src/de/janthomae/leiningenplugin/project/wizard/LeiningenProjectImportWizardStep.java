package de.janthomae.leiningenplugin.project.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportWizardStep;
import de.janthomae.leiningenplugin.module.forms.LeiningenModuleInformationForm;
import de.janthomae.leiningenplugin.project.LeiningenProject;
import de.janthomae.leiningenplugin.project.LeiningenProjectBuilder;

import javax.swing.*;


/**
 * Created with IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 4/14/13
 * Time: 4:33 PM
 * <p/>
 * The first page in the 'Import Project' workflow.  After the user chooses the project.clj file location.
 */
public class LeiningenProjectImportWizardStep extends ProjectImportWizardStep {

    private final VirtualFile projectFile;
    private LeiningenModuleInformationForm moduleInformationForm;
    private final LeiningenProject leiningenProject;

    /**
     * Initialize the wizard step with wizard context and the path of the project.clj file.
     *
     * @param context The wizard context.
     * @param projectFile Absolute path to the project.clj file to import.
     */
    public LeiningenProjectImportWizardStep(WizardContext context, String projectFile) {
        super(context);
        this.projectFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(projectFile);
        leiningenProject = LeiningenProject.create(this.projectFile);

        moduleInformationForm = new LeiningenModuleInformationForm();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return true;
    }

    @Override
    public void updateStep() {
        moduleInformationForm.setData(leiningenProject);
    }

    @Override
    public JComponent getComponent() {
        return moduleInformationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        //Tell the builder where the projectFile is.
        getBuilder().setProjectFile(leiningenProject.getProjectFile());

        //Point to the parent directory so we can create the .idea directory.
        String parentDir = FileUtil.toSystemDependentName(leiningenProject.getWorkingDir().getCanonicalPath());
        getWizardContext().setProjectFileDirectory(parentDir);
    }

    @Override
    protected LeiningenProjectBuilder getBuilder() {
        return (LeiningenProjectBuilder) super.getBuilder();
    }
}
