package se.bonan.playerinvites;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Date: 2015-03-14
 * Time: 01:37
 */
public class PlayerInvites extends JavaPlugin {

    Invites invites;
    Economy economy = null;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            saveDefaultConfig();
        setupEconomy();
        invites = new Invites(this);

    }

    /**
     * Gets Economy provider (Vault)
     * @return True if economy is available
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Get economy plugin (Vault)
     *
     * @return Economy (null if not present)
     */
    public Economy getEconomy() {
        return economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /**
         * Strings for help text
         */
        String strUsage = getConfig().getString("stringUsage", "Usage:");
        String strPlayer = getConfig().getString("stringPlayer", "player");
        String strAmount = getConfig().getString("stringAmount", "amount");

        /**
         * Base command. Note that this only is used in help texts, you
         * need to manually add an alias if you change the command
         */
        String cmdBase = getConfig().getString("cmdBase", "invite");

        /**
         * Sub-commands for /invite
         */
        String cmdUse = getConfig().getString("cmdUse", "use");
        String cmdGive = getConfig().getString("cmdGive", "give");
        String cmdShow = getConfig().getString("cmdShow", "show");
        String cmdReload = getConfig().getString("cmdReload", "reload");
        String cmdBuy = getConfig().getString("cmdBuy", "buy");
        String cmdHelp = getConfig().getString("cmdHelp", "help");

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            if (!player.hasPermission(command.getPermission())) {
                sender.sendMessage(ChatColor.RED + getConfig().getString(
                        "stringPermissionError",
                        "You don't have permission for that command"
                ));
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("invite")) {
            String cmd = "";
            String arg1 = "";
            String arg2 = "";
            if (args.length > 0) {
                cmd = args[0];
                if (args.length > 1)
                    arg1 = args[1];
                if (args.length > 2)
                    arg2 = args[2];
            }

            /**
             * Shows your or another players current number of invites, who invited them
             *   and who they have invited
             */
            if (cmd.equalsIgnoreCase(cmdShow)) {
                if (sender.hasPermission("invites.show") || sender.hasPermission("invites.showother")) {
                    if (sender.hasPermission("invites.showother") && !arg1.equals("") && arg2.equals("")) {
                        /**
                         * Show other players details
                         */
                        String invited = invites.showInvited(arg1);
                        if (invited != null)
                            sender.sendMessage(invited);
                        sender.sendMessage(invites.showInvites(
                                arg1,
                                player != null && arg1.equalsIgnoreCase(player.getName())
                        ));
                        return true;
                    } else if (arg1.equals("") && player != null) {
                        /**
                         * Show own details
                         */
                        String invited = invites.showInvited(player.getName());
                        if (invited != null)
                        sender.sendMessage(invited);
                        sender.sendMessage(invites.showInvites(
                                player.getName(),
                                true
                        ));
                        return true;
                    }
                    if (player == null)
                        /** Help text for console: "Usage: /invite show <player>" **/
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdShow+" <"+strPlayer+">");
                    else if (sender.hasPermission("invites.showother"))
                        /** Help text for showother: "Usage: /invite show [player]" **/
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdShow+" ["+strPlayer+"]");
                    else
                        /** Help text for show: "Usage: /invite show" **/
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdShow);

                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + getConfig().getString(
                            "stringPermissionError",
                            "You don't have permission for that command"
                    ));
                    return true;
                }
            }

            /**
             * Uses an invite to white list another player
             */
            if (cmd.equalsIgnoreCase(cmdUse)) {
                if (sender.hasPermission("invites.invite") && player != null) {
                    if (arg1.equals("") || !arg2.equals("")) {
                        /** Help text for use: "Usage: /invite use <player>" **/
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdUse+" <"+strPlayer+">");
                        return true;
                    }
                    /**
                     * Invites the player and sends result as a message
                     */
                    sender.sendMessage(invites.invitePlayer(player, arg1));
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + getConfig().getString(
                            "stringPermissionError",
                            "You don't have permission for that command"
                    ));
                    return true;
                }
            }

            /**
             * Buy invites for in-game money
             *
             * Uses Vault for Economy integration
             */
            if (cmd.equalsIgnoreCase(cmdBuy)) {
                if (sender.hasPermission("invites.buy") && player != null) {
                    /**
                     * Check if economy is available and enabled
                     */
                    if (economy != null && getConfig().getBoolean("buyInvite", false)) {
                        Integer count = 1;
                        if (!arg1.equals("")) {
                            try {
                                count = Integer.parseInt(arg1);
                            } catch (NumberFormatException e) {
                                /** Message on invalid amount **/
                                sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdBuy+" ["+strAmount+"]");
                                return true;
                            }
                        }

                        if (count > 0 && arg2.equals("")) {
                            /**
                             * Buys invite and sends result as a message
                             */
                            sender.sendMessage(invites.buyInvite(player, count));
                            return true;
                        }

                        /** Message on too many/few arguments */
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdBuy+" ["+strAmount+"]");
                        return true;
                    }
                    sender.sendMessage(ChatColor.RED.toString() +
                            getConfig().getString("stringBuyDisabled", "Buying invites is disabled"));
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + getConfig().getString(
                            "stringPermissionError",
                            "You don't have permission for that command"
                    ));
                    return true;
                }
            }

            /**
             * Reloads data file and configuration
             */
            if (cmd.equalsIgnoreCase(cmdReload)) {
                if (sender.hasPermission("invites.reload")) {
                    /**
                     * Reloads the data file without saving
                     *
                     * Saving is done after every action, saving here would
                     * make it impossible to manually edit the data file
                     */
                    String reload = invites.reload();
                    if (reload.equals("")) {
                        /**
                         * Reloads the configuration
                         */
                        reloadConfig();
                        sender.sendMessage(ChatColor.GREEN.toString() +
                                "Invite configuration and data reloaded successfully.");
                        return true;
                    }
                    /**
                     * Error when reloading, send error to admin in-game
                     */
                    sender.sendMessage(reload);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + getConfig().getString(
                            "stringPermissionError",
                            "You don't have permission for that command"
                    ));
                    return true;
                }
            }

            /**
             * Give invites to player
             *
             * Note that this is an admin command, it does not subtract from
             * your invites to give to another player.
             */
            if (cmd.equalsIgnoreCase(cmdGive)) {
                if (sender.hasPermission("invites.give")) {
                    Integer count = 1;
                    if (!arg2.equals("")) {
                        try {
                            count = Integer.parseInt(arg2);
                        } catch (NumberFormatException e) {
                            /**
                             * Invalid amount message
                             * "Usage: /invite give <player> [amount]"
                             */
                            sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdGive+" <"+strPlayer+"> ["+strAmount+"]");
                            return true;
                        }
                    }
                    if (arg1.equals("") || args.length > 3 || count == 0) {
                        /**
                         * Too few/many arguments, or amount = 0
                         */
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdGive+" <"+strPlayer+"> ["+strAmount+"]");
                        return true;
                    }

                    /**
                     * Give invites to player
                     */
                    sender.sendMessage(invites.giveInvite(arg1, count));
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + getConfig().getString(
                            "stringPermissionError",
                            "You don't have permission for that command"
                    ));
                    return true;
                }
            }

            /**
             * No return yet, no command or invalid command given.
             *
             * Show help text for the commands available to the player
             *
             * opts = Sub-commands separated with pipeline
             */

            String opts = "";
            String text = "";
            String text2 = "";

            if (sender.hasPermission("invites.showother")) {
                /**
                 * Help text if player has permission to show other players info
                 */
                opts = ChatColor.GOLD + cmdShow + ChatColor.RESET;
                text = ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdShow +
                        ChatColor.RESET + " [" +
                        ChatColor.DARK_GREEN + strPlayer +
                        ChatColor.RESET + "] - " +
                        getConfig().getString("stringHelpShow", "Show player invites") + "\n";
            } else if (sender.hasPermission("invites.show")) {
                /**
                 * Help text if player only has permission to show their own info
                 */
                opts = ChatColor.GOLD + cmdShow + ChatColor.RESET;
                text = ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdShow +
                        ChatColor.RESET + " - " +
                        getConfig().getString("stringHelpShowYour", "Show your invites") + "\n";
            }

            if (sender.hasPermission("invites.invite")) {
                /**
                 * Help text if player has permission to use invites
                 */
                opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdUse + ChatColor.RESET;
                text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdUse +
                        ChatColor.RESET + " <" +
                        ChatColor.DARK_GREEN + strPlayer +
                        ChatColor.RESET + "> - " +
                        getConfig().getString("stringHelpUse", "Use invite on player") + "\n";
            }

            if (sender.hasPermission("invites.buy") && economy != null && getConfig().getBoolean("buyInvite", false)) {
                /**
                 * Help text if player has permission to buy invites, and economy is available
                 */
                Double price = getConfig().getDouble("buyInvitePrice", 0);
                if (price > 0) {
                    opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdBuy + ChatColor.RESET;
                    text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                            ChatColor.GOLD + " " + cmdBuy +
                            ChatColor.RESET + " [" +
                            ChatColor.DARK_GREEN + strAmount +
                            ChatColor.RESET + "] - " +
                            getConfig().getString("stringHelpBuy", "Buy invites") + "\n";
                    /**
                     * Shows price for invite below help texts
                     */
                    text2 = "\n" + getConfig().getString("stringHelpBuyCost", "" +
                            "One invite costs %s")
                            .replace("%s", ChatColor.BLUE.toString() + price + " " +
                            (price==1?economy.currencyNameSingular() : economy.currencyNamePlural()));
                }
            }

            if (sender.hasPermission("invites.give")) {
                /**
                 * Help text if player has permission to give (create new) invites
                 */
                opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdGive + ChatColor.RESET;
                text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdGive +
                        ChatColor.RESET + " <" +
                        ChatColor.DARK_GREEN + strPlayer +
                        ChatColor.RESET + "> [" +
                        ChatColor.DARK_GREEN + strAmount +
                        ChatColor.RESET + "] - " +
                        getConfig().getString("stringHelpGive", "Give a player invites") + "\n";
            }

            if (sender.hasPermission("invites.reload")) {
                /**
                 * Help text if player has permission to reload plugin configuration/data
                 */
                opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdReload + ChatColor.RESET;
                text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdReload +
                        ChatColor.RESET + " - " +
                        getConfig().getString("stringHelpReload", "Reloads configuration") + "\n";
            }

            if (text.equals("")) {
                /**
                 * Player has no permission to do anything
                 */
                sender.sendMessage(ChatColor.RED.toString() + getConfig().getString(
                        "stringPermissionError",
                        "You don't have permission for that command"
                ));
                return true;
            }

            /**
             * Shows summary of available commands
             *
             * "Usage: /invite <show|use|buy|give|reload>"
             */
            sender.sendMessage(strUsage + ChatColor.DARK_GRAY + " /" + cmdBase + ChatColor.RESET + " <" + opts + ">");

            /**
             * Shows help text for each sub command
             */
            sender.sendMessage(text+text2);
            return true;
        }

        return false;
    }
}
