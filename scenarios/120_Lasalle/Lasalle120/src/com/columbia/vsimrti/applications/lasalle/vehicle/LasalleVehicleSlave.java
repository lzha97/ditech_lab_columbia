package com.columbia.vsimrti.applications.lasalle.vehicle;


import com.columbia.vsimrti.applications.lasalle.message.IntraVehicleMsg;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.VehicleOperatingSystem;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;

/**
 * Slave application that reacts on messages passed from another
 * app running on the same vehicle.
 */
public class LasalleVehicleSlave extends AbstractApplication<VehicleOperatingSystem> {
    @Override
    public void processEvent(Event event) throws Exception {
        Object resource = event.getResource();
        if (resource != null) {
            if (resource instanceof IntraVehicleMsg) {
                final IntraVehicleMsg message = (IntraVehicleMsg) resource;
                // message was passed from another app on the same vehicle
                if (message.getOrigin().equals(getOperatingSystem().getId())) {
                    getLog().infoSimTime(this, "Received message from another application: {}", message.toString());
                }
            }
        }
    }

    @Override
    public void setUp() {
        getLog().infoSimTime(this, "Initialize application");
    }

    @Override
    public void tearDown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

}

