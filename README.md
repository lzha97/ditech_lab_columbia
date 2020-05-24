# Custom Scenario in from 120 to Lasalle 

includes scenario application src files in 
`scenarios/120_Lasalle/Lasalle120/src`

These have already been converted into the jar file: 
`scenarios/120_Lasalle_applicationNT/Lasalle120-2.jar`

To edit the java source files, follow the instructions in the Vsimrti-user-manual-19.1 chapter 3.5 (Vsimrti Application Simulator)
Make sure that the jdk matches the version of java used by Vimsrti (Java 8). 

# Lasalle Weather 

Java source files located in `/java-src` folder. Scenario is based on Barnim example in VSimrti. TO-DO's located in the "Vsimrti simulations" project [here](https://github.com/lzha97/ditech_lab_columbia/projects).

## Icy hazard 
Ice rectangle positioned on Broadway between 122nd and 121st streets in eventserver folder. 
Most recent demo of Lasalle Icy Conditions: [here](https://www.youtube.com/watch?v=YfMiiD5P4dc&feature=youtu.be)


## Weather RSU

Attempted to position a weather serving RSU as following but resulted in event strength = 0 and no vehicle sending DENM message. No vehicles seems to get rerouted in video, but rerouting is shown in the log files. 
```
{
    "lat":40.805107,
    "lon": -73.959934,
    "name": "WeatherServer",
    "applications": [ "com.columbia.vsimrti.applications.lasalle.rsu.LasalleWeatherServer" ]
}
```

Video of lasalle weather scenario with placement of weather RSU [here](https://www.youtube.com/watch?v=SYGTNEaBOhg&feature=youtu.be)