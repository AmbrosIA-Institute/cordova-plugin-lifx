var exec = require('cordova/exec');

function LIFXPlugin() { 
    console.log("LIFXPlugin.js: is created");
}

LIFXPlugin.prototype.list = function(success, failure) {
    console.log("LIFXPlugin.js: list");
    exec(success, failure, "LIFXPlugin", "list", []);
}

LIFXPlugin.prototype.toggle = function(deviceID, success, failure) {
    console.log("LIFXPlugin.js: toggle");
    exec(success, failure, "LIFXPlugin", "toggle", [deviceID]);
}

LIFXPlugin.prototype.setlightcolor = function(deviceID, color, success, failure) {
    console.log("LIFXPlugin.js: setlightcolor");
    exec(success, failure, "LIFXPlugin", "setlightcolor", [deviceID, color]);
}

LIFXPlugin.prototype.waitforconnection = function(success, failure) {
    console.log("LIFXPlugin.js: waitforconnection");
    exec(success, failure, "LIFXPlugin", "waitforconnection", []);
}

var lifxPlugin = new LIFXPlugin();
module.exports = lifxPlugin;
