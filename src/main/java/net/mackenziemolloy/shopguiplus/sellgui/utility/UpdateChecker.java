package net.mackenziemolloy.shopguiplus.sellgui.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.mackenziemolloy.shopguiplus.sellgui.SellGUI;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final PlatformScheduler scheduler;
    private final int resourceId;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.scheduler = SellGUI.scheduler();
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        scheduler.runAsync(task -> getVersionInternal(consumer));
    }

    private void getVersionInternal(Consumer<String> consumer) {
        String updateUrlFormat = ("https://api.spigotmc.org/legacy/update.php?resource=%s");
        String updateUrl = String.format(Locale.US, updateUrlFormat, this.resourceId);

        try (
            InputStream inputStream = new URL(updateUrl).openStream();
            Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                consumer.accept(scanner.next());
            }
        } catch (IOException ex) {
            Logger logger = this.plugin.getLogger();
            logger.log(Level.INFO, "Failed to check for updates because an error occurred:", ex);
        }
    }
}
