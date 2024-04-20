package me.rebirthclient.api.events;

public class Event {
    private final Stage stage;
    private boolean cancel;
    public Event(Stage stage) {
        this.cancel = false;
        this.stage = stage;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public void cancel() {
        this.cancel = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    public Stage getStage() {
        return stage;
    }


    public enum Stage{
        Pre, Post
    }
}
