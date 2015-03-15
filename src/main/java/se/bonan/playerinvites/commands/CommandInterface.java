package se.bonan.playerinvites.commands;

import org.bukkit.entity.Player;

/**
 * User: Björn
 * Date: 2015-03-14
 * Time: 21:17
 */
public interface CommandInterface {

    public String invoke(Player player, String[] args);
    public Boolean hasPerm(Player player);
    public String usage(Player player);
    public String help(Player player);
    public String getName();

}
