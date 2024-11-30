package chylex.hee.system.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.proxy.ModCommonProxy.MessageType;

public final class Log {

    static final Logger logger = LogManager.getLogger("HardcoreEnderExpansion");

    public static final boolean isDeobfEnvironment;
    public static boolean forceDebugEnabled;
    private static FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");
    private static long lastLogReport = -1;
    private static byte obfEnvironmentWarning = 0;

    static {
        isDeobfEnvironment = ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")).booleanValue();

        if (isDeobfEnvironment && MinecraftServer.getServer() != null
                && MinecraftServer.getServer().getClass().getSimpleName().equals("DedicatedServer")) {
            try (FileOutputStream fos = new FileOutputStream(new File("eula.txt"))) {
                Properties properties = new Properties();
                properties.setProperty("eula", "true");
                properties.store(fos, "Screw your EULA, I don't want that stuff in my workspace.");
            } catch (Exception e) {}
        }
    }

    public static void initializeDebug() {
        if (forceDebugEnabled || isDeobfEnvironment)
            HardcoreEnderExpansion.proxy.sendMessage(MessageType.DEBUG_TITLE_SET, ArrayUtils.EMPTY_INT_ARRAY);
        if (forceDebugEnabled) HardcoreEnderExpansion.notifications
                .report("[Hardcore Ender Expansion] Forced debugging is enabled.", true);
    }

    public static boolean isDebugEnabled() {
        return forceDebugEnabled || isDeobfEnvironment;
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void debug(String message, Object... data) {
        if (forceDebugEnabled || isDeobfEnvironment) logger.info(getMessage(message, data));

        if (forceDebugEnabled && !isDeobfEnvironment && ++obfEnvironmentWarning >= 30) {
            logger.warn(
                    getMessage(
                            "Detected obfuscated environment, don't forget to disable logging debug info after you are done debugging!"));
            obfEnvironmentWarning = 0;
        }
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void reportedDebug(String message, Object... data) {
        if (forceDebugEnabled || isDeobfEnvironment) {
            debug(message, data);
            HardcoreEnderExpansion.notifications.report("[DEBUG] " + getMessage(message, data));
        }
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void info(String message, Object... data) {
        logger.info(getMessage(message, data));
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void warn(String message, Object... data) {
        logger.warn(getMessage(message, data));
        if (forceDebugEnabled || isDeobfEnvironment)
            HardcoreEnderExpansion.notifications.report("[WARN] " + getMessage(message, data));
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void error(String message, Object... data) {
        logger.error(getMessage(message, data));
        if (forceDebugEnabled || isDeobfEnvironment)
            HardcoreEnderExpansion.notifications.report("[ERROR] " + getMessage(message, data));
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void reportedError(String message, Object... data) {
        logger.error(getMessage(message, data));
        HardcoreEnderExpansion.notifications.report(
                "[" + dateFormat.format(Calendar.getInstance().getTime())
                        + "] "
                        + getMessage(message, data)
                        + " Check the log for stack trace to report.");
    }

    /** Use $x where x is between 0 and data.length-1 to input variables. */
    public static void throwable(Throwable throwable, String message, Object... data) {
        logger.catching(Level.ERROR, throwable);
        logger.error(getMessage(message, data));

        if (lastLogReport == -1 || TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastLogReport) >= 10)
            HardcoreEnderExpansion.notifications.report(
                    "[" + dateFormat.format(Calendar.getInstance().getTime())
                            + "] "
                            + getMessage(message, data)
                            + " Check the log for stack trace to report.");
        lastLogReport = System.nanoTime();
    }

    private static String getMessage(String message, Object... data) {
        for (int a = data.length - 1; a >= 0; a--)
            message = message.replace("$" + a, data[a] == null ? "null" : String.valueOf(data[a]));
        return message;
    }
}
