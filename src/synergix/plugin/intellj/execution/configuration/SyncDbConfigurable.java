package synergix.plugin.intellj.execution.configuration;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBLabel;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Arrays;
import java.util.List;

public class SyncDbConfigurable extends SettingsEditor<SyncDbConfiguration> implements PanelWithAnchor {
    private SyncDbConfiguration syncDbConfiguration;
    private Project myProject;
    private JPanel myWholePanel;
    private JComponent anchor;
    private LabeledComponent<ComponentWithBrowseButton> myMainClass;

    private JPanel mySuperModelDistOption;
    private JBLabel mySuperModelDistDirectoryLabel;
    private TextFieldWithBrowseButton mySuperModelDistDirectory;

    private JPanel mySuperModelStableOption;
    private JBLabel mySuperModelStableDirectoryLabel;
    private TextFieldWithBrowseButton mySuperModelStableDirectory;

    private JPanel dbCommandOption;
    private LabeledComponent<JTextField> myCommandOption;
    private LabeledComponent<JTextField> myDbListLabeledComponent;
    private LabeledComponent<JTextField> mySchemaOption;
    private JPanel mySyncActionPanel;
    private JPanel myDbListPanel;

    private JPanel dbNamesOption;

    public SyncDbConfigurable(final Project project) {
        this.myProject = project;
        mySuperModelDistDirectoryLabel.setLabelFor(mySuperModelDistDirectory.getTextField());
        mySuperModelStableDirectoryLabel.setLabelFor(mySuperModelStableDirectory.getTextField());

        mySuperModelDistDirectory.addBrowseFolderListener("Choose Super Model's Dist Directory", null, null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());

        mySuperModelStableDirectory.addBrowseFolderListener("Choose Super Model's Stable Directory", null, null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());

        myCommandOption.getComponent().setText("sync");
        mySchemaOption.getComponent().setText("modmain");
        mySuperModelDistDirectory.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStableDirectory();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStableDirectory();
            }
        });

        mySuperModelStableDirectory.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDistDirectory();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDistDirectory();
            }
        });
    }

    @Override
    protected void resetEditorFrom(@NotNull SyncDbConfiguration s) {
        this.syncDbConfiguration = s;
        this.mySuperModelDistDirectory.setText(s.getSuperModelDistDirectory());
        this.mySuperModelStableDirectory.setText(s.getSuperModelStableDirectory());
        this.myCommandOption.getComponent().setText(s.getDbCommand());
        this.mySchemaOption.getComponent().setText(s.getDbSchema());
        this.myDbListLabeledComponent.getComponent().setText(String.join(",", s.getDbNames().toArray(new String[s.getDbNames().size()])));
    }

    @Override
    protected void applyEditorTo(@NotNull SyncDbConfiguration s) throws ConfigurationException {
        s.setSuperModelDistDirectory(mySuperModelDistDirectory.getText());
        s.setSuperModelStableDirectory(mySuperModelStableDirectory.getText());
        s.setDbCommand(myCommandOption.getComponent().getText());
        s.setDbSchema(mySchemaOption.getComponent().getText());

        String dbListText = myDbListLabeledComponent.getComponent().getText();
        if (!StringUtils.isEmpty(dbListText)) {
            s.setDbNames(Arrays.asList(dbListText.split(",")));
        }
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myWholePanel;
    }

    @Override
    public JComponent getAnchor() {
        return null;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        this.anchor = anchor;
        this.mySuperModelDistDirectoryLabel.setAnchor(anchor);
        this.mySuperModelStableDirectoryLabel.setAnchor(anchor);
    }

    private void updateStableDirectory() {
        if (StringUtils.isEmpty(mySuperModelStableDirectory.getText())) {
            String distPath = mySuperModelDistDirectory.getText();
            int index = FilenameUtils.indexOfLastSeparator(distPath);
            mySuperModelStableDirectory.setText(distPath.substring(0, index + 1) + "stable");
        }
    }

    private void updateDistDirectory() {
        if (StringUtils.isEmpty(mySuperModelDistDirectory.getText())) {
            String stablePath = mySuperModelStableDirectory.getText();
            int index = FilenameUtils.indexOfLastSeparator(stablePath);
            mySuperModelDistDirectory.setText(stablePath.substring(0, index + 1) + "dist");
        }
    }
}
