package components;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import components.AppSettingsComponent;
import components.AppSettingsState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 主设置窗口
 *
 * @author 郑龙
 * @date 2020/11/6 9:00
 */
public class AppSettingConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    @Override
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getDisplayName() {
        return "郭德纲相声插件设置";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        return !mySettingsComponent.getMyStoragePathText().equals(settings.storagePath);
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.storagePath = mySettingsComponent.getMyStoragePathText();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setMyStoragePathText(settings.storagePath);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
