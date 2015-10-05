package com.raytheon.uf.ooi.plugin.instrumentagent;

import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

public class InstrumentAgentLock {

    private final static String details = "INSTRUMENT_AGENT_LOCK";
    private final static long lockDuration = 86400000; // Locks time out after 1
                                                       // day

    public static boolean unlock(String id) {
        return ClusterLockUtils.unlock(id, details);
    }

    public static boolean lock(String id, String key) {
        ClusterTask ct = ClusterLockUtils.lock(id, details, key, lockDuration,
                false);
        return ct.getExtraInfo().equals(key);
    }

    public static String get(String id) {
        ClusterTask ct = ClusterLockUtils.lookupLock(id, details);
        if (ct != null) {
            if (ct.isRunning()) {
                return ct.getExtraInfo();
            }
            return "";
        }
        return "";
    }
}
