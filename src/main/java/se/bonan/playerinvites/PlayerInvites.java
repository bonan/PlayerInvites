package se.bonan.playerinvites;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import se.bonan.playerinvites.commands.*;
import se.bonan.playerinvites.exception.PlayerNotFoundException;
import se.bonan.playerinvites.object.DataFile;
import se.bonan.playerinvites.object.PlayerData;
import se.bonan.playerinvites.tasks.AutoGiveTask;
import se.bonan.playerinvites.tasks.SaveTask;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.*;

public class PlayerInvites extends JavaPlugin {

    /**
     * Static config for access from Str
     */
    public static FileConfiguration config;

    /**
     * Static cmd for access from Str
     */
    public static String cmd = "invite";

    /**
     * DataFile container, contains all relevant data for this plugin
     */
    private DataFile data;

    /**
     * For integration with Vault
     */
    private Economy economy = null;

    /**
     * Tasks
     */
    private BukkitTask saveTask;
    private BukkitTask autoGiveTask;

    /**
     * Available command instances, resets on reload
     */
    private List<CommandInterface> commands = new ArrayList<>();

    @Override
    public void onEnable() {
        File conf = new File(getDataFolder(), "config.yml");
        if (!conf.exists()) {
            saveDefaultConfig();
            /**
             * Generate new configuration file
             */
            try {
                StringBuilder confString = new StringBuilder();

                /**
                 * Read old config
                 */
                FileReader r = new FileReader(conf);
                char[] t = new char[1024];
                for (int read = r.read(t); read >= 0; read = r.read(t)) {
                    confString.append(String.valueOf(t).substring(0, read));
                }
                r.close();

                /**
                 * Add string configuration
                 */
                confString.append(Str.getConfig());

                /**
                 * Write configuration
                 */
                FileWriter w = new FileWriter(conf);
                w.write(confString.toString());
                w.close();
            } catch (IOException e) {

            }

        }
        data = new DataFile(null);
        load();
    }

    @Override
    public void onDisable() {

    }

    /**
     * Reloads configuration and data
     * @return Empty string on success, error string otherwise
     */
    public String reload() {
        reloadConfig();
        return load();
    }

    /**
     * Loads data from file and sets everything up
     * @return Empty string on success, error string otherwise
     */
    public String load() {
        try {
            /**
             * Static config used by Str
             */
            config = getConfig();

            /**
             * Name of base command, used by Str and in other places
             */
            cmd = config.getString("cmdBase", "invite");

            /**
             * Reset economy variable
             */
            economy = null;

            /**
             * Check for previously scheduled AutoGiveTask
             */
            if (autoGiveTask != null) {
                /**
                 * Cancel previous scheduled task
                 */
                autoGiveTask.cancel();
                autoGiveTask = null;
            }

            /**
             * Create new AutoGiveTask, loads settings from configuration
             */
            AutoGiveTask task = new AutoGiveTask(this);

            /**
             * Check if task is actually needed, otherwise scheduling is not
             * necessary.
             */
            if (task.isNeeded()) {
                /**
                 * Schedule task
                 */
                autoGiveTask = task.runTaskTimer(this, 20, task.getInterval());
            }

            /**
             * Define commands
             *
             * Command names can be overriden in config
             */
            this.commands = new LinkedList<>();
            this.commands.add(new ShowCommand(config.getString("cmdShow", "show"), this));
            this.commands.add(new UseCommand(config.getString("cmdUse", "use"), this));
            this.commands.add(new BuyCommand(config.getString("cmdBuy", "buy"), this));
            this.commands.add(new GiveCommand(config.getString("cmdGive", "give"), this));
            this.commands.add(new ReloadCommand(config.getString("cmdReload", "reload"), this));

            /**
             * Loads file from data.json in plugin data folder
             */
            data = DataFile.load(new File(getDataFolder(), "data.json"), getServer());
        } catch (Exception e) {
            getLogger().warning("Unable to load PlayerInvites data file: " + e.getMessage());
            return ChatColor.RED + "Reload error: " + e.getMessage();
        }
        return "";
    }

    /**
     * Schedule saving of data
     */
    public void save() {
        save(false);
    }

    /**
     * Schedules saving of data as json to data.json in plugin data folder
     * If nothing has changed for 30 seconds, the file will be saved
     * @param now True to save file directly (not async)
     */
    public void save(Boolean now) {
        if (saveTask != null) {
            saveTask.cancel();
        }
        SaveTask task = new SaveTask(this, new File(getDataFolder(), "data.json"));
        if (now) {
            task.run();
            saveTask = null;
        } else {
            saveTask = task.runTaskLaterAsynchronously(this, 20L*30L);
        }
    }

    /**
     * Get economy plugin (Vault)
     *
     * @return Economy (null if not present)
     */
    public Economy getEconomy() {
        if (economy == null) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    economy = rsp.getProvider();
                }
            }
        }
        return economy;
    }

    /**
     * Returns DataFile object
     * @return Data file
     */
    public DataFile getData() {
        return data;
    }

    /**
     * Creates a new PlayerData object if not present and saves it to DataFile
     * @param player Player object
     * @param invitedBy Object of player who invited
     * @return Player Data
     */
    public PlayerData addPlayer(OfflinePlayer player, OfflinePlayer invitedBy) {
        PlayerData d;
        Map<String,PlayerData> players = data.getPlayers();
        String uuid = player.getUniqueId().toString();
        if (players.containsKey(uuid)) {
            d = players.get(uuid);
        } else {
            d = new PlayerData(player, invitedBy);
            data.getPlayers().put(player.getUniqueId().toString(), d);
        }
        return d;
    }

    /**
     * Gets PlayerData from a player object
     * @param player
     * @return Player Data
     */
    public PlayerData getPlayerData(OfflinePlayer player) {
        return addPlayer(player, null);
    }

    /**
     * Gets PlayerData from a player uuid
     * @param uuid
     * @return Player Data
     * @throws PlayerNotFoundException
     */
    public PlayerData getPlayerData(String uuid) throws PlayerNotFoundException {
        if (!data.getPlayers().containsKey(uuid))
            throw new PlayerNotFoundException(uuid);
        return data.getPlayers().get(uuid);
    }

    /**
     * Invoked when console/player issues /invite
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            if (!player.hasPermission(command.getPermission())) {
                sender.sendMessage(Str.ErrorPermission.str());
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("invite")) {

            if (args.length > 0) {
                /**
                 * Look for command
                 */
                for (CommandInterface action: commands) {
                    if (action.getName().equalsIgnoreCase(args[0])) {
                        /**
                         * Check permission for command
                         */
                        if (!action.hasPerm(player)) {
                            sender.sendMessage(Str.ErrorPermission.str());
                            return true;
                        }

                        /**
                         * Invoke command and return result to CommandSender
                         */
                        sender.sendMessage(
                                action.invoke(
                                        player,
                                        Arrays.copyOfRange(args, 1, args.length)
                                )
                        );
                        return true;
                    }
                }
            }

            /**
             * If no command was found, show help
             */

            String opts = "";
            String usage = "";

            /**
             * Loop through commands to get usage and help strings
             */
            for (CommandInterface action: commands) {
                /**
                 * Don't show commands that the player doesn't have permission for
                 */
                if (action.hasPerm(player)) {
                    /**
                     * Add command to opts string
                     */
                    opts = opts + (opts.length() > 0 ? Str.HelpCmdSep.str() : "") + action.getName();

                    /**
                     * Get usage help
                     */
                    usage = usage + action.usage(player) + "\n";

                    /**
                     * Check for additional help strings
                     */
                    String help = action.help(player);
                    if (help != null && !help.equals("")) {
                        usage = usage + "    " + help + "\n";
                    }
                }
            }

            /**
             * Shows short usage line
             * "Usage: /invite <show|give|use|...>"
             */
            sender.sendMessage(Str.HelpUsage.format(opts));

            /**
             * Shows help
             */
            sender.sendMessage(usage);
            return true;

        }

        return false;
    }
}
