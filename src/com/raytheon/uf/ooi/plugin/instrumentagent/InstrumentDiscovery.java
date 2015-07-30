package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;

public class InstrumentDiscovery {
    
    private Consul consul;
    private Map agentMap = new LRUMap();
    
    public InstrumentDiscovery() {
        consul = Consul.newClient();
    }
    
    public void findInstruments() {
        HealthClient health = consul.healthClient();
        List<ServiceHealth> nodes = health.getHealthyServiceInstances("instrument_driver").getResponse();
        
        for (ServiceHealth node: nodes) {
            String address = node.getNode().getAddress();
            Integer port = node.getService().getPort();
            String serviceId = node.getService().getId();
            String[] refdes = serviceId.split("instrument_driver_");
        }
    }
    
}
