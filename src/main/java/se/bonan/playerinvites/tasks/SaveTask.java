package se.bonan.playerinvites.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import se.bonan.playerinvites.PlayerInvites;

import java.io.File;
import java.io.IOException;

/**
 * User: Björn
 * Date: 2015-03-15
 * Time: 15:31
 */
public class SaveTask extends BukkitRunnable {

    private final PlayerInvites plugin;
    private final File file;

    public SaveTask(PlayerInvites plugin, File file) {
        this.plugin = plugin;
        this.file = file;

    }

    @Override
    public void run() {
        try {
            plugin.getData().save(file);
        } catch (IOException e) {

        }
    }
}
