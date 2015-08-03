package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

import com.raytheon.uf.common.dataplugin.sensorreading.SensorReadingRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.ooi.cassandra.DataParticleJson;
import com.raytheon.uf.edex.ooi.decoder.dataset.AbstractParticleDecoder;

public class SampleAccumulator implements Runnable {
    private IUFStatusHandler statusHandler = UFStatus.getHandler(this.getClass());
    private Map<String, List<DataParticleJson>> particleMap = new HashMap<>();
    private long PUBLISH_INTERVAL = 5000;

    @EndpointInject(uri = "direct-vm:generate?timeout=720000")
    protected ProducerTemplate producer;

    public synchronized void process(Map<String, Object> particle, String sensor) throws Exception {
        DataParticleJson p = new DataParticleJson(particle);
        p.setSensor(sensor);
        p.setMethod("streamed");
        if (!particleMap.containsKey(p.getStream_name())) {
            List<DataParticleJson> l = new LinkedList<>();
            particleMap.put(p.getStream_name(), l);
        }
        particleMap.get(p.getStream_name()).add(p);
    }

    public void publish() {
        long now = System.currentTimeMillis();
        Map<String, List<DataParticleJson>> records;
        synchronized (particleMap) {
            records = particleMap;
            particleMap = new HashMap<>();
        }

        if (!records.isEmpty()) {
            statusHandler.handle(Priority.INFO, "Going to publish particles");
            Map<String, Object> headers = new HashMap<>();
            headers.put("enqueueTime", now);
            headers.put("dequeueTime", now);
            producer.sendBodyAndHeaders(records, headers);
        }
    }

    public void run() {
        while (true) {
            try {
                publish();
                Thread.sleep(PUBLISH_INTERVAL);
            } catch (Exception e) {
                statusHandler.handle(Priority.CRITICAL, "Ignoring exception in InstrumentAgent publish loop: " + e);
            }
        }
    }
}
