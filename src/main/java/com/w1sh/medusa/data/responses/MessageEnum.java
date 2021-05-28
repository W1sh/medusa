package com.w1sh.medusa.data.responses;

public enum MessageEnum {
    PING_SUCCESS("message.event.ping.success"),
    ROLL_START("message.event.roll.start"),
    ROLL_RESULT("message.event.roll.result"),
    ROLL_ERROR("message.event.roll.error"),
    ROULETTE_RESULT("message.event.roulette.result"),
    ROULETTE_ERROR("message.event.roulette.error"),
    WHENPOINTS("message.event.whenpoints"),
    BLOCKLIST("message.event.blocklist"),
    BLOCKLIST_ERROR("message.event.blocklist.error"),
    BLOCKLIST_SHOW_ERROR("message.event.blocklist.show.error"),
    BLOCKLIST_ADD_SUCCESS("message.event.blocklist.add.success"),
    BLOCKLIST_ADD_ERROR("message.event.blocklist.add.error"),
    BLOCKLIST_REMOVE_SUCCESS("message.event.blocklist.remove.success"),
    BLOCKLIST_REMOVE_ERROR("message.event.blocklist.remove.error"),
    NOLINKS("message.event.nolinks"),
    UPTIME_SUCCESS("message.event.uptime.success"),
    DISABLE_SINGLE_SUCCESS("message.event.disable.single.success"),
    DISABLE_SINGLE_ERROR("message.event.disable.single.error"),
    DISABLE_MULTIPLE_SUCCESS("message.event.disable.multiple.success"),
    DISABLE_MULTIPLE_ERROR("message.event.disable.multiple.error"),
    NOGAMBLING("message.event.nogambling"),
    POINTS("message.event.points"),
    RULES_ERROR("message.event.rules.error"),
    RULES_ACTIVATED("message.event.rules.activated"),
    RULES_DEACTIVATED("message.event.rules.deactivated"),
    MOVETIME_ERROR("message.event.movetime.error"),
    JOIN_SUCCESS("message.event.voice.join.success"),
    JOIN_ERROR("message.event.voice.join.error"),
    LEAVE_SUCCESS("message.event.voice.leave.success"),
    LEAVE_ERROR("message.event.voice.leave.error"),
    LOOP_SUCCESS("message.event.loop.success"),
    LOOP_ERROR("message.event.loop.error"),
    PLAYLIST_DELETE_SUCCESS("message.event.playlist.delete.success"),
    PLAYLIST_DELETEALL_SUCCESS("message.event.playlist.deleteall.success"),
    PLAYLIST_LOAD_SUCCESS("message.event.playlist.load.success"),
    PLAYLIST_SAVE_SUCCESS("message.event.playlist.save.success"),
    PLAYLIST_SAVE_ERROR("message.event.playlist.save.error"),
    PLAYLIST_ERROR("message.event.playlist.error"),
    PLAYER_PAUSE("message.player.pause"),
    PLAYER_RESUME("message.player.resume"),
    PLAYER_SKIP("message.player.skip"),
    PLAYER_REMOVE_SUCCESS("message.player.remove.success"),
    PLAYER_REMOVE_ERROR("message.player.remove.error"),
    PLAYER_CLEAR("message.player.clear"),
    PLAYER_SHUFFLE("message.player.shuffle"),
    PLAYER_PLAYLIST_LOAD("message.player.playlist.load"),
    VALIDATOR_ARGUMENTS_ERROR("message.validator.arguments.error"),
    VALIDATOR_PERMISSIONS_ERROR("message.validator.permissions.error"),
    CHANGE_PREFIX_SUCCESS("message.event.prefix.success");

    private final String messageKey;

    MessageEnum(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
