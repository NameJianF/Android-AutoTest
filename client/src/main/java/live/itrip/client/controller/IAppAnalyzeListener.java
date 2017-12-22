package live.itrip.client.controller;

import live.itrip.client.bean.apk.ApkInfo;

/**
 * Created by IntelliJ IDEA.
 * Description: app 解析监听
 *
 * @author JianF
 * Date:  2017/11/22
 * Time:  10:40
 * Modify:
 */
public interface IAppAnalyzeListener {
    /**
     * 显示到主界面
     *
     * @param apkInfo apk info
     */
    void showToWindow(ApkInfo apkInfo);
}
