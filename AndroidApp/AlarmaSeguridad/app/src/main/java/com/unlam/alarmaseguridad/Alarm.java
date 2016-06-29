package com.unlam.alarmaseguridad;

import org.json.JSONArray;

public class Alarm {
    private boolean activated = false;
    private boolean change = false;
    private boolean ringing = false;
    private boolean panic = false;
    private String temperature = "";
    private String gas = "";
    private String logs = "";

    public boolean isChange() {
        return change;
    }

    public void setChange(boolean change) {
        this.change = change;
        this.setSendMessage(true);
    }
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public boolean isSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(boolean sendMessage) {
        this.sendMessage = sendMessage;
    }

    private boolean sendMessage = false;

    private static Alarm instance = null;

    protected Alarm() {
        // Exists only to defeat instantiation.
    }

    public static Alarm getInstance() {
        if(instance == null) {
            instance = new Alarm();
        }
        return instance;
    }

    public boolean isPanic() {
        return panic;
    }

    public void setPanic(boolean panic) {
        if(isPanic() != panic) {
            this.panic = panic;
            this.setSendMessage(true);
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isRinging() {
        return ringing;
    }

    public void setActivated(boolean activated) {
        if(isActivated() != activated){
            this.activated = activated;
        }
    }

    public void setRinging(boolean ringing) {
        this.ringing = ringing;
    }
}
