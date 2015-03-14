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

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String strUsage = getConfig().getString("stringUsage", "Usage:");
        String strPlayer = getConfig().getString("stringPlayer", "player");
        String strAmount = getConfig().getString("stringAmount", "amount");

        String cmdBase = getConfig().getString("cmdBase", "invite");

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
                sender.sendMessage(getConfig().getString(
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

            if (cmd.equalsIgnoreCase(cmdShow)) {
                if (sender.hasPermission("invites.show") || sender.hasPermission("invites.showother")) {
                    if (sender.hasPermission("invites.showother") && !arg1.equals("") && arg2.equals("")) {
                        sender.sendMessage(invites.showInvited(
                                arg1
                        ));
                        sender.sendMessage(invites.showInvites(
                                arg1,
                                player != null && arg1.equalsIgnoreCase(player.getName())
                        ));
                        return true;
                    } else if (arg1.equals("") && player != null) {
                        sender.sendMessage(invites.showInvited(
                                player.getName()
                        ));
                        sender.sendMessage(invites.showInvites(
                                player.getName(),
                                true
                        ));
                        return true;
                    }
                    if (player == null)
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdShow+" <"+strPlayer+">");
                    else if (sender.hasPermission("invites.showother"))
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdShow+" ["+strPlayer+"]");
                    else
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

            if (cmd.equalsIgnoreCase(cmdUse)) {
                if (sender.hasPermission("invites.invite") && player != null) {
                    if (arg1.equals("") || !arg2.equals("")) {
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdUse+" <"+strPlayer+">");
                        return true;
                    }
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

            if (cmd.equalsIgnoreCase(cmdBuy)) {
                if (sender.hasPermission("invites.buy") && player != null) {
                    if (economy != null && getConfig().getBoolean("buyInvite", false)) {
                        Integer count = 1;
                        if (!arg1.equals("")) {
                            try {
                                count = Integer.parseInt(arg1);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdBuy+" ["+strAmount+"]");
                                return true;
                            }
                        }

                        if (count > 0 && arg2.equals("")) {
                            sender.sendMessage(invites.buyInvite(player, count));
                            return true;
                        }

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

            if (cmd.equalsIgnoreCase(cmdReload)) {
                if (sender.hasPermission("invites.reload")) {
                    String reload = invites.reload();
                    if (reload.equals("")) {
                        reloadConfig();
                        sender.sendMessage(ChatColor.GREEN.toString() +
                                "Invite configuration and data reloaded successfully.");
                        return true;
                    }
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

            if (cmd.equalsIgnoreCase(cmdGive)) {
                if (sender.hasPermission("invites.give")) {
                    Integer count = 1;
                    if (!arg2.equals("")) {
                        try {
                            count = Integer.parseInt(arg2);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdGive+" <"+strPlayer+"> ["+strAmount+"]");
                            return true;
                        }
                    }
                    if (arg1.equals("") || args.length > 3 || count == 0) {
                        sender.sendMessage(strUsage+" /"+cmdBase+" "+cmdGive+" <"+strPlayer+"> ["+strAmount+"]");
                        return true;
                    }

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

            String opts = "";
            String text = "";
            String text2 = "";

            if (sender.hasPermission("invites.showother")) {
                opts = ChatColor.GOLD + cmdShow + ChatColor.RESET;
                text = ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdShow +
                        ChatColor.RESET + " [" +
                        ChatColor.DARK_GREEN + strPlayer +
                        ChatColor.RESET + "] - " +
                        getConfig().getString("stringHelpShow", "Show player invites") + "\n";
            } else if (sender.hasPermission("invites.show")) {
                opts = ChatColor.GOLD + cmdShow + ChatColor.RESET;
                text = ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdShow +
                        ChatColor.RESET + " - " +
                        getConfig().getString("stringHelpShowYour", "Show your invites") + "\n";
            }

            if (sender.hasPermission("invites.invite")) {
                opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdUse + ChatColor.RESET;
                text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdUse +
                        ChatColor.RESET + " <" +
                        ChatColor.DARK_GREEN + strPlayer +
                        ChatColor.RESET + "> - " +
                        getConfig().getString("stringHelpUse", "Use invite on player") + "\n";
            }

            if (sender.hasPermission("invites.buy") && economy != null && getConfig().getBoolean("buyInvite", false)) {
                Double price = getConfig().getDouble("buyInvitePrice", 0);
                if (price > 0) {
                    opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdBuy + ChatColor.RESET;
                    text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                            ChatColor.GOLD + " " + cmdBuy +
                            ChatColor.RESET + " [" +
                            ChatColor.DARK_GREEN + strAmount +
                            ChatColor.RESET + "] - " +
                            getConfig().getString("stringHelpBuy", "Buy invites") + "\n";
                    text2 = "\n" + getConfig().getString("stringHelpBuyCost", "" +
                            "One invite costs %s")
                            .replace("%s", ChatColor.BLUE.toString() + price + " " +
                            (price==1?economy.currencyNameSingular() : economy.currencyNamePlural()));
                }
            }

            if (sender.hasPermission("invites.give")) {
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
                opts = opts + (opts.length()>0?"|":"") + ChatColor.GOLD + cmdReload + ChatColor.RESET;
                text = text + ChatColor.DARK_GRAY + "  /" + cmdBase +
                        ChatColor.GOLD + " " + cmdReload +
                        ChatColor.RESET + " - " +
                        getConfig().getString("stringHelpReload", "Reloads configuration") + "\n";
            }

            sender.sendMessage(strUsage + ChatColor.DARK_GRAY + " /" + cmdBase + ChatColor.RESET + " <" + opts + ">");
            sender.sendMessage(text+text2);
            return true;
        }

        return false;
    }
}
