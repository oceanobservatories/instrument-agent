package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;
import com.ecwid.consul.v1.health.model.HealthService;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

public class InstrumentDiscovery {
    private final IUFStatusHandler log = UFStatus.getHandler(this.getClass());
    private Map<String, InstrumentAgent> agentMap = new HashMap<>();
    private static String serviceName = "instrument_driver";
    
    public InstrumentDiscovery() {
        Thread t = new Thread() {

            @Override
            public void run() {
                long index = 0;
                while (true) {
                    index = findDrivers(index);
                }
            }
        };
        t.start();
    }

    private synchronized long findDrivers(long index) {
        try {
            ConsulClient client = new ConsulClient("localhost");
            QueryParams qp = new QueryParams(5, index);
            Response<List<HealthService>> resp = client
                    .getHealthServices(serviceName, false, qp);
            List<HealthService> services = resp.getValue();
            index = resp.getConsulIndex();
            
            for (HealthService service : services) {
                updateMaps(service);
            }
            return index;

        } catch (Exception e) {
            log.error("Exception in findDrivers: ", e);
            try {
                // Sleep briefly to prevent runaway should there be a problem
                // with Consul
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            return 0L;
        }
    }
    
    private synchronized void updateMaps(HealthService health) {
        String serviceId = health.getService().getId();
        String refdes = serviceId.replace("instrument_driver_", "");
        boolean alive = true;

        log.debug("Discovery: refdes=" + refdes + " health=" + health);
        List<Check> checks = health.getChecks();
        for (Check check : checks) {
            if (check.getServiceName().equals(serviceName)) {
                if (check.getStatus() != CheckStatus.PASSING) {
                    alive = false;
                    break;
                }
            }
        }

        if (alive) {
            if (!agentMap.containsKey(refdes)) {
                String host = health.getNode().getAddress();
                Integer port = health.getService().getPort();
                InstrumentAgent agent = new InstrumentAgent(host, port);
                log.info("Discovery found new InstrumentAgent: refdes=" + refdes
                        + " host=" + host + " port=" + port);
                agentMap.put(refdes, agent);
            }
        } else {
            if (agentMap.containsKey(refdes)) {
                log.info("Discovery removing expired InstrumentAgent: refdes="
                        + refdes);
                agentMap.remove(refdes);
            }
        }
    }

    public InstrumentAgent getAgent(String refdes) {
        return agentMap.get(refdes);
    }
    
    public Set<String> getAgents() {
        return agentMap.keySet();
    }
}
