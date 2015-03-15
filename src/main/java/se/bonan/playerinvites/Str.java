package se.bonan.playerinvites;

/**
 * Strings used throughout the plugin
 *
 * Any string here can be overriden in config by setting variable "string<name>"
 * Example:
 *   stringBuySuccess: "Congratulations! You have bought %1 invites for a cost of %2"
 */
public enum Str {

    // 0 = BLACK,
    // 1 = DARK_BLUE,
    // 2 = DARK_GREEN,
    // 3 = DARK_AQUA,
    // 4 = DARK_RED,
    // 5 = DARK_PURPLE,
    // 6 = GOLD,
    // 7 = GRAY,
    // 8 = DARK_GRAY,
    // 9 = BLUE,
    // a = GREEN,
    // b = AQUA,
    // c = RED,
    // d = LIGHT_PURPLE,
    // e = YELLOW,
    // f = WHITE,
    // k = MAGIC,
    // l = BOLD,
    // m = STRIKETHROUGH,
    // n = UNDERLINE,
    // o = ITALIC,
    // r = RESET

    Usage("Usage:"),
    Player("player"),
    Amount("amount"),
    PlayerName("§b%1§r"),
    And("§rand§b"),
    Help("  §7/%c §6%1§r - %2"),
    HelpUsage("Usage: §7/%c §r<§6%1§r>"),
    HelpArg("§r<§a%1§r>"),
    HelpOpt("§r[§a%1§r]"),
    HelpCmdSep("§r|§6"),
    HelpShow("Show player invites"),
    HelpShowYour("Show your invites"),
    HelpUse("Invite another player to server"),
    HelpBuy("Buy invites"),
    HelpReload("Reload configuration and data"),
    HelpBuyCost("One invite costs %1"), // %1 = Amount + Currency
    HelpSet("Set number of invites for a player"),
    HelpGive("Give a player invites"),
    ShowInvitesNone("§b%1§a has §bno§a invites available"), // %1 = Player name
    ShowInvitesOne("§b%1§a has §bone§a invite available"), // %1 = Player name, %2 = count
    ShowInvites("§b%1§a has §b%2§a invites available"),
    ShowInvitesSelfNone("§aYou have §bno§a invites available"),
    ShowInvitesSelfOne("§aYou have §bone§a invite available"),
    ShowInvitesSelf("§aYou have §b%2§a invites available"),
    ShowInvitedBy("§b%1§a was invited by §b%2"), // %1 = Player name, %2 = Inviter
    ShowInvitedNone("§b%1§a hasn't invited anyone yet"),
    ShowInvited("§b%1§a has invited: §b%2"),
    ShowInvitedSep("§r, §b"),
    GiveInviteOne("§b%1§a has been given §bone§a new invite."),
    GiveInvite("§b%1§a has been given §b%2§a new invites."),
    GivenInviteOne("§aYou have been given §bone§a additional invite."),
    GivenInvite("§aYou have been given §b%1§a additional invites."),
    GivenInviteUsage("§aInvite new players with §7/%c §6%u §r<§aplayer§r>"),
    GivenInvitePeriodic("§aYou have received §b%1§a new invites."),
    GivenInvitePeriodicOne("§aYou have received §bone§a new invite."),
    InviteSuccess("§b%1§a has been invited."),
    BuySuccess("§aBought §b%1§a invites for §b%2"),
    ReloadSuccess("§aConfiguration and data reloaded successfully"),

    ErrorPermission("§cYou don't have permission to use that command"),
    ErrorConsole("§cThis command cannot be run from console"),
    ErrorNoInvites("§cYou have no invites available"),
    ErrorInvite("§cUnable to invite §b%1§c"),
    ErrorInviteExists("§c%1 has already been invited"),
    ErrorNoPlayer("§cNo player specified"),
    ErrorNotFound("§cPlayer §b%1§c was not found"),
    ErrorBuyDisabled("§cBuying invites is disabled"),
    ErrorBuyNoFundsOne("§cYou don't have enough money to buy an invite, another §e%1§c is needed"),
    ErrorBuyNoFunds("§cYou don't have enough money to buy %1 invites, another §e%2§c is needed"),
    ErrorBuy("§cBuy failed");

    private String def;
    private Str(String def) {
        this.def = def;
    }
    public String toString() {
        return PlayerInvites.config.getString("string" + this.name(), def).replace("%%","%");
    }
    public String str() {
        return toString();
    }
    public String def() {
        return def;
    }
    public String format(String ... args) {
        String string = PlayerInvites.config.getString("string" + this.name(), def);
        if (string.contains("%c"))
            string = string.replace("%c", PlayerInvites.cmd);
        if (string.contains("%u"))
            string = string.replace("%u", PlayerInvites.config.getString("cmdUse", "use"));
        for (int i = args.length; i > 0; i--) {
            if (string.contains("%"+i))
                string = string.replace("%"+i, args[i-1]);
        }

        return string.replace("%%", "%");
    }

    public static String getConfig() {
        StringBuilder conf = new StringBuilder("\r\n\r\n##\r\n" +
                "## Language/strings configuration\r\n" +
                "## Uncomment lines to replace strings with localized version\r\n" +
                "##\r\n");

        for (Str str: Str.values()) {
            conf.append("#string")
                    .append(str.name())
                    .append(": \"")
                    .append(str.def().replace("\"", "\\\""))
                    .append("\"\r\n");
        }

        return conf.toString();
    }
}