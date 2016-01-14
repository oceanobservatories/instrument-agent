package com.raytheon.uf.ooi.plugin.instrumentagent;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.ooi.alertalarm.AlertAlarmNotifier;

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
 * Jan 14, 2016 3310       pcable      Fix /ping and /resource
 *
 * </pre>
 *
 * @author pcable
 * @version 1.0
 */

@Path("/instrument")
public class InstrumentAgentWebImpl implements IAgentWebInterface {
    private final IUFStatusHandler log = UFStatus.getHandler(this.getClass());
    private final Executor executor;
    private String contentPathString;
    private InstrumentDiscovery discovery;

    public InstrumentAgentWebImpl(String basePath) {
        discovery = new InstrumentDiscovery();
        
        IPathManager pathManager = PathManagerFactory.getPathManager();
        LocalizationContext context = pathManager.getContext(LocalizationType.EDEX_STATIC, LocalizationLevel.BASE);

        File contentPath = pathManager.getFile(context, basePath);
        if (!contentPath.exists()) {
            throw new IllegalArgumentException("Unable to find web agent static resources at " + contentPath);
        }
        contentPathString = contentPath.getAbsolutePath();

        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("agentWebAsync-%d")
                .setDaemon(true).build());
    }

    @Override
    public Response listAgents(boolean verbose) {
        log.handle(Priority.DEBUG, "listAgents");
        String json;

        Collection<String> agents = discovery.getAgents();

        if (verbose) {
            Map<String, Map<String, String>> map = new HashMap<>();
            for (String refdes : agents) {
                map.put(refdes, discovery.getAgent(refdes).getInterfaceMap());
            }
            json = JsonHelper.toJson(map);
        } else {
            json = JsonHelper.toJson(agents);
        }

        return Response.ok(json).build();
    }

    @Override
    public void getAgent(final AsyncResponse asyncResponse, String id) {
        log.handle(Priority.INFO, "getAgent: " + id);
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            asyncResponse.resume(Response.ok(thisAgent.getOverallState(), MediaType.APPLICATION_JSON).build());
        }
    }

    @Override
    public void ping(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.ping(timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });

        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void initialize(final AsyncResponse asyncResponse, final String id, final String config, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.initialize(config, timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void configure(final AsyncResponse asyncResponse, final String id, final String config, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.configure(config, timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void initParams(final AsyncResponse asyncResponse, final String id, final String config, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.initParams(config, timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void connect(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.connect(timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void disconnect(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.disconnect(timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void discover(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.discover(timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void getMetadata(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.getMetadata(timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void getCapabilities(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.getCapabilities(timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void getState(final AsyncResponse asyncResponse, final String id, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.getState(timeout);
                    log.handle(Priority.INFO, "Received reply, calling resume...");
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void getResource(final AsyncResponse asyncResponse, final String id, String resource, final int timeout) {
        if (resource == null)
            resource = "\"DRIVER_PARAMETER_ALL\"";
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            final String myResource = resource;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.getResource(myResource, timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void setResource(final AsyncResponse asyncResponse, final String id, final String resource, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.setResource(resource, timeout);
                    asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public void execute(final AsyncResponse asyncResponse, final String id, final String details, 
 final String command, String kwargs,
            final String key, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);

        if (thisAgent != null) {
            if (kwargs == null)
                kwargs = "{}";
            final String myKwargs = kwargs;
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    String locker = InstrumentAgentLock.get(id);
                    if (locker == "" || locker == key) {
                        String reply = thisAgent.execute(command, myKwargs,
                                timeout);
                        asyncResponse.resume(Response.ok(reply)
                                .type(MediaType.APPLICATION_JSON).build());

                    } else {
                        String msg = "Can't execute " + command + ". ID = " + id
                                + " is locked";
                        asyncResponse.resume(Response.status(409)
                                .entity(JsonHelper.toJson(msg))
                                .type(MediaType.APPLICATION_JSON).build());

                    }
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }

    @Override
    public Response getApp() {
        try {
            return Response.seeOther(new URI("instrument/app/index.html")).build();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    @Override
    public Response getStatic(String path) {
        try {
            String resource = new String(Files.readAllBytes(Paths.get(contentPathString, path)));
            if (path.endsWith(".js"))
                return Response.ok(resource, MediaType.APPLICATION_JSON_TYPE).build();
            if (path.endsWith(".html"))
                return Response.ok(resource, MediaType.TEXT_HTML_TYPE).build();
            return Response.ok(resource).build();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.noContent().build();
        }
    }

    /**
     * Process events from OMS Server
     * 
     * @param event
     *            HTTP POST JSON list. The list contains dictionaries with each
     *            dictionary being one event.
     * 
     */
    @Override
    public Response processEvent(String event) {

        AlertAlarmNotifier aaNotifier = AlertAlarmNotifier.getInstance();

        try {
            List<Map<String, Object>> eventMapList = JsonHelper
                    .toMapList(event);

            if (eventMapList != null) {
                aaNotifier = AlertAlarmNotifier.getInstance();
                for (Map<String, Object> eventMap : eventMapList) {

                    log.handle(Priority.DEBUG,
                            "event Map = " + eventMap.toString());
                    aaNotifier.notifyOmsUser(eventMap);
                }
            } else {
                log.handle(Priority.ERROR,
                        "Expected OMS Events JSON dictionary list as input to process event: "
                                + event);
            }
        } catch (IOException e) {
            log.error("Error processing OMS Events", e);
        }
        return Response.status(Status.ACCEPTED).build();
    }
    
    private Response agentNotFound() {
        return Response.status(404).build();
    }

    @Override
    public void lockInstrument(final AsyncResponse asyncResponse,
            final String id,
            final String key) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> response = new HashMap<>();
                response.put("id", id);
                response.put("key", key);
                response.put("locked-by", key);
                if (InstrumentAgentLock.lock(id, key)) {
                    log.info("Locked instrument: " + id + " with key: " + key);
                    asyncResponse.resume(Response.ok()
                            .entity(JsonHelper.toJson(response))
                            .type(MediaType.APPLICATION_JSON_TYPE).build());
                } else {
                    // Lock already held
                    String locker = InstrumentAgentLock.get(id);
                    String msg = "Failed to lock instrument: " + id
                            + " with key: " + key;
                    response.put("locked-by", locker);
                    response.put("message", msg);
                    log.warn(msg);
                    asyncResponse.resume(Response.status(409)
                            .entity(JsonHelper.toJson(response))
                            .type(MediaType.APPLICATION_JSON_TYPE).build());
                }
            }
        });
    }

    @Override
    public void unlockInstrument(final AsyncResponse asyncResponse,
            final String id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (InstrumentAgentLock.unlock(id)) {
                    asyncResponse.resume(Response.ok().build());
                    log.info("Unlocked instrument: " + id);
                } else {
                    String msg = "Failed to unlock instrument: " + id;
                    log.warn(msg);
                    asyncResponse.resume(Response.serverError()
                            .entity(JsonHelper.toJson(msg))
                            .type(MediaType.APPLICATION_JSON_TYPE).build());
                }
            }
        });
    }

    @Override
    public void getLockStatus(final AsyncResponse asyncResponse, final String id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String locker = InstrumentAgentLock.get(id);
                asyncResponse
                        .resume(Response.ok().entity(JsonHelper.toJson(locker))
                                .type(MediaType.APPLICATION_JSON_TYPE).build());
            }
        });
    }

    @Override
    public void shutdown(final AsyncResponse asyncResponse, final String id,
            final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);
        if (thisAgent != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String reply = thisAgent.shutdown(timeout);
                    asyncResponse.resume(Response.ok(reply)
                            .type(MediaType.APPLICATION_JSON).build());
                }
            });
        } else {
            asyncResponse.resume(agentNotFound());
        }
    }
}
