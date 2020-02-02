# GPS_Android

## Description
This repository contains all code related to the Android app of TU Ilmenau group project called 
<b>Performance analysis framework for base station placement using IEEE 802.11</b>

## Functionality
The app allows sending device location to the 'MessageBroker' component using MQTT queue. Available interaction options:
- current IP address shown
- switcher to permit continuos address sending
- new attempt time-out after fail adjustment
- manual address pushing

## System requirements
- Android Studio
- min SDK version 14
- target SDK version 29

## How to use
1. clone the repository
2. import to Android Studio
3. establish connection MQTT broker
4. launch on virtual or a real device, they must be connected over wifi network.


### Download

* HTTP download 1Mo from `http://ipv4.ikoula.testdebit.info`


### Upload

* HTTP upload 1Mo to `http://ipv4.ikoula.testdebit.info`

Links can be customized in 
```
    Config.class 
```

The 2 following links describe the process of speedtest.net :

http://www.ookla.com/support/a21110547/what-is-the-test-flow-and-methodology-for-the-speedtest
https://support.speedtest.net/hc/en-us/articles/203845400-How-does-the-test-itself-work-How-is-the-result-calculated-

### Data

request payload:

```
{
  "time": 0,
  "messageType": "", // "raw", "wifi"
  "device": {
    "id": "",
    "deviceType": ""// "handy","UAV"
  },
  "payload": {
    "infoType": "",
    "ssid": "",
    "bssid": "",
    "signal": {
      "rssi": 0
    },
    "downSpeed": 0.0, // kb/s
    "upSpeed": 0.0   //kb/s
  },
  "longitude": 0.0,
  "latitude": 0.0
}
```