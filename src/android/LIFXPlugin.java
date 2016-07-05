
package institute.ambrosia.lifx;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.Math;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLightCollection;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;

public class LIFXPlugin extends CordovaPlugin implements LFXLight.LFXLightListener, LFXLightCollection.LFXLightCollectionListener, LFXNetworkContext.LFXNetworkContextListener {
    public static final String TAG = "LIFX Plugin";

    private LFXNetworkContext networkContext;
    private WifiManager.MulticastLock multicastLock = null;

    private boolean networkConnected = false;
    private boolean waitingToNotifyConnection = false;
    CallbackContext onConnectCallbackContext;


    public LIFXPlugin() {}

    public void lightDidChangeLabel(LFXLight light, String label) {
        Log.i(TAG, "light did change label");
    }

    public void lightDidChangeColor(LFXLight light, LFXHSBKColor color) {
        Log.i(TAG, "light did change label");
    }

    public void lightDidChangePowerState(LFXLight light, LFXTypes.LFXPowerState powerState) {
        Log.i(TAG, "light did change power state");
    }

    public void lightCollectionDidAddLight(LFXLightCollection lightCollection, LFXLight light) {
        Log.i(TAG, "Added light: " + light.getDeviceID() + " " + light.getLabel());
    }

    public void lightCollectionDidRemoveLight(LFXLightCollection lightCollection,  LFXLight light) {
        Log.i(TAG, "light collection did remove light");
    }

    public void lightCollectionDidChangeLabel(LFXLightCollection lightCollection, String label) {
        Log.i(TAG, "light collection did change label");
    }

    public void lightCollectionDidChangeColor(LFXLightCollection lightCollection, LFXHSBKColor color) {
        Log.i(TAG, "light collection did change color");
    }

    public void lightCollectionDidChangeFuzzyPowerState(LFXLightCollection lightCollection, LFXTypes.LFXFuzzyPowerState fuzzyPowerState) {
        Log.i(TAG, "light collection did change fuzzy power state");
    }

    public void networkContextDidConnect(LFXNetworkContext networkContext) {
        Log.i(TAG, "network context did connect");
        networkConnected = true;
        if (waitingToNotifyConnection) {
            waitingToNotifyConnection = false;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            onConnectCallbackContext.sendPluginResult(pluginResult);
        }
    }

    public void networkContextDidDisconnect(LFXNetworkContext networkContext) {
        Log.i(TAG, "network context did disconnect");
        networkConnected = false;
    }

    public void networkContextDidAddTaggedLightCollection(LFXNetworkContext networkContext, LFXTaggedLightCollection collection) {
        Log.i(TAG, "network context did add tagged light collection");
    }

    public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext networkContext, LFXTaggedLightCollection collection) {
        Log.i(TAG, "network context did remove tagged light collection");
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init LIFXPlugin");

        WifiManager wifi = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("lifx_cordova_plugin_lock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        networkContext = LFXClient.getSharedInstance(cordova.getActivity().getApplicationContext()).getLocalNetworkContext();
        networkContext.addNetworkContextListener(this);
        networkContext.getAllLightsCollection().addLightCollectionListener(this);
        networkContext.connect();
    }

    private void listLights(CallbackContext callbackContext) {
        JSONArray arr = new JSONArray();
        Log.i(TAG, "Check for lights");
        for (LFXLight light : networkContext.getAllLightsCollection().getLights()) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("DeviceID", light.getDeviceID());
                obj.put("Label", light.getLabel());
            }
            catch (JSONException e) {
            }
            Log.i(TAG, "Found light: " + obj.toString());
            arr.put(obj);
        }
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, arr);
        callbackContext.sendPluginResult(pluginResult);
    }

    private LFXHSBKColor getLFXColorFromJSONRGB(JSONArray color) {
        int r, g, b;
        float[] hsv = new float[3];
        try {
            r = color.getInt(0);
            g = color.getInt(1);
            b = color.getInt(2);
            Log.i("TAG", "rgb: " + new Integer(r).toString() + " " + new Integer(g).toString() + " " + new Integer(b).toString() + " ");
            Color.RGBToHSV(r, g, b, hsv);
        }
        catch (JSONException e) {
        }
        return LFXHSBKColor.getColor(hsv[0], hsv[1], hsv[2], 6500);
    }

    private void setLightColor(CallbackContext callbackContext, JSONArray args) {
        String deviceID;
        JSONArray color;
        PluginResult pluginResult;
        try {
            Log.i(TAG, "try.. ");
            Log.i(TAG, "args are: " + args.toString());
            deviceID = args.getString(0);
            color = args.getJSONArray(1);
            Log.i(TAG, "get light .. ");
            LFXLight light = networkContext.getAllLightsCollection().getLightWithDeviceID(deviceID);
            Log.i(TAG, "got light .. ");
            if (light != null) {
                Log.i(TAG, "light is not null");
                light.setColorOverDuration(getLFXColorFromJSONRGB(color), 0);
                pluginResult = new PluginResult(PluginResult.Status.OK);
            }
            else {
                Log.i(TAG, "light is null");
                pluginResult = new PluginResult(PluginResult.Status.ERROR);
            }
            callbackContext.sendPluginResult(pluginResult);
        }
        catch (JSONException e) {
            Log.e(TAG, "setLigthcolor JSON fail");
            Log.e(TAG, e.toString());
        }
    }

    private void toggleLight(CallbackContext callbackContext, JSONArray args) {
        boolean newStatus = false;
        String deviceID;
        PluginResult pluginResult;
        try {
            deviceID = args.getString(0);
            LFXLight light = networkContext.getAllLightsCollection().getLightWithDeviceID(deviceID);
            if (light != null) {
                if (light.getPowerState() == LFXTypes.LFXPowerState.ON) {
                    Log.i(TAG, "Toggle light off");
                    light.setPowerState(LFXTypes.LFXPowerState.OFF);
                    newStatus = false;
                } else {
                    Log.i(TAG, "Toggle light on");
                    light.setPowerState(LFXTypes.LFXPowerState.ON);
                    newStatus = true;
                }
                pluginResult = new PluginResult(PluginResult.Status.OK, newStatus);
                callbackContext.sendPluginResult(pluginResult);
            }
            else {
                pluginResult = new PluginResult(PluginResult.Status.ERROR);
                callbackContext.sendPluginResult(pluginResult);
            }
        }
        catch (JSONException e) {
        }

    }

    private void waitForConnection(CallbackContext callbackContext) {
        if (networkConnected) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);
        }
        else {
            onConnectCallbackContext = callbackContext;
            waitingToNotifyConnection = true;
        }
    }

    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "action is " + action);
        if ("list".equals(action)) {
            Log.i(TAG, "List..");
            listLights(callbackContext);
        }
        else if ("toggle".equals(action)) {
            Log.i(TAG, "Toggle..");
            toggleLight(callbackContext, args);
        }
        else if ("setlightcolor".equals(action)) {
            Log.i(TAG, "Set light color..");
            setLightColor(callbackContext, args);
        }
        else if ("waitforconnection".equals(action)) {
            Log.i(TAG, "Wait for connection..");
            waitForConnection(callbackContext);
        }
        return true;
    }

}
