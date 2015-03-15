package se.bonan.playerinvites.exception;

import org.bukkit.ChatColor;

/**
 * User: Björn
 * Date: 2015-03-14
 * Time: 22:50
 */
abstract public class PlayerInvitesException extends Exception {
    public PlayerInvitesException(String message) {
        super(ChatColor.stripColor(message));
    }

    protected PlayerInvitesException(String message, Throwable cause) {
        super(ChatColor.stripColor(message), cause);
    }

    protected PlayerInvitesException(Throwable cause) {
        super(cause);
    }

    protected PlayerInvitesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(ChatColor.stripColor(message), cause, enableSuppression, writableStackTrace);
    }

    protected PlayerInvitesException() {
    }
}
