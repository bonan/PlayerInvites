package se.bonan.playerinvites.exception;

import se.bonan.playerinvites.PlayerInvites;
import se.bonan.playerinvites.Str;

/**
 * User: Björn
 * Date: 2015-03-15
 * Time: 00:38
 */
public class PlayerNotSpecifiedException extends PlayerInvitesException {

    public PlayerNotSpecifiedException() {
        super(Str.ErrorNoPlayer.format());
    }
}
