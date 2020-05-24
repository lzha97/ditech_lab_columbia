package com.columbia.vsimrti.applications.lasalle.rsu;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.RoadSideUnitApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.RoadSideUnitOperatingSystem;
import com.dcaiti.vsimrti.rti.enums.SensorType;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.geometry.GeoCircle;
import com.dcaiti.vsimrti.rti.geometry.GeoPoint;
import com.dcaiti.vsimrti.rti.objects.TIME;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.denm.DENM;
import com.dcaiti.vsimrti.rti.objects.v2x.denm.DENMContent;

/**
 * This class acts as an omniscient application for a server that warns vehicles
 * about certain hazards on the road. The hazard is hard-coded for tutorial purposes,
 * in more realistic scenarios the location would've been updated dynamically.
 */


public class LasalleWeatherServer extends AbstractApplication<RoadSideUnitOperatingSystem> implements RoadSideUnitApplication {

    /**
     * Send hazard location at this interval, in seconds.
     */
    private final static long INTERVAL = 2 * TIME.SECOND;

    /**
     * Location of the hazard which causes the route change.
     */
    private final static GeoPoint HAZARD_LOCATION = GeoPoint.latlon(40.810904, -73.961969);

    /**
     * Road ID where hazard is located.
     */
    private final static String HAZARD_ROAD = "195743140_42428844_1506591536"; //"-3366_2026362940_1313885502";

    private final static SensorType SENSOR_TYPE = SensorType.Ice;
    private final static float SPEED = 25 / 3.6f;


    /**
     * This method is called by VSimRTI when the vehicle that has been equipped with this application
     * enters the simulation.
     * It is the first method called of this class during a simulation.
     */
    @Override
    public void setUp() {
        getLog().infoSimTime(this, "Initialize WeatherServer application");
        getOperatingSystem().getCellModule().enable();
        getLog().infoSimTime(this, "Activated Cell Module");
        sample();
    }

    /**
     * This method is called by VSimRTI when a previously triggered event is handed over to the
     * application by VSimRTI for processing.
     * Events can be triggered be this application itself, e.g. to run a routine periodically.
     *
     * @param event The event to be processed
     */
    @Override
    public void processEvent(Event event) throws Exception {
        sample();
    }

    /**
     * Method to let the WeatherServer send a DEN message periodically.
     *
     * This method sends a DEN message and generates a new event during each call.
     * When said event is triggered (via VSimRTI), processEvent() is called, which in turn calls sample().
     * This way, sample() is called periodically at a given interval (given by the generated event time)
     * and thus the DENM is sent periodically at this interval.
     */
    private void sample() {
        final DENM denm = constructDENM(); // Construct exemplary DENM

        getOperatingSystem().getCellModule().sendV2XMessage(denm);
        getLog().infoSimTime(this, "Sent DENM");
        // Line up new event for periodic sending
        getOperatingSystem().getEventManager().addEvent(new Event(getOperatingSystem().getSimulationTime() + INTERVAL, this));
    }

    /**
     * Constructs a staged DEN message for tutorial purposes that matches exactly the requirements of
     * the Barnim tutorial scenario.
     *
     * This is not meant to be used for real scenarios and is for the purpose of the tutorial only.
     *
     * @return The constructed DENM
     */
    private DENM constructDENM() {
        final GeoCircle geoCircle = new GeoCircle(HAZARD_LOCATION, 3000.0D);
        final MessageRouting routing = getOperatingSystem().getCellModule().createMessageRouting().geoBroadCast(geoCircle);

        final int strength = getOperatingSystem().getStateOfEnvironmentSensor(SENSOR_TYPE);

        getLog().infoSimTime(this, "Strength: " +Integer.toString(strength));

        return new DENM(routing,
                new DENMContent(
                        getOperatingSystem().getSimulationTime(),
                        getOs().getInitialPosition(),
                        HAZARD_ROAD,
                        SENSOR_TYPE,
                        strength,
                        SPEED,
                        0.0f,
                        HAZARD_LOCATION,
                        null,
                        null
                )
        );
    }

    /**
     * This method is called by VSimRTI when the vehicle that has been equipped with this application
     * leaves the simulation.
     * It is the last method called of this class during a simulation.
     */
    @Override
    public void tearDown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

}

