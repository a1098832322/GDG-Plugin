package listener;

import com.zl.network.CallBack;
import ws.schild.jave.EncoderProgressListener;
import ws.schild.jave.MultimediaInfo;

/**
 * MP3转码监听器
 *
 * @author 郑龙
 * @date 2020/11/5 14:56
 */
public class ConvertProgressListener implements EncoderProgressListener {
    private CallBack<Double, Double> callBack;

    public ConvertProgressListener(CallBack<Double, Double> callBack) {
        this.callBack = callBack;
    }

    @Override
    public void sourceInfo(MultimediaInfo multimediaInfo) {
        //分析完源文件后会回调此方法
    }

    @Override
    public void progress(int i) {
        //转码进程
        double progress = i / 10.00;
        callBack.callback(progress);
    }

    @Override
    public void message(String s) {
        //编码器调用该方法以通知关于代码转换操作的消息（通常消息是警告）
    }
}
