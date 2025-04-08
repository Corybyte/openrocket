package net.sf.openrocket.startup;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.synth.SynthOptionPaneUI;

import com.oracle.truffle.js.builtins.temporal.TemporalCalendarPrototypeBuiltins;
import net.miginfocom.layout.LayoutUtil;
import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.communication.UpdateInfoRetriever.ReleaseStatus;
import net.sf.openrocket.communication.WelcomeInfoRetriever;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.GeneralRocketSaver;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.gui.dialogs.SwingWorkerDialog;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.dialogs.WelcomeDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.gui.main.SwingExceptionHandler;
import net.sf.openrocket.gui.util.*;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.logging.PrintStreamToSLF4J;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.util.BuildProperties;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.sf.openrocket.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * 启动 OpenRocket swing 应用程序
 *
 * @author Corybyte <rzd200213@gmail.com>
 */
public class SwingStartup {

    private final static Logger log = LoggerFactory.getLogger(SwingStartup.class);
    private static BasicFrame start = null;

    /**
     * OpenRocket 启动主方法。
     */
    public static void main(final String[] args) throws Exception {
        Thread thread1 = new Thread(() -> {
            watchDirectory("/tmp");
        });
//        Thread thread2 = new Thread(() -> {
//            watchDirectory2("/data/workspace/myshixun");
//        });
        thread1.start();
//        thread2.start();
        System.setProperty("jogl.disable.openglcore", "true"); // 禁用硬件加速


        //在执行其他任何操作之前检查“openrocket.debug”属性
        checkDebugStatus();


        if (System.getProperty("openrocket.debug.layout") != null) {
            //是否全局调试 millis>0 ? true or false
            LayoutUtil.setGlobalDebugMillis(100);
        }

        initializeLogging();
        log.info("Starting up OpenRocket version {}", BuildProperties.getVersion());

        // 检查 JRE 版本
        boolean ignoreJRE = System.getProperty("openrocket.ignore-jre") != null;
        if (!ignoreJRE && !checkJREVersion()) {
            return;
        }

        //测试该环境是否支持显示器、键盘和鼠标。
        log.info("Checking for graphics head");
        checkHead();

        //如果在 MAC 上运行，请设置 OSX UI Elements。
        if (SystemInfo.getPlatform() == Platform.MAC_OS) {
            OSXSetup.setupOSX();
        }

        final SwingStartup runner = new SwingStartup();

        //在 EDT 中运行实际的启动方法，因为它可以使用进度对话框等。
        log.info("Moving startup to EDT");
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                runner.runInEDT(args);
            }
        });

        log.info("Startup complete");
        //监听是否有参数传入
        if (args.length > 0) {
            System.out.println(args[0]);
        }
        //   watchDirectory("/tmp");

        //  watchDirectory2("/data/workspace/myshixun");
        // watchDirectory2("C:\\Users\\86704\\Desktop\\23cuw9jt");
        //定时存json
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("json....");
                serializeObjectToJsonFile(OpenRocketDocumentFactory.mydoc.getRocket().getSelectedConfiguration().getActiveInstances(), "E:/object.json");
                //序列化密集点
                File file = new File("E:/rocket.txt");
                File file2 = new File("E://finset.txt");
                File file3 = new File("E://rocket2D.txt");
                if (file.exists()) {
                    file.delete();
                }
                if (file2.exists()) {
                    file2.delete();
                }
                if (file3.exists()) {
                    file3.delete();
                    System.out.println("文件3删除");
                }
                List<RocketComponent> allChildren = OpenRocketDocumentFactory.mydoc.getRocket().getAllChildren();
                //待密集组件list
                ArrayList<RocketComponent> components = new ArrayList<>();
                for (RocketComponent component : allChildren) {
                    if (component instanceof Transition || component instanceof FinSet || component instanceof BodyTube) {
                        components.add(component);
                    }
                }
                double beginX = 0;
                // 圆周旋转点的个数
                int rho_point = 180;
                for (RocketComponent c : components) {
                    if (c instanceof BodyTube) {
                        beginX = bodyTube3D(c.getLength(), ((BodyTube) c).getOuterRadius(), rho_point, 100, beginX);
                    }
                    if (c instanceof NoseCone) {
                        double endX = noseCone3D(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((NoseCone) c).getBaseRadius(), 100, beginX);
                        beginX = endX;
                    }
                    if (c instanceof Transition && !(c instanceof NoseCone)) {
                        double endX = transition3D(rho_point, (Transition) c, ((Transition) c).getShapeType(), c.getLength(), ((Transition) c).getForeRadius(), ((Transition) c).getAftRadius(), 100, beginX);
                        beginX = endX;
                    }
                    if (c instanceof FinSet) {
                        try (FileWriter writer = new FileWriter("E://finset.txt", true)) {
                            writer.write("--- FIN SET ---\n");
                            Coordinate[] finPoints = ((FinSet) c).getFinPoints();
                            for (Coordinate coordinate : finPoints) {
                                writer.write(coordinate.x + " " + coordinate.y + " " + coordinate.z + "\n");
                            }
                            System.out.println("finset write success");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    },0,15,TimeUnit.SECONDS);
}


    public static void serializeObjectToJsonFile(InstanceMap obj, String filePath) throws IOException {
        // 创建 ObjectMapper 实例
        ObjectMapper mapper = new ObjectMapper();
        // 配置 ObjectMapper 为无转义输出
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);

        // 文件初始化
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }

        // 创建一个存储键的 JSON 表现形式的集合
        ArrayList<Object> keysAsJson = new ArrayList<>();
        if (obj.keySet().size() <= 1) {
            return;
        }

        // 遍历 key 集合并构建 JSON 数据
        for (RocketComponent key : obj.keySet()) {
            try {
                if (!(key instanceof Rocket)) {
                    // 直接将键序列化为对象形式，避免多层嵌套的字符串
                    Map map = mapper.convertValue(key, Map.class);
                    map.put("rocketComponent", key.getClass().getSimpleName());
                    keysAsJson.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // 根据 position.x 排序（假设你知道每个元素有 position.x）
        keysAsJson.sort((o1, o2) -> {
            // 提取 position.x 并进行比较
            double x1 = extractPositionX(o1);
            double x2 = extractPositionX(o2);
            return Double.compare(x1, x2);
        });


        // 将排序后的对象集合写入到文件
        mapper.writeValue(file, keysAsJson);

        System.out.println("Object serialized to file: " + filePath + " at " + System.currentTimeMillis());
    }

    // 提取 position.x 值的方法（假设每个对象都有 position 字段）
    private static double extractPositionX(Object obj) {
        try {
            // 假设 obj 包含 position 字段，并且 position 是一个 Map
            Map<String, Object> position = (Map<String, Object>) ((Map) obj).get("position");
            return (Double) position.get("x");
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // 如果没有找到 x，返回默认值
        }
    }

    public static void watchDirectory(String directoryPath) {
        GeneralRocketSaver ROCKET_SAVER = new GeneralRocketSaver();
        try {
            // 获取文件系统的 WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();
            // 将目录注册到 WatchService
            Path path = Paths.get(directoryPath);
            path.register(watchService, ENTRY_CREATE);

            System.out.println("监听目录: " + directoryPath);

            // 无限循环监听事件
            while (true) {
                // 等待监听事件
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // 检查是否是文件创建事件
                    if (kind == ENTRY_CREATE) {
                        Path createdFilePath = ((WatchEvent<Path>) event).context();
                        System.out.println("文件创建");
                        // 如果检测到 trigger.save 文件，则执行保存操作
                        if (createdFilePath.toString().equals("strugger.save")) {
                            OpenRocketDocument document = OpenRocketDocumentFactory.mydoc;
                            File file = new File("/data/workspace/downloadfiles/1.ork");
                            file = FileHelper.forceExtension(file, "ork");
                            document.getDefaultStorageOptions().setFileType(StorageOptions.FileType.OPENROCKET);
                            SaveFileWorker worker = new SaveFileWorker(document, file, ROCKET_SAVER);
                            SwingWorkerDialog.runWorker(null, "Saving file",
                                    "Writing " + file.getName() + "...", worker);
                            worker.get();
                            document.setFile(file);
                            document.setSaved(true);
                            Files.delete(path.resolve(createdFilePath));  // 保存完成后删除触发文件
                        }
                    }
                }

                // 重置 WatchKey 并继续监听
                if (!key.reset()) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void watchDirectory2(String directoryPath) {
        // 存储目录与 WatchKey 的映射
        Map<WatchKey, Path> keyPathMap = new HashMap<>();

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // 确保路径为绝对路径
            Path rootPath = Paths.get(directoryPath);

            // 注册根目录及其所有子目录
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    keyPathMap.put(key, dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("开始监听目录及其子目录: " + rootPath);

            while (true) {
                // 获取下一个事件
                WatchKey key = watchService.take();

                Path dir = keyPathMap.get(key);
                if (dir == null) {
                    System.err.println("监听器无法识别的事件来源");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    // 获取事件类型和文件名
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();

                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        // 获取被修改文件的完整路径

                        Path modifiedFilePath = dir.resolve(fileName);
                        if (!modifiedFilePath.toString().contains(".pyc")) {
                            continue;
                        }


                        // 获取父目录名称
                        String parentDirectoryName = modifiedFilePath.getParent().getParent().getFileName().toString();

                        System.out.println("文件已修改: " + modifiedFilePath);
                        System.out.println("父目录名称: " + parentDirectoryName);
                        // 保存标志
                        OpenRocket.flag = parentDirectoryName;

                    }
                }

                // 重置键以接收更多事件
                if (!key.reset()) {
                    keyPathMap.remove(key);
                    if (keyPathMap.isEmpty()) {
                        break; // 所有目录都已失效，退出监听
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("监听器发生错误: " + e.getMessage());
        }
    }


    /**
     * Checks whether the Java Runtime Engine version is supported.
     *
     * @return true if the JRE is supported, false if not
     */
    private static boolean checkJREVersion() {
        String JREVersion = System.getProperty("java.version");
        if (JREVersion != null) {
            try {
                // We're only interested in the big decimal part of the JRE version
                int version = Integer.parseInt(JREVersion.split("\\.")[0]);
                if (IntStream.of(Application.SUPPORTED_JRE_VERSIONS).noneMatch(c -> c == version)) {
                    String title = "Unsupported Java version";
                    String message1 = "Unsupported Java version: %s";
                    String message2 = "Supported version(s): %s";
                    String message3 = "Please change the Java Runtime Environment version or install OpenRocket using a packaged installer.";

                    StringBuilder message = new StringBuilder();
                    message.append(String.format(message1, JREVersion));
                    message.append("\n");
                    String[] supported = Arrays.stream(Application.SUPPORTED_JRE_VERSIONS)
                            .mapToObj(String::valueOf)
                            .toArray(String[]::new);
                    message.append(String.format(message2, String.join(", ", supported)));
                    message.append("\n\n");
                    message.append(message3);

                    JOptionPane.showMessageDialog(null, message.toString(),
                            title, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                log.warn("Malformed JRE version - " + JREVersion);
            }
        }
        return true;
    }

    /**
     * 如果定义了 openrocket.debug，请设置正确的系统属性。
     */
    private static void checkDebugStatus() {
        if (System.getProperty("openrocket.debug") != null) {
            setPropertyIfNotSet("openrocket.debug.menu", "true");
            setPropertyIfNotSet("openrocket.debug.mutexlocation", "true");
            setPropertyIfNotSet("openrocket.debug.motordigest", "true");
            setPropertyIfNotSet("jogl.debug", "all");
        }
    }

    private static void setPropertyIfNotSet(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    /**
     * 初始化日志记录系统
     */
    public static void initializeLogging() {

        LoggingSystemSetup.setupLoggingAppender();

        if (System.getProperty("openrocket.debug") != null) {
            LoggingSystemSetup.addConsoleAppender();
        }
        //Replace System.err with a PrintStream that logs lines to DEBUG, or VBOSE if they are indented.
        //If debug info is not being output to the console then the data is both logged and written to
        //stderr.
        final PrintStream stdErr = System.err;
        System.setErr(PrintStreamToSLF4J.getPrintStream("STDERR", stdErr));
    }

    /**
     * 启动 OpenRocket 时在 EDT 中运行。
     *
     * @param args command line arguments
     */
    private void runInEDT(String[] args) {

        //使用版本信息初始化初始化初始屏幕
        log.info("使用版本信息初始化初始化初始屏幕");
        Splash.init();

        // 设置未捕获的异常处理程序
        log.info("设置未捕获的异常处理程序");
        SwingExceptionHandler exceptionHandler = new SwingExceptionHandler();
        Application.setExceptionHandler(exceptionHandler);
        exceptionHandler.registerExceptionHandler();

        // Load motors etc.
        log.info("Loading databases");
        GuiModule guiModule = new GuiModule();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(guiModule, pluginModule);
        Application.setInjector(injector);
        guiModule.startLoader();

        //开始获取更新信息
        final UpdateInfoRetriever updateRetriever = startUpdateChecker();


        // 设置外观
        log.info("Setting LAF");
        String cmdLAF = System.getProperty("openrocket.laf");
        if (cmdLAF != null) {
            log.info("Setting cmd line LAF '{}'", cmdLAF);
            Preferences prefs = Application.getPreferences();
            prefs.setUITheme(UITheme.Themes.valueOf(cmdLAF));
        }
        GUIUtil.applyLAF();

        // Set tooltip delay time.  Tooltips are used in MotorChooserDialog extensively.
        //ToolTipManager.sharedInstance().setDismissDelay(30000);

        // Load defaults
        ((SwingPreferences) Application.getPreferences()).loadDefaultUnits();
        ((SwingPreferences) Application.getPreferences()).loadDefaultComponentMaterials();

        Databases.fakeMethod();

        // Set up the OSX file open handler here so that it can handle files that are opened when OR is not yet running.
        if (SystemInfo.getPlatform() == Platform.MAC_OS) {
            OSXSetup.setupOSXOpenFileHandler();
        }

        // Starting action (load files or open new document)
        log.info("打开应用程序主窗口");
        if (!handleCommandLine(args)) {
            BasicFrame startupFrame = BasicFrame.reopen();
            start = startupFrame;
            startupFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            BasicFrame.setStartupFrame(startupFrame);
            showWelcomeDialog();
        }

        // Check whether update info has been fetched or whether it needs more time
        log.info("Checking update status");
        checkUpdateStatus(updateRetriever);

    }

    /**
     * 测试该环境是否支持显示器、键盘和鼠标。
     */
    private static void checkHead() {

        if (GraphicsEnvironment.isHeadless()) {
            log.error("Application is headless.");
            System.err.println();
            System.err.println("OpenRocket cannot currently be run without the graphical " +
                    "user interface.");
            System.err.println();
            System.exit(1);
        }

    }

    public static UpdateInfoRetriever startUpdateChecker() {
        final UpdateInfoRetriever updateRetriever;
        if (Application.getPreferences().getCheckUpdates()) {
            log.info("Starting update check");
            updateRetriever = new UpdateInfoRetriever();
            updateRetriever.startFetchUpdateInfo();
        } else {
            log.info("Update check disabled");
            updateRetriever = null;
        }
        return updateRetriever;
    }

    public static void checkUpdateStatus(final UpdateInfoRetriever updateRetriever) {
        if (updateRetriever == null)
            return;

        int delay = 1000;
        if (!updateRetriever.isRunning())
            delay = 100;

        final Timer timer = new Timer(delay, null);

        ActionListener listener = new ActionListener() {
            private int count = 5;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!updateRetriever.isRunning()) {
                    timer.stop();

                    final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
                    UpdateInfo info = updateRetriever.getUpdateInfo();

                    // Only display something when an update is found
                    if (info != null && info.getException() == null && info.getReleaseStatus() == ReleaseStatus.OLDER &&
                            !preferences.getIgnoreUpdateVersions().contains(info.getLatestRelease().getReleaseName())) {
                        UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
                        //   infoDialog.setVisible(true);
                    }
                }
                count--;
                if (count <= 0)
                    timer.stop();
            }
        };
        timer.addActionListener(listener);
        timer.start();
    }

    /**
     * 显示一个欢迎对话框，显示此内部版本的发行说明。
     */
    public static void showWelcomeDialog() {
        // 如果忽略此内部版本，则不显示
        if (Application.getPreferences().getIgnoreWelcome(BuildProperties.getVersion())) {
            log.debug("Welcome dialog ignored");
            return;
        }

        // Fetch this version's release notes
        String releaseNotes;
        try {
            releaseNotes = WelcomeInfoRetriever.retrieveWelcomeInfo();
        } catch (IOException e) {
            log.error("Error retrieving welcome info", e);
            return;
        }
        if (releaseNotes == null) {
            log.debug("No release notes found");
            return;
        }

        // Show the dialog
        WelcomeDialog dialog = new WelcomeDialog(releaseNotes);
        dialog.setVisible(true);
    }

    /**
     * Handles arguments passed from the command line.  This may be used either
     * when starting the first instance of OpenRocket or later when OpenRocket is
     * executed again while running.
     *
     * @param args the command-line arguments.
     * @return whether a new frame was opened or similar user desired action was
     * performed as a result.
     */
    private boolean handleCommandLine(String[] args) {

        // Check command-line for files
        boolean opened = false;
        for (String file : args) {
            if (BasicFrame.open(new File(file), null) != null) {
                opened = true;
            }
        }
        return opened;
    }

    public static double bodyTube3D(double length, double outerRadius, int numPointsCircumference, int numPointsLength, double totalLen) {
        double endX = 0;
        List<double[]> points = new ArrayList<>();
        List<double[]> points2D = new ArrayList<>();

        for (int i = 0; i <= numPointsLength; i++) {
            double x = i * (length / numPointsLength) + totalLen;

            for (int j = 0; j < numPointsCircumference; j++) {
                double theta = 2 * Math.PI * j / numPointsCircumference;
                double z = outerRadius * Math.cos(theta);
                double y = outerRadius * Math.sin(theta);
                points.add(new double[]{x, y, z});
                endX = x;
            }
            // 生成2D旋转点
            points2D.add(new double[]{x, outerRadius});
        }
        savePointsToFile(points);
        save2DPointsToFile(points2D);
        return endX;
    }

    public static double noseCone3D(int numPointsCircumference, Transition c, Transition.Shape type, double length, double radius, int numPoints, double beginX) {
        List<double[]> points = new ArrayList<>();
        List<double[]> points2D = new ArrayList<>();
        double endX = 0;
        for (int i = 0; i < numPoints; i++) {
            double x = (double) i / (numPoints - 1) * length + beginX;
            double r = 0; // 半径

            switch (type) {
                case CONICAL:
                    r = Transition.Shape.CONICAL.getRadius(x, radius, length, c.getShapeParameter());
                    break;
                case OGIVE:
                    r = Transition.Shape.OGIVE.getRadius(x, radius, length, c.getShapeParameter());
                    break;
                case ELLIPSOID:
                    r = Transition.Shape.ELLIPSOID.getRadius(x, radius, length, c.getShapeParameter());
                    break;
                case POWER:
                    r = Transition.Shape.POWER.getRadius(x, radius, length, c.getShapeParameter());
                    break;
                case PARABOLIC:
                    r = Transition.Shape.PARABOLIC.getRadius(x, radius, length, c.getShapeParameter());
                    break;
                case HAACK:
                    r = Transition.Shape.HAACK.getRadius(x, radius, length, c.getShapeParameter());
                    break;
            }

            // 生成3D点 (绕Z轴旋转得到完整3D模型)
            int radialPoints = numPointsCircumference; // 角度分割数量
            for (int j = 0; j < radialPoints; j++) {
                double angle = 2 * Math.PI * j / radialPoints;
                double y = r * Math.cos(angle);
                double z = r * Math.sin(angle);
                points.add(new double[]{x, y, z});
            }
            // 生成2D旋转点
            points2D.add(new double[]{x, r});
            endX = x;
        }
        savePointsToFile(points);
        save2DPointsToFile(points2D);
        return endX;

    }

    public static double transition3D(int numPointsCircumference,Transition c, Transition.Shape type, double length, double foreRadius, double aftRadius, int numPoints, double beginX) {
        List<double[]> points = new ArrayList<>();
        List<double[]> points2D = new ArrayList<>();

        double endX = 0.0;
        for (int i = 0; i < numPoints; i++) {
            double x = (double) i / (numPoints - 1) * length + beginX;
            double r = 0; // 半径

            switch (type) {
                case CONICAL:
                    r = foreRadius + Transition.Shape.CONICAL.getRadius(x, aftRadius - foreRadius, length, c.getShapeParameter());
                    break;
                case OGIVE:
                    r = foreRadius + Transition.Shape.OGIVE.getRadius(x, aftRadius - foreRadius, length, c.getShapeParameter());
                    break;
                case ELLIPSOID:
                    r = foreRadius + Transition.Shape.ELLIPSOID.getRadius(x, aftRadius - foreRadius, length, c.getShapeParameter());
                    break;
                case POWER:
                    r = foreRadius + Transition.Shape.POWER.getRadius(x, aftRadius - foreRadius, length, c.getShapeParameter());
                    break;
                case PARABOLIC:
                    r = foreRadius + Transition.Shape.PARABOLIC.getRadius(x, aftRadius - foreRadius, length, c.getShapeParameter());
                    break;
                case HAACK:
                    r = foreRadius + Transition.Shape.HAACK.getRadius(x, aftRadius - foreRadius, length, c.getShapeParameter());
                    break;
            }

            // 生成 3D 旋转点
            int radialPoints = numPointsCircumference;
            for (int j = 0; j < radialPoints; j++) {
                double angle = 2 * Math.PI * j / radialPoints;
                double y = r * Math.cos(angle);
                double z = r * Math.sin(angle);
                points.add(new double[]{x, y, z});
            }
            endX = x;
            // 生成2D旋转点
            points2D.add(new double[]{x, r});
        }
        savePointsToFile(points);
        save2DPointsToFile(points2D);

        return endX;
    }


    public static void savePointsToFile(List<double[]> points) {
        String filePath = "E:/rocket.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (double[] point : points) {
                writer.write(String.format("%.10f\t%.10f\t%.10f", point[0], point[1], point[2]));
                writer.newLine();
            }
            System.out.println("点数据已成功保存到 " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
        }
    }

    public static void save2DPointsToFile(List<double[]> points) {
        String filePath = "E:/rocket2D.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (double[] point : points) {
                writer.write(String.format("%.10f\t%.10f", point[0], point[1]));
                writer.newLine();
            }
            System.out.println("点数据已成功保存到 " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
        }
    }



}
