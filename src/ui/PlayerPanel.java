package ui;

import com.intellij.ui.JBColor;
import com.zl.network.HttpClient;
import com.zl.player.MusicPlayer;
import com.zl.pojo.TrackModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 播放器面板
 *
 * @author 郑龙
 * @date 2020/10/10 10:27
 */
public class PlayerPanel extends JPanel implements MouseListener {
    /**
     * 掌握的父窗口实例
     */
    private PluginToolWindow mainWindowInstance;

    /**
     * MusicPlayer
     */
    public static MusicPlayer musicPlayer;

    /**
     * 播放列表
     */
    private List<TrackModel> trackList;

    /**
     * 当前播放音频的序号
     */
    private int currentTrackOnPlayIndex;

    /**
     * 定义长宽
     */
    private int width, height;

    /**
     * 按钮
     */
    public static JButton btnLast, btnNext, btnPlayOrPause, btnStop, btnGoToPage;

    /**
     * 标题/状态
     */
    public static JLabel title;

    /**
     * 页码输入框
     */
    public static JTextField pageField;

    /**
     * 进度条
     */
    private JProgressBar progressBar;

    /**
     * 播放时间标签
     */
    private JLabel timeLabel;

    /**
     * 播放状态
     */
    private boolean isPause = true;

    public List<TrackModel> getTrackList() {
        return trackList;
    }

    public void setCurrentTrackOnPlayIndex(int currentTrackOnPlayIndex) {
        this.currentTrackOnPlayIndex = currentTrackOnPlayIndex;
    }

    public PlayerPanel(PluginToolWindow instance, int width, int height) {
        this.mainWindowInstance = instance;
        this.width = width;
        this.height = height;
        musicPlayer = MusicPlayer.getInstancePlayer();
        trackList = new ArrayList<>();
        currentTrackOnPlayIndex = 0;
        initUI();
    }

    /**
     * 初始化UI
     */
    public void initUI() {
        //标题
        title = new JLabel("未播放");
        title.setFont(new Font("字体", Font.BOLD, 24));

        //上一曲
        btnLast = new JButton("⏮");
        btnLast.setPreferredSize(new Dimension(40, 40));
        btnLast.addMouseListener(this);

        //下一曲
        btnNext = new JButton("⏭");
        btnNext.setPreferredSize(new Dimension(40, 40));
        btnNext.addMouseListener(this);

        //暂停或播放
        btnPlayOrPause = new JButton("▶");
        btnPlayOrPause.setPreferredSize(new Dimension(40, 40));
        btnPlayOrPause.addMouseListener(this);

        //停止
        btnStop = new JButton("⏹");
        btnStop.setPreferredSize(new Dimension(40, 40));
        btnStop.addMouseListener(this);

        //进度条
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setPreferredSize(new Dimension(300, 5));

        //音频时间
        timeLabel = new JLabel("00:00");

        //页码输入框
        pageField = new JTextField("1");
        //页码按钮
        btnGoToPage = new JButton("Go");
        btnGoToPage.setPreferredSize(new Dimension(60, 30));
        btnGoToPage.addMouseListener(this);

        this.setLayout(new BorderLayout(10, 5));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(btnLast);
        panel.add(btnPlayOrPause);
        panel.add(btnStop);
        panel.add(btnNext);
        panel.add(new JLabel("页码:"));
        panel.add(pageField);
        panel.add(btnGoToPage);

        //播放进度条
        JPanel processBarPanel = new JPanel();
        processBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        processBarPanel.add(progressBar);
        //添加音频时间label
        processBarPanel.add(timeLabel);

        this.add(title, BorderLayout.NORTH);
        this.add(panel, BorderLayout.CENTER);
        this.add(processBarPanel, BorderLayout.SOUTH);
        this.setSize(width, height);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (btnPlayOrPause.equals(e.getComponent())) {
            isPause = !isPause;
            if (isPause) {
                btnPlayOrPause.setText("⏸");
                musicPlayer.play();
            } else {
                btnPlayOrPause.setText("▶");
                musicPlayer.pause();
            }
        } else if (btnStop.equals(e.getComponent())) {
            btnPlayOrPause.setText("▶");
            musicPlayer.stop();
        } else if (btnLast.equals(e.getComponent())) {
            //获得播放序号
            mainWindowInstance.playOrDownload(trackList.get(Optional.of(currentTrackOnPlayIndex - 1)
                    .filter(i -> i > 0).orElse(0)));
            currentTrackOnPlayIndex = Math.max(currentTrackOnPlayIndex - 1, 0);
        } else if (btnNext.equals(e.getComponent())) {
            mainWindowInstance.playOrDownload(trackList.get(Optional.of(currentTrackOnPlayIndex + 1)
                    .filter(i -> i < trackList.size())
                    .orElse(trackList.size())));
            currentTrackOnPlayIndex = Math.min(currentTrackOnPlayIndex + 1, trackList.size());
        } else if (btnGoToPage.equals(e.getComponent())) {
            new Thread(() -> {
                try {
                    mainWindowInstance.getPlayListModel().clear();
                    mainWindowInstance.getTrackModelList().clear();
                    HttpClient.doSearch(Optional.ofNullable(pageField.getText()).map(s -> {
                        try {
                            int page = Integer.parseInt(s);
                            if (page <= 0) {
                                pageField.setText("1");
                                return 1;
                            } else {
                                pageField.setText(String.valueOf(page));
                                return page;
                            }

                        } catch (NumberFormatException numberFormatException) {
                            //输入不合法,默认到第一页
                            pageField.setText("1");
                        }
                        return 1;
                    }).orElse(1)).forEach(albumModel -> mainWindowInstance.getPlayListModel()
                            .addElement(albumModel));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
