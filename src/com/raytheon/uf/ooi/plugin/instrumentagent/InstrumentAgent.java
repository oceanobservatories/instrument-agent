package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 22, 2014            pcable      Initial creation
 * Aug 02, 2015            pcable      Implement Discovery
 * Oct 21, 2015            pcable      Add shutdown endpoint
 *
 * </pre>
 *
 * @author pcable
 * @version 1.0
 */

public class InstrumentAgent {
    protected IUFStatusHandler status = UFStatus.getHandler(InstrumentAgent.class);

    private AbstractDriverInterface driverInterface;

    public InstrumentAgent(String driverHost, int commandPort) {
        driverInterface = new ZmqDriverInterface(driverHost, commandPort);
        driverInterface.connect();
    }

    protected String sendCommand(String command, String args, String kwargs, int timeout) {
        String reply = driverInterface.sendCommand(command, args, kwargs,
                timeout);
        status.handle(Priority.INFO, "Received reply from InstrumentDriver: " + reply);
        return reply;
    }

    protected String getOverallState() {
        return sendCommand("overall_state", "[]", "{}", 2000);
    }

    protected String sendCommand(String command, int timeout) {
        return sendCommand(command, "[]", "{}", timeout);
    }

    protected String sendCommand(String command, String args, int timeout) {
        return sendCommand(command, args, "{}", timeout);
    }

    public String ping(int timeout) {
        return sendCommand(Constants.PING, "PONG", timeout);
    }

    public String initialize(String config, int timeout) {
        return sendCommand(Constants.INITIALIZE, config, timeout);
    }

    public String configure(String config, int timeout) {
        return sendCommand(Constants.CONFIGURE, config, timeout);
    }

    public String initParams(String config, int timeout) {
        return sendCommand(Constants.SET_INIT_PARAMS, config, timeout);
    }

    public String connect(int timeout) {
        return sendCommand(Constants.CONNECT, timeout);
    }

    public String disconnect(int timeout) {
        return sendCommand(Constants.DISCONNECT, timeout);
    }

    public String discover(int timeout) {
        return sendCommand(Constants.DISCOVER_STATE, timeout);
    }

    public String getMetadata(int timeout) {
        return sendCommand(Constants.GET_CONFIG_METADATA, timeout);
    }

    public String getCapabilities(int timeout) {
        return sendCommand(Constants.GET_CAPABILITIES, timeout);
    }

    public String getState(int timeout) {
        return sendCommand(Constants.GET_RESOURCE_STATE, timeout);
    }

    public String getResource(String args, int timeout) {
        return sendCommand(Constants.GET_RESOURCE, args, timeout);
    }

    public String setResource(String args, int timeout) {
        return sendCommand(Constants.SET_RESOURCE, args, timeout);
    }

    public String execute(String args, String kwargs, int timeout) {
        return sendCommand(Constants.EXECUTE_RESOURCE, args, kwargs, timeout);
    }

    public String shutdown(int timeout) {
        return sendCommand(Constants.STOP_DRIVER, timeout);
    }

    public AbstractDriverInterface getDriverInterface() {
        return driverInterface;
    }

    public void setDriverInterface(AbstractDriverInterface driverInterface) {
        this.driverInterface = driverInterface;
    }

    public Map<String, String> getInterfaceMap() {
        Map<String, String> map = new HashMap<>();
        map.put("host", driverInterface.getHost());
        map.put("port", Integer.toString(driverInterface.getPort()));
        return map;
    }
}
