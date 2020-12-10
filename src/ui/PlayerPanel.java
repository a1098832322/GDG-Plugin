package ui;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.model.Track;
import com.tulskiy.musique.model.TrackData;
import com.tulskiy.musique.util.Util;
import com.zl.network.HttpClient;
import com.zl.player.MusicPlayer;
import components.AppSettingsState;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
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
    private JSlider progressSlider;

    /**
     * 播放时间标签
     */
    private JLabel timeLabel;

    /**
     * 播放状态
     */
    private boolean isPause = true;

    /**
     * 进度条启用状态
     */
    private boolean progressEnabled = false;

    /**
     * 是否正在拖动进度条
     */
    private boolean isSeeking = false;

    /**
     * 定时器
     */
    private Timer timer;

    /**
     * 播放列表长度
     */
    private int trackListSize = 0;

    public void setTrackListSize(int trackListSize) {
        this.trackListSize = trackListSize;
    }

    public void setCurrentTrackOnPlayIndex(int currentTrackOnPlayIndex) {
        this.currentTrackOnPlayIndex = currentTrackOnPlayIndex;
    }

    public PlayerPanel(PluginToolWindow instance, int width, int height) {
        this.mainWindowInstance = instance;
        this.width = width;
        this.height = height;
        timer = new Timer(1000, e -> {
            if (progressEnabled && musicPlayer.getCurrentPlayer().isPlaying()) {
                progressSlider.setValue((int) musicPlayer.getCurrentPlayer().getCurrentSample());
            }
            if (musicPlayer.getCurrentPlayer().isPlaying() && !isSeeking) {
                updateTimeLabel();
            }
        });
        timer.start();

        musicPlayer = MusicPlayer.getInstancePlayer();
        //添加监听器
        musicPlayer.addListener(e -> {
            Track track = musicPlayer.getCurrentPlayer().getTrack();

            switch (e.getEventCode()) {
                case PLAYING_STARTED:
                    timer.start();
                    break;
                case PAUSED:
                    timer.stop();
                    break;
                case STOPPED:
                    timer.stop();
                    progressEnabled = false;
                    timeLabel.setText("-:--");

                    //判断是用户停止还是播放完毕
                    if (track.getTrackData().getTotalSamples() == progressSlider.getMaximum()) {
                        //是播放完毕，则尝试自动播放下一首
                        currentTrackOnPlayIndex = Math.min(currentTrackOnPlayIndex + 1, trackListSize);
                        mainWindowInstance.play(currentTrackOnPlayIndex);

                    }

                    progressSlider.setValue(progressSlider.getMinimum());
                    break;
                case FILE_OPENED:
                    if (track != null) {
                        int max = (int) track.getTrackData().getTotalSamples();
                        if (max == -1) {
                            progressEnabled = false;
                        } else {
                            progressEnabled = true;
                            progressSlider.setMaximum(max);
                        }
                    }
                    progressSlider.setValue((int) musicPlayer.getCurrentPlayer().getCurrentSample());
                    updateTimeLabel();
                    break;
                case SEEK_FINISHED:
                    //跳播放进度结束
                    isSeeking = false;
                    break;
            }
        });
        currentTrackOnPlayIndex = 0;
        initUI();
    }

    /**
     * 更新时间标记
     */
    private void updateTimeLabel() {
        Player player = musicPlayer.getCurrentPlayer();
        updateTimeLabel(player.getCurrentSample(), player);
    }

    /**
     * 根据当前时间更新时间标记
     *
     * @param currentSample 当前时间
     */
    private void updateTimeLabel(long currentSample) {
        Player player = musicPlayer.getCurrentPlayer();
        updateTimeLabel(currentSample, player);
    }

    /**
     * 根据当前时间更新时间标记
     *
     * @param currentSample 当前时间
     * @param player        音乐播放器实例
     */
    private void updateTimeLabel(long currentSample, Player player) {
        if (player.getTrack() != null) {
            TrackData trackData = player.getTrack().getTrackData();
            timeLabel.setText(Util.samplesToTime(currentSample,
                    player.getTrack().getTrackData().getSampleRate(), 0) + "/" + trackData.getLength());
        }
    }

    /**
     * 获得X轴滑动值
     *
     * @param slider 滑动控件
     * @param x      X轴值
     * @return X轴差值
     */
    private int getSliderValueForX(JSlider slider, int x) {
        return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
    }

    /**
     * 初始化UI
     */
    public void initUI() {
        //标题
        title = new JLabel(" 未播放");
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
        progressSlider = new JSlider(SwingConstants.HORIZONTAL);
        progressSlider.setPreferredSize(new Dimension(300, 5));
        progressSlider.setValue(0);
        progressSlider.setFocusable(false);
        //监听鼠标事件
        progressSlider.addMouseListener(new MouseAdapter() {
            //鼠标释放事件
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!progressEnabled) {
                    return;
                }
                musicPlayer.getCurrentPlayer().seek(progressSlider.getValue());
            }

            //鼠标按下事件
            @Override
            public void mousePressed(MouseEvent e) {
                if (!progressEnabled) {
                    return;
                }
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                //修改时间标签显示
                updateTimeLabel(progressSlider.getValue());
            }
        });
        //鼠标拖动事件
        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!progressEnabled) {
                    return;
                }
                //修改标记，表示正在拖动进度条
                isSeeking = true;
                //修改进度条值
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                //修改时间标签显示
                updateTimeLabel(progressSlider.getValue());

            }
        });

        //音频时间
        timeLabel = new JLabel("-:--");

        //页码输入框
        pageField = new JTextField("1");
        //页码按钮
        btnGoToPage = new JButton("Go");
        btnGoToPage.setPreferredSize(new Dimension(60, 30));
        btnGoToPage.addMouseListener(this);

        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
        processBarPanel.add(progressSlider);
        //添加音频时间label
        processBarPanel.add(timeLabel);

        this.add(title);
        this.add(panel);
        this.add(processBarPanel);
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
            currentTrackOnPlayIndex = Math.max(currentTrackOnPlayIndex - 1, -1);
            mainWindowInstance.play(currentTrackOnPlayIndex);
        } else if (btnNext.equals(e.getComponent())) {
            currentTrackOnPlayIndex = Math.min(currentTrackOnPlayIndex + 1, trackListSize);
            mainWindowInstance.play(currentTrackOnPlayIndex);
        } else if (btnGoToPage.equals(e.getComponent())) {
            new Thread(() -> {
                try {
                    //锁定艺术家信息框
                    ArtistPanel.lockArtistEditTextField();
                    //清空列表数据
                    mainWindowInstance.getPlayListModel().clear();
                    mainWindowInstance.getTrackModelList().clear();
                    HttpClient.doSearch(AppSettingsState.getInstance().keyword, Optional.ofNullable(pageField.getText()).map(s -> {
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
                    //解锁输入框
                    ArtistPanel.unlockArtistEditTextField();
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
