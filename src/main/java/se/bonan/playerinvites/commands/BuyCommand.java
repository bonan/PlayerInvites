package se.bonan.playerinvites.commands;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;

/**
 * Command that allows a player to buy invites from the server
 * Integrates with Vault for Economy
 *
 * Usage:
 *   /invite buy [amount]
 *
 * Config:
 *   buyInvite: Set to true to enable buying of invites
 *   buyInvitePrice: Price per invite
 *
 * Permission:
 *   invites.buy - Allows a player to use the command
 */
public class BuyCommand extends CommandBase implements CommandInterface
{

    public BuyCommand(String name, PlayerInvites plugin) {
        super(name, plugin, "invites.buy");
    }

    @Override
    public String invoke(Player player, String[] args) {

        /**
         * Don't allow console to run command
         */
        if (player == null)
            return Str.ErrorConsole.str();

        /**
         * Number of invites to buy
         * Default: 1
         */
        Integer count = 1;
        if (args.length == 1) {
            try {
                /** Sets number of invites from argument */
                count = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                return usagePrefix(player);
            }
        }
        /**
         * Show usage if more than 1 argument is provided
         * Or if the player tries to buy zero or negative amounts
         * of invites
         */
        if (args.length > 1 || count < 1)
            return usagePrefix(player);

        /**
         * Get Economy integration (Vault) and check if buying is enabled
         */
        Economy economy = plugin.getEconomy();
        if (economy == null || !plugin.getConfig().getBoolean("buyInvite", false)) {
            return Str.ErrorBuyDisabled.str();
        }

        /**
         * Get the price per invite from configuration and calculate the total amount
         */
        Double price = plugin.getConfig().getDouble("buyInvitePrice", 5000);
        Double amount = price * count;

        /**
         * Don't allow buying of invites for zero or negative amounts of money
         */
        if (amount <= 0)
            return Str.ErrorBuy.str();

        /**
         * Get player balance and check if there is enough funds
         */
        Double balance = economy.getBalance(player);
        if (balance < amount) {
            if (count == 1)
                return Str.ErrorBuyNoFundsOne.format((amount-balance) + " " + economy.currencyNameSingular());
            return Str.ErrorBuyNoFunds.format(count.toString(), (amount-balance) + " " + economy.currencyNamePlural());
        }

        /**
         * Withdraw money from player
         */
        EconomyResponse r = economy.withdrawPlayer(player, amount);
        if (r.transactionSuccess()) {
            /**
             * Create new invites and save data
             */
            plugin.getPlayerData(player).addInvites(count);
            plugin.save();
            return Str.BuySuccess.format(
                    count.toString(),
                    amount + " " + (
                            amount == 1?
                                    economy.currencyNameSingular():
                                    economy.currencyNamePlural()
                    )
            );
        }

        /**
         * Something went wrong with withdraw
         */
        return Str.ErrorBuy.str();

    }

    @Override
    public String usage(Player player) {
        /**
         * /invite buy [amount]
         */
        return usageStr(Str.HelpBuy.str(),
                Str.HelpOpt.format(Str.Amount.str())
        );
    }

    @Override
    public String help(Player player) {
        Double price = plugin.getConfig().getDouble("buyInvitePrice");
        Economy economy = plugin.getEconomy();
        if (economy == null)
            return "";
        return Str.HelpBuyCost.format(price + " " +
                (price == 1?
                    economy.currencyNameSingular():
                    economy.currencyNamePlural())
        );
    }


}
