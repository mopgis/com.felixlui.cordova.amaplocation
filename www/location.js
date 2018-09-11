var cordova = require('cordova');

function LocationPlugin() { }

LocationPlugin.prototype.startWatchLocation = function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "LocationPlugin", "startwatchlocation", []);
};

LocationPlugin.prototype.stopWatchLocation = function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "LocationPlugin", "stopwatchlocation", []);
};


module.exports = new LocationPlugin();
