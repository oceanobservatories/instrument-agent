package com.raytheon.uf.ooi.plugin.instrumentagent;

/**
 * Created by pcable on 8/15/14.
 */
public class Constants {
    private Constants() {}

    // Driver Commands
    public final static String PING                     = "driver_ping";
    public final static String INITIALIZE               = "initialize";
    public final static String CONFIGURE                = "configure";
    public final static String CONNECT                  = "connect";
    public final static String DISCONNECT               = "disconnect";
    public final static String GET_INIT_PARAMS          = "get_init_params";
    public final static String SET_INIT_PARAMS          = "set_init_params";
    public final static String APPLY_STARTUP_PARAMS     = "apply_startup_params";
    public final static String GET_CACHED_CONFIG        = "get_cached_config";
    public final static String GET_CONFIG_METADATA      = "get_config_metadata";
    public final static String DISCOVER_STATE           = "discover_state";
    public final static String GET_RESOURCE_STATE       = "get_resource_state";
    public final static String GET_RESOURCE             = "get_resource";
    public final static String SET_RESOURCE             = "set_resource";
    public final static String START_DIRECT             = "start_direct";
    public final static String EXECUTE_DIRECT           = "execute_direct";
    public final static String STOP_DIRECT              = "stop_direct";
    public final static String GET_CAPABILITIES         = "get_resource_capabilities";
    public final static String EXECUTE_RESOURCE         = "execute_resource";
    public final static String STOP_DRIVER              = "stop_driver_process";
    public final static int    DEFAULT_TIMEOUT			= 600;

    // Driver States
    public final static String DRIVER_STATE_UNCONFIGURED = "DRIVER_STATE_UNCONFIGURED";
    public final static String DRIVER_STATE_DISCONNECTED = "DRIVER_STATE_DISCONNECTED";
    public final static String DRIVER_STATE_UNKNOWN      = "DRIVER_STATE_UNKNOWN";

    // Driver Events
    public final static String STATE_CHANGE_EVENT       = "DRIVER_ASYNC_EVENT_STATE_CHANGE";
    public final static String SAMPLE_EVENT             = "DRIVER_ASYNC_EVENT_SAMPLE";
    public final static String CONFIG_CHANGE_EVENT      = "DRIVER_ASYNC_EVENT_CONFIG_CHANGE";
    public final static String DRIVER_SYNC_EVENT 		= "DRIVER_SYNCHRONOUS_EVENT_REPLY";
    public final static String DRIVER_ASYNC_EVENT 		= "DRIVER_AYSNC_EVENT_REPLY";
    public final static String DRIVER_ASYNC_FUTURE 		= "DRIVER_ASYNC_EVENT_FUTURE";
	public final static String DRIVER_BUSY 				= "DRIVER_BUSY_EVENT";
    public final static String DRIVER_EXCEPTION 		= "DRIVER_EXCEPTION_EVENT";
    
    //Provenance Keys
    public final static String REFERENCE_DESIGNATOR     = "refdes";
    public final static String DRIVER_MODULE = "driver_module";
    public final static String DRIVER_CLASS = "driver_class";
    public final static String DRIVER_VERSION = "driver_version";
    public final static String DRIVER_HOST = "driver_host";

    // Driver Streams
    public static final String STREAM_NAME              = "stream_name";
    public static final String QUALITY_FLAG             = "quality_flag";
    public static final String PREFERRED_TIMESTAMP      = "preferred_timestamp";
    public static final String PORT_TIMESTAMP           = "port_timestamp";
    public static final String DRIVER_TIMESTAMP         = "driver_timestamp";
    public static final String PKT_FORMAT_ID            = "pkt_format_id";
    public static final String PKT_VERSION              = "pkt_version";
    public static final String VALUE                    = "value";
    public static final String VALUES                   = "values";
    public static final String VALUE_ID                 = "value_id";
}
