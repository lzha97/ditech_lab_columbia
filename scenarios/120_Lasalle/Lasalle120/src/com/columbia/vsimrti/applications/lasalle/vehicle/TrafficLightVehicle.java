package com.columbia.vsimrti.applications.lasalle.vehicle;


import com.columbia.vsimrti.applications.lasalle.message.GreenWaveMsg;
import com.columbia.vsimrti.applications.lasalle.trafficLight.TrafficLightApp;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.VehicleOperatingSystem;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.geometry.GeoCircle;
import com.dcaiti.vsimrti.rti.geometry.GeoPoint;
import com.dcaiti.vsimrti.rti.objects.TIME;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;

public final class TrafficLightVehicle extends AbstractApplication<VehicleOperatingSystem> {
    private final static long  TIME_INTERVAL = TIME.SECOND;

    private void sendGeocastMessage() {
        final double range = 15;
        final GeoCircle geoCircle = new GeoCircle(getOperatingSystem().getPosition(), range);
        final MessageRouting routing = getOperatingSystem()
                .getAdHocModule()
                .createMessageRouting()
                .geoBroadCast(geoCircle);
        getOperatingSystem().getAdHocModule().sendV2XMessage(new GreenWaveMsg(routing, TrafficLightApp.SECRET));
        getLog().infoSimTime(this, "Sent secret passphrase");
    }

    private void sample() {
        final Event event = new Event(getOperatingSystem().getSimulationTime() + TIME_INTERVAL, this);
        getOperatingSystem().getEventManager().addEvent(event);
        sendGeocastMessage();
    }

    @Override
    public void setUp() {
        getLog().infoSimTime(this, "Initialize application");
        getOperatingSystem().getAdHocModule().enable();
        getLog().infoSimTime(this, "Activated WLAN Module");
        sample();
    }

    @Override
    public void tearDown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    @Override
    public void processEvent(Event event) throws Exception {
        if (!isValidStateAndLog()) {
            return;
        }
        getLog().infoSimTime(this, "Processing event");
        sample();
    }

}

