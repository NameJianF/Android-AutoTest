package live.itrip.client.device;

import javafx.scene.image.Image;
import live.itrip.client.bean.apk.ApkInfo;
import live.itrip.client.bean.apk.ImpliedFeature;
import live.itrip.client.service.DirectoryService;
import live.itrip.client.util.Logger;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author JianF
 */
public class AaptCmdExecutor {
    private static final String LAUNCHABLE_ACTIVITY = "launchable-activity";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String TARGET_SDK_VERSION = "targetSdkVersion";
    private static final String USES_PERMISSION = "uses-permission";
    private static final String APPLICATION_LABEL = "application-label";
    private static final String APPLICATION_ICON = "application-icon";
    private static final String USES_FEATURE = "uses-feature";
    private static final String USES_IMPLIED_FEATURE = "uses-implied-feature";
    //    public static final String SUPPORTS_SCREENS = "supports-screens";
    //    public static final String SUPPORTS_ANY_DENSITY = "supports-any-density";
    //    public static final String DENSITIES = "densities";
    private static final String PACKAGE = "package";
    private static final String APPLICATION = "application:";

    private ProcessBuilder mBuilder;
    private static final String SPLIT_REGEX = "(: )|(=')|(' )|'";
    private static final String FEATURE_SPLIT_REGEX = "(:')|(',')|'";

    private static AaptCmdExecutor instance;

    /**
     * 单例模式
     *
     * @return AaptCmdExecutor
     */
    public static AaptCmdExecutor getInstance() {
        if (instance == null) {
            synchronized (AaptCmdExecutor.class) {
                if (instance == null) {
                    instance = new AaptCmdExecutor();
                }
            }
        }

        return instance;
    }

    private AaptCmdExecutor() {
        mBuilder = new ProcessBuilder();
        mBuilder.redirectErrorStream(true);
    }


    /**
     * 返回一个apk程序的信息。
     *
     * @param apkPath apk的路径。
     * @return apkInfo 一个Apk的信息。
     */
    public ApkInfo getApkInfo(String apkPath) {
        Process process = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            String command = String.format("\"%s\" d badging \"%s\"",
                    DirectoryService.getAaptPath(), apkPath);
            Logger.debug(command);

            process = mBuilder.command(command).start();
            is = process.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "utf8"));
            String tmp = br.readLine();
            if (tmp == null || !tmp.startsWith(PACKAGE)) {
                throw new Exception("参数不正确，无法正常解析APK包。输出结果为:" + tmp + "...");
            }
            ApkInfo apkInfo = new ApkInfo();
            do {
                setApkInfoProperty(apkInfo, tmp);
            } while ((tmp = br.readLine()) != null);

            InputStream inputStream = getAppIcon(apkPath, apkInfo.getApplicationIcon());
            if (inputStream != null) {
                apkInfo.setIcon(new Image(inputStream));
            }

            return apkInfo;
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (is != null) {
                closeIO(is);
            }
            if (br != null) {
                closeIO(br);
            }
        }
        return null;
    }

    private InputStream getAppIcon(String apkpath, String iconName) {
        try {
            ZipFile zFile = new ZipFile(apkpath);
            ZipEntry entry = zFile.getEntry(iconName);
            entry.getComment();
            entry.getCompressedSize();
            entry.getCrc();
            entry.isDirectory();
            entry.getSize();
            entry.getMethod();
            return zFile.getInputStream(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置APK的属性信息。
     *
     * @param apkInfo apk info
     * @param source  source
     */
    private void setApkInfoProperty(ApkInfo apkInfo, String source) {
        if (source.startsWith(PACKAGE)) {
            splitPackageInfo(apkInfo, source);
        } else if (source.startsWith(SDK_VERSION)) {
            apkInfo.setSdkVersion(getPropertyInQuote(source));
        } else if (source.startsWith(LAUNCHABLE_ACTIVITY)) {
            // launchable-activity: name='com.example.android.apis.ApiDemos'  label='' icon=''
            String[] packageInfo = source.split(SPLIT_REGEX);
            apkInfo.setLaunchableActivity(packageInfo[2]);
        } else if (source.startsWith(TARGET_SDK_VERSION)) {
            apkInfo.setTargetSdkVersion(getPropertyInQuote(source));
        } else if (source.startsWith(USES_PERMISSION)) {
            apkInfo.addToUsesPermissions(getPropertyInQuote(source));
        } else if (source.startsWith(APPLICATION_LABEL)) {
            apkInfo.setApplicationLable(getPropertyInQuote(source));
        } else if (source.startsWith(APPLICATION_ICON)) {
            apkInfo.addToApplicationIcons(getKeyBeforeColon(source), getPropertyInQuote(source));
        } else if (source.startsWith(APPLICATION)) {
            String[] rs = source.split("( icon=')|'");
            apkInfo.setApplicationIcon(rs[rs.length - 1]);
        } else if (source.startsWith(USES_FEATURE)) {
            apkInfo.addToFeatures(getPropertyInQuote(source));
        } else if (source.startsWith(USES_IMPLIED_FEATURE)) {
            apkInfo.addToImpliedFeatures(getFeature(source));
        }
    }

    private ImpliedFeature getFeature(String source) {
        String[] result = source.split(FEATURE_SPLIT_REGEX);
        return new ImpliedFeature(result[1], result[2]);
    }

    /**
     * 返回出格式为name: 'value'中的value内容。
     *
     * @param source source
     * @return property value
     */
    private String getPropertyInQuote(String source) {
        return source.substring(source.indexOf("'") + 1, source.length() - 1);
    }

    /**
     * 返回冒号前的属性名称
     *
     * @param source source
     * @return 冒号前的属性名称
     */
    private String getKeyBeforeColon(String source) {
        return source.substring(0, source.indexOf(':'));
    }

    /**
     * 分离出包名、版本等信息。
     *
     * @param apkInfo       apk info
     * @param packageSource package Source
     */
    private void splitPackageInfo(ApkInfo apkInfo, String packageSource) {
        String[] packageInfo = packageSource.split(SPLIT_REGEX);
        apkInfo.setPackageName(packageInfo[2]);
        apkInfo.setVersionCode(packageInfo[4]);
        apkInfo.setVersionName(packageInfo[6]);
    }

    /**
     * 释放资源。
     *
     * @param c 将关闭的资源
     */
    private void closeIO(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
