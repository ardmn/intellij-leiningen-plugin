package de.janthomae.leiningenplugin.module.forms;

import de.janthomae.leiningenplugin.project.LeiningenProject;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Chris Shellenbarger
 * Date: 4/14/13
 * Time: 6:08 PM
 *
 * This is the java class that backs the LeiningenModuleInformationForm
 */
public class LeiningenModuleInformationForm {
    private JPanel mainPanel;
    private JTextField groupNameTextField;
    private JTextField artifactTextField;
    private JTextField versionTextField;
    private JTextField projectFileTextField;

    /**
     * Get the main panel created by this form.
     *
     * @return The main panel created by this form.
     */
    public JPanel getMainPanel() {
        groupNameTextField.setEditable(false);
        artifactTextField.setEditable(false);
        versionTextField.setEditable(false);
        projectFileTextField.setEditable(false);

        return mainPanel;
    }

    public void setData(LeiningenProject data) {
        groupNameTextField.setText(data.getGroup());
        artifactTextField.setText(data.getName());
        versionTextField.setText(data.getVersion());
        projectFileTextField.setText(data.getVirtualFile().getPath());
    }
}
