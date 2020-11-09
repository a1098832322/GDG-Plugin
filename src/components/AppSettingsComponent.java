package components;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author 郑龙
 * @date 2020/11/6 9:03
 */
public class AppSettingsComponent {


    /**
     * Supports creating and managing a {@link JPanel} for the Settings Dialog.
     */


    private final JPanel myMainPanel;
    private final JBTextField myStoragePath = new JBTextField();

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("相声音频文件保存路径:"), myStoragePath, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return myStoragePath;
    }

    public String getMyStoragePathText() {
        return myStoragePath.getText();
    }

    public void setMyStoragePathText(@NotNull String newText) {
        myStoragePath.setText(newText);
    }

}
