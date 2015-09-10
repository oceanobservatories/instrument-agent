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

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.raytheon.uf.common.dataquery.requests.SharedLockRequest;
import com.raytheon.uf.common.dataquery.requests.SharedLockRequest.RequestType;
import com.raytheon.uf.common.dataquery.responses.SharedLockResponse;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterTask;
import com.raytheon.uf.edex.database.cluster.ClusterTaskPK;
import com.raytheon.uf.edex.database.handlers.SharedLockRequestHandler;
import com.raytheon.uf.edex.ooi.alertalarm.AlertAlarmNotifier;
import com.raytheon.uf.edex.ooi.instrument.events.Helper;




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
        log.handle(Priority.INFO, "listAgents");
        String json;
        try {
            Collection<String> agents = discovery.getAgents();
            if (agents != null)
                if (verbose) {
                    Map<String, Map<String, String>> map = new HashMap<>();
                    for (String refdes : agents) {
                        map.put(refdes,
                                discovery.getAgent(refdes).getInterfaceMap());
                    }
                    json = JsonHelper.toJson(map);
                } else {
                    json = JsonHelper.toJson(agents);
                }
            else
                json = "\"No instruments found!\"";
        } catch (IOException e) {
            json = "\"error encoding agent List: " + e.getMessage() + "\"";
            log.error("error encoding agent List: ", e);
            e.printStackTrace();
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
            resource = "DRIVER_PARAMETER_ALL";
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
            final String command, String kwargs, final int timeout) {
        final InstrumentAgent thisAgent = discovery.getAgent(id);

        if (thisAgent != null) {
            if (kwargs == null)
                kwargs = "{}";
            final String myKwargs = kwargs;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                	 
                	 try {
	                     ClusterTask ct = ClusterLockUtils.lookupLock(id, details);
	                     if (ct != null) {
	                		 ClusterTaskPK id = ct.getId();
	     		    		 log.handle(Priority.INFO, "ID name = " + id.getName() + " Details = " + id.getDetails() + " locked = " + ct.isRunning());
	                		 
	                		 // Execute the command if the instrument is not locked
	                		 if (!ct.isRunning()) {
	                			 String reply = thisAgent.execute(command, myKwargs, timeout);
	                             asyncResponse.resume(Response.ok(reply).type(MediaType.APPLICATION_JSON).build());
	                			 
	                		 } else {
	                			 asyncResponse.resume(Response.ok("Can't execute " + command + ". ID = " + id.getName() + " is locked", MediaType.APPLICATION_JSON).build());
	                			 
	                		 }
	                     } else {
	                	     log.handle(Priority.INFO, "LockUtils lookupLock returns null for Cluster Task");
	                		 asyncResponse.resume(Response.ok("LockUtils lookupLock returns null Cluster Task", MediaType.APPLICATION_JSON).build());
	                	 }
                     } catch (Exception e) {
                    	 log.error("ClusterLockUtils.lookupLock exception", e);
                    	 asyncResponse.resume(Response.ok("ClusterLockUtils.lookupLock exception", MediaType.APPLICATION_JSON).build());
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
     * @param event HTTP POST JSON list. The list contains dictionaries with each dictionary being one event.
     *                          
     */
    @Override
	public Response processEvent(String event) {
    	
    	AlertAlarmNotifier aaNotifier = AlertAlarmNotifier.getInstance();
    	
    	try {
    		List<Map<String, Object>> eventMapList = Helper.toMapList(event);
			
    		if (eventMapList != null) {		
    			aaNotifier = AlertAlarmNotifier.getInstance();
    			for ( Map<String, Object> eventMap : eventMapList) {	
					log.handle(Priority.INFO, "event Map = " + eventMap.toString());
					if (aaNotifier != null) {
					    aaNotifier.notifyOmsUser(eventMap);
					} else {
						log.handle(Priority.INFO, "AlertAlarmNotifier instance is null");
						
					}
				}			
			} else {
				log.handle(Priority.INFO, "OMS Event List is null");		
			}	
	    } catch (IOException e) {
		    log.error("process OMS event IO Exception", e);
	    }
    	return Response.status(Status.ACCEPTED).build();
	}
    
    
    /**
     * Request to lock/unlock a sensor
     * @param asyncResponse Asynchronous response
     * @param requestInfo JSON string contains reference designator, requester and request type information
     * 
     */
    @Override
    public void sharedLockRequest(final AsyncResponse asyncResponse, String requestInfo) {
        String refDesignator = "";
  	    String requester = "";
  	    final String requestType;
  	    final String LOCK_REQUEST = "lock";
  	    final SharedLockRequest request;
  	    final SharedLockRequestHandler requestHandler;
  	   	
	    try {
	        request = new SharedLockRequest();
	  	    requestHandler = new SharedLockRequestHandler();
				
	  	    Map<String, Object> requestInfoMap = Helper.toMap(requestInfo);
	  	    if (requestInfoMap != null) {
		        log.handle(Priority.INFO, "Request Info Map = " + requestInfoMap.toString());
		        refDesignator = (String) requestInfoMap.get("ref_desig");
		        requester = (String) requestInfoMap.get("requester");
		        requestType = (String) requestInfoMap.get("request_type");
		        request.setName(refDesignator);
		        request.setDetails(requester);
		    		
		        // Set request type based on the request (lock or unlock)
		        if (requestType.equals(LOCK_REQUEST)) {
		            request.setRequestType(RequestType.WRITER_LOCK);
		        } else {
		            request.setRequestType(RequestType.WRITER_UNLOCK);	
		        }
		        
		        executor.execute(new Runnable() {
	                @Override
	                public void run() {
	                	 //SharedLockResponse response;
	                     try {
	                    	 final SharedLockResponse response = requestHandler.handleRequest(request);
		     		         log.handle(Priority.INFO, "response = " + response.toString());
		     		         if (response.isSucessful()) {
		    	  		        log.handle(Priority.INFO, requestType + " is successfull");
		    	  		     } else {
		    	  		        log.handle(Priority.INFO, requestType + " is not successfull");
		    	  		     }
		                     String responseString = Helper.toJson(response);
		                     asyncResponse.resume(Response.ok(responseString, MediaType.APPLICATION_JSON).build());
		                     
	                     }  catch (Exception e) {
	                    	 log.error("handleRequest Exception", e);
	                    	 asyncResponse.resume(Response.ok("handleRequest Exception", MediaType.APPLICATION_JSON).build());
	                     }
	                }
		        });
	    
	  	    } else {
			    log.handle(Priority.INFO, "Request Info Map is null");
				asyncResponse.resume(Response.ok("Request Info Map is null", MediaType.APPLICATION_JSON).build());
		    }
		    	
	    } catch (Exception e) {
	        log.error("sharedLockRequest Exception", e);
	        asyncResponse.resume(Response.ok("sharedLockRequest Exception", MediaType.APPLICATION_JSON).build());
	    }
    }
    
    /**
     * Get a shared lock from Postgres database 
     * @param asyncReponse Asynchronous response
     * @param lockInfo JSON String contains reference designator and lock owner information
     */
    @Override
    public void getSharedLock(final AsyncResponse asyncResponse, String lockInfo) {
    	final String refDesignator = "ref_desig";
    	final String lockOwner = "lock_owner";
    	log.handle(Priority.INFO, "getSharedLock executes...");
    	
    	try {
	    	Map<String, Object> lockInfoMap = Helper.toMap(lockInfo);
			if (lockInfoMap != null) {
	    		log.handle(Priority.INFO, "Lock Info Map = " + lockInfoMap.toString());
	    		final String name = (String) lockInfoMap.get(refDesignator);
	    		final String details = (String) lockInfoMap.get(lockOwner);
	    		if (name != null && details != null) {
		    		executor.execute(new Runnable() {
		                 @Override
		                 public void run() {
		                     try {
			                     ClusterTask ct = ClusterLockUtils.lookupLock(name, details);
			                     if (ct != null) {
			                		 ClusterTaskPK id = ct.getId();
			     		    		 log.handle(Priority.INFO, "ID name = " + id.getName() + " Details = " + id.getDetails() + " Running = " + ct.isRunning());
			                		 String ctString = Helper.toJson(ct);
			                         asyncResponse.resume(Response.ok(ctString).type(MediaType.APPLICATION_JSON).build());
			                	 } else {
			                	     log.handle(Priority.INFO, "LockUtils lookupLock returns null for Cluster Task");
			                		 asyncResponse.resume(Response.ok("LockUtils lookupLock returns null Cluster Task", MediaType.APPLICATION_JSON).build());
			                	 }
		                     } catch (IOException e) {
		                    	 log.error("Helper to Json Exception", e);
		                    	 asyncResponse.resume(Response.ok("Helper to Json Exception", MediaType.APPLICATION_JSON).build());
		                     }
		                 }
		             });
	    		}
			}
    	} catch (Exception e) {
    		log.error("getSharedLock Exception", e);
    		 asyncResponse.resume(Response.ok("getSharedLock Exception", MediaType.APPLICATION_JSON).build());
    	}    
    }
    

	private Response agentNotFound() {
        // TODO
        return Response.status(404).build();
    }
}
