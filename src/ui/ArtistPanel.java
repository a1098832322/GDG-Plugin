package ui;

import components.AppSettingsState;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * 艺术家切换面板
 *
 * @author 郑龙
 * @date 2020/12/10 11:04
 */
public class ArtistPanel extends JPanel {
    /**
     * 当前表演艺术家/关键词
     */
    private String currentArtist;

    /**
     * 提示框
     */
    private JLabel label;

    /**
     * 输入框
     */
    public static JTextField textField;

    public ArtistPanel(int width, int height) {
        this.currentArtist = AppSettingsState.getInstance().keyword;
        initUI();
    }

    public ArtistPanel(String artist, int width, int height) {
        this.currentArtist = StringUtils.isBlank(artist) ? AppSettingsState.getInstance().keyword : artist;
        initUI();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel("当前艺术家:");
        label.setPreferredSize(new Dimension(80, 40));
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(290, 30));
        textField.setText(currentArtist);
        this.add(label);
        this.add(textField);
    }
}
