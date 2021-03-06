package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Abstract class representing a generic interface to an Instrument Driver
 */

public abstract class AbstractDriverInterface extends Observable {
    protected IUFStatusHandler status = UFStatus.getHandler(AbstractDriverInterface.class);
    protected int DEFAULT_TIMEOUT = 60;

    protected abstract String _sendCommand(String command, int timeout);

    protected abstract void connect();

    protected abstract void shutdown();

    protected abstract void setHost(String host);

    protected abstract String getHost();

    protected abstract void setPort(int port);

    protected abstract int getPort();

    protected void handleException(List<?> exception) {
        // TODO - alert user
        status.handle(Priority.ERROR, "handleException: " + exception);
    }

    protected String sendCommand(String command, String args, String kwargs,
            int timeout) {
        String json = "{\"cmd\": \"" + command + "\", \"args\": " + args + ", \"kwargs\": " + kwargs + "}";
        status.handle(Priority.DEBUG, "Preparing to send: " + json);
        try {
            // parse json to verify validity prior to sending...
            JsonHelper.toMap(json);
            return _sendCommand(json, timeout);
        } catch (Exception e) {
            return failedCommand(json, e);
        }
    }

    private String failedCommand(String command, Exception e) {
        Map<String, Object> map = new HashMap<>();
        map.put("cmd", command);
        map.put("reply", "FAIL: " + e);
        return JsonHelper.toJson(map);
    }
}
