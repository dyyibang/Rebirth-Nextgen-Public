package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;

public class SendCommandMessageEvent extends Event {

    public String message;

    public SendCommandMessageEvent(String command) {
        super(Stage.Pre);
        this.message = command;
    }
}