package org.plugin;

public class User {
    private String username;
    private String inviteCode;
    private String ownInviteCode;
    private boolean isSpectator;

    public User(String username) {
        this.username = username;
        this.isSpectator = true;
    }

    public String getUsername() {
        return username;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getOwnInviteCode() {
        return ownInviteCode;
    }

    public void setOwnInviteCode(String ownInviteCode) {
        this.ownInviteCode = ownInviteCode;
    }

    public boolean isSpectator() {
        return isSpectator;
    }

    public void setSpectator(boolean isSpectator) {
        this.isSpectator = isSpectator;
    }
}