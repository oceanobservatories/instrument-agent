package com.raytheon.uf.ooi.plugin.instrumentagent;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;

import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Concrete implementation of the Instrument Driver interface for ZMQ
 */

public class ZmqDriverInterface extends AbstractDriverInterface {
    private ZContext context;
    private String commandUrl;
    private int port;
    private String host;
    private final int commandTimeout = 10000;

    public ZmqDriverInterface(String host, int port) {
        this.host = host;
        this.port = port;
        buildCommandUrl();
    }

    public void connect() {
        context = new ZContext();
        context.setLinger(0);
    }

    @Override
    protected synchronized String _sendCommand(String command) {
        status.handle(Priority.INFO, "Sending command: " + command);
        // Send the command
        ZMQ.Socket commandSocket;
        commandSocket = context.createSocket(ZMQ.REQ);
        commandSocket.connect(commandUrl);
        commandSocket.setLinger(0);
        commandSocket.send(command);

        // Get the response
        PollItem items[] = { new PollItem(commandSocket, Poller.POLLIN) };
        int rc = ZMQ.poll(items, commandTimeout);

        if (rc == -1)
            // INTERRUPTED
            return null;
        String reply = null;
        
        for (PollItem item: items) {
            if (item.isReadable()) {
                reply = commandSocket.recvStr();
                status.handle(Priority.DEBUG, "ZMQ received: " + reply);
                break;
            }
        }
        if (reply == null) {
            status.handle(Priority.INFO, "Empty message received from command: " + command);
        }
        
        commandSocket.close();
        return reply;
    }


    public void shutdown() {
        if (context != null) {
            status.handle(Priority.INFO, "Closing ZMQ context");
            for (ZMQ.Socket socket : context.getSockets()) {
                socket.setLinger(0);
                socket.close();
            }
        }
        context.close();
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
        buildCommandUrl();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
        buildCommandUrl();
    }

    public void buildCommandUrl() {
        commandUrl = String.format("tcp://%s:%d", host, port);
    }
}