package ui;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zl.audio.ConvertingAnyAudioToMp3Sync;
import com.zl.network.DownloadListener;
import com.zl.network.HttpClient;
import com.zl.player.MusicPlayer;
import com.zl.pojo.AlbumModel;
import com.zl.pojo.TrackModel;
import com.zl.util.FileUtil;
import components.AppSettingsState;
import components.AutoPlay;
import listener.ConvertProgressListener;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 创建侧边栏
 *
 * @author 郑龙
 * @date 2020/10/10 10:00
 */
public class PluginToolWindow extends MouseAdapter implements ToolWindowFactory, AutoPlay {
    /**
     * 双击事件
     */
    private static final int DOUBLE_CLICK = 2;

    /**
     * 主窗体
     */
    private JPanel mainPanel;

    /**
     * 艺术家控件
     */
    private ArtistPanel artistPanel;

    /**
     * 播放器控件窗口
     */
    private PlayerPanel playerPanel;

    /**
     * 滚动窗体
     */
    private JScrollPane playListScrollPane, musicScrollPane;

    /**
     * 音频列表
     */
    public JList<TrackModel> musicList;

    /**
     * 专辑列表
     */
    public JList<AlbumModel> playList;

    /**
     * list数据对象
     */
    private DefaultListModel<AlbumModel> playListModel;

    /**
     * 音频列表
     */
    private DefaultListModel<TrackModel> trackModelList;

    public DefaultListModel<AlbumModel> getPlayListModel() {
        return playListModel;
    }

    public DefaultListModel<TrackModel> getTrackModelList() {
        return trackModelList;
    }

    public PluginToolWindow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        artistPanel = new ArtistPanel(400, 50);
        artistPanel.setPreferredSize(new Dimension(400, 50));
        mainPanel = new JPanel(new BorderLayout(10, 5));
        playerPanel = new PlayerPanel(this, 400, 200);
        playerPanel.setPreferredSize(new Dimension(400, 160));

        //初始化两个列表
        initScrollPaneView(screen);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        headerPanel.add(artistPanel);
        headerPanel.add(playerPanel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        listPanel.add(playListScrollPane);
        listPanel.add(musicScrollPane);
        listPanel.setSize(500, 400);

        mainPanel.add(listPanel, BorderLayout.CENTER);
    }

    /**
     * 初始化两个滚动列表
     *
     * @param screen 屏幕尺寸信息
     */
    private void initScrollPaneView(Dimension screen) {
        playListScrollPane = new JScrollPane();
        playListScrollPane.setPreferredSize(new Dimension(200, 400));
        musicScrollPane = new JScrollPane();
        musicScrollPane.setPreferredSize(new Dimension(200, 400));

        playListModel = new DefaultListModel<>();
        trackModelList = new DefaultListModel<>();

        playList = new JList<>(playListModel);
        //设置list样式，元素居中
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        renderer.setSize(400, (int) (screen.getHeight() - 400));
        playList.setCellRenderer(renderer);
        //点击事件
        playList.addMouseListener(this);

        playListScrollPane.setViewportView(playList);

        musicList = new JList<>(trackModelList);
        //设置list样式，元素居中
        DefaultListCellRenderer musicListRender = new DefaultListCellRenderer();
        musicListRender.setHorizontalAlignment(SwingConstants.LEFT);
        musicListRender.setSize(400, (int) (screen.getHeight() - 400));
        musicList.setCellRenderer(musicListRender);
        //点击事件
        musicList.addMouseListener(this);

        musicScrollPane.setViewportView(musicList);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", true);
        toolWindow.getContentManager().addContent(content);

        playListModel.clear();
        new Thread(() -> {
            try {
                HttpClient.doSearch(AppSettingsState.getInstance().keyword, 1).forEach(albumModel -> playListModel.addElement(albumModel));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 打开或关闭所有控件
     *
     * @param rootPanel 控件根容器
     * @param isEnable  打开或关闭：false关闭，true打开
     */
    private void disableOrEnableAllComponent(JComponent rootPanel, boolean isEnable) {
        if (rootPanel != null) {
            for (Component component : rootPanel.getComponents()) {
                component.setEnabled(isEnable);
                if (isEnable) {
                    component.removeMouseListener(this);
                } else {
                    component.addMouseListener(this);
                }
            }
            rootPanel.setEnabled(false);
        }

    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (this.musicList.equals(evt.getComponent())) {
            if (evt.getClickCount() == DOUBLE_CLICK) {
                // 监听鼠标双击事件
                int index = ((JList<AlbumModel>) evt.getSource()).locationToIndex(evt.getPoint());
                //下载或播放
                playOrDownload(trackModelList.get(index));
                //保存播放序号
                playerPanel.setCurrentTrackOnPlayIndex(index);
            }
        } else if (this.playList.equals(evt.getComponent())) {
            // 监听鼠标点击事件
            int index = ((JList<AlbumModel>) evt.getSource()).locationToIndex(evt.getPoint());
            //获得元素
            AlbumModel entity = playListModel.get(index);
            //清空歌曲列表
            trackModelList.clear();

            //查播放列表详情
            try {
                List<TrackModel> musicList = HttpClient.queryAlbumDetailById(entity.getAlbumId());
                if (CollectionUtils.isNotEmpty(musicList)) {
                    //设置播放列表长度
                    playerPanel.setTrackListSize(musicList.size());
                    //添加进AWT列表
                    musicList.forEach(trackModel -> trackModelList.addElement(trackModel));
                } else {
                    //弹出提示框
                    Messages.showWarningDialog("该曲目可能为VIP曲目，无法白嫖播放!", "提示");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载或播放音频
     *
     * @param trackModel 音频对象实体
     */
    private void playOrDownload(TrackModel trackModel) {
        PlayerPanel.btnPlayOrPause.setText("▶");

        //不使用内置路径
//        String path = Optional.of(OSinfo.getOSname()).map(os -> {
//            System.out.println("OS:" + os);
//
//            if (OSinfo.isLinux()) {
//                return Storage.LINUX_PATH;
//            } else if (OSinfo.isMacOS() || OSinfo.isMacOSX()) {
//                return Storage.MAC_PATH;
//            } else {
//                //默认windows路径
//                return Storage.WINDOWS_PATH;
//            }
//        }).orElse(Storage.WINDOWS_PATH);

        //使用配置路径
        final String dirPath = AppSettingsState.getInstance().storagePath;

        if (FileUtil.judeDirExists(new File(dirPath))) {
            String downloadFileName = dirPath + trackModel.getTrackName() + ".m4a";
            String playFileName = dirPath + trackModel.getTrackName() + ".mp3";

            if (!FileUtil.contain(playFileName)) {
                //文件大小
                final double[] fileSize = {0.00};
                //不存在则下载
                HttpClient.download(trackModel.getSrc(), downloadFileName,
                        new DownloadListener() {
                            @Override
                            public void start(long max) {
                                fileSize[0] = (double) max / 1024;
                                PlayerPanel.title.setText(" 开始下载: " + trackModel.getTrackName());
                                //禁用所有
                                ArtistPanel.lockArtistEditTextField();
                                disableOrEnableAllComponent(playerPanel, false);
                                disableOrEnableAllComponent(mainPanel, false);
                                playList.setEnabled(false);
                                musicList.setEnabled(false);
                            }

                            @Override
                            public void loading(int progress) {
                                double percent = (progress / fileSize[0]) * 100;
                                PlayerPanel.title.setText(" 下载中: " + String.format("%.2f", percent) + "%");
                            }

                            @Override
                            public void complete(String path) {

                                PlayerPanel.title.setText(trackModel.getTrackName() + "下载完成!");

                                //M4A转码MP3
                                PlayerPanel.title.setText(" 开始转码");
                                ConvertingAnyAudioToMp3Sync.convertingAnyAudioToMp3WithAProgressListener(new File(path)
                                        , new File(playFileName), new ConvertProgressListener(process -> {
                                            PlayerPanel.title.setText(" 转码中: " + process + "%");
                                            return process;
                                        }));


                                PlayerPanel.title.setText(" " + trackModel.getTrackName());
                                //启用所有控件
                                disableOrEnableAllComponent(playerPanel, true);
                                disableOrEnableAllComponent(mainPanel, true);
                                playList.setEnabled(true);
                                musicList.setEnabled(true);

                                //修改播放按钮显示
                                PlayerPanel.btnPlayOrPause.setText("⏸");

                                MusicPlayer player = MusicPlayer.getInstancePlayer();
                                player.loadMusicSrc(playFileName);
                                player.openMusic();

                                //所有步骤完成后清理M4A缓存文件
                                FileUtil.cleanM4aCache(dirPath);
                            }

                            @Override
                            public void fail(int code, String message) {
                                System.out.println("下载失败！");
                                //启用所有控件
                                disableOrEnableAllComponent(playerPanel, true);
                                disableOrEnableAllComponent(mainPanel, true);
                                playList.setEnabled(true);
                                musicList.setEnabled(true);
                            }

                            @Override
                            public void loadFail(String message) {

                            }
                        }, 0L);
            } else {
                PlayerPanel.btnPlayOrPause.setText("⏸");
                //存在文件则直接播放
                com.zl.player.MusicPlayer player = MusicPlayer.getInstancePlayer();
                player.loadMusicSrc(playFileName);
                player.openMusic();
            }

            ArtistPanel.unlockArtistEditTextField();
            PlayerPanel.title.setText(" " + trackModel.getTrackName());

        }
    }

    @Override
    public void play(int index) {
        if (!trackModelList.isEmpty()) {
            if (index < trackModelList.size() && index >= 0) {
                //设置选中
                musicList.setSelectedIndex(index);
                //播放
                playOrDownload(trackModelList.get(index));
            }
        }
    }
}
