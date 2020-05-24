package com.columbia.vsimrti.applications.lasalle.vehicle;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.VehicleApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.VehicleOperatingSystem;
import com.dcaiti.vsimrti.rti.enums.SensorType;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;

/**
 * This application shall induce vehicles to slow down in hazardous environments.
 * In afterUpdateVehicleInfo() the application requests new data from a vehicle's
 * sensors and analyzes the data with respect to strength after every single update.
 * Once a sensor indicates that a certain vehicle has entered a potentially
 * hazardous area, the application will reduce the speed of the respective vehicle
 * within a specified time frame. After the respective vehicle has left the dangerous
 * zone, its speed will no longer be reduced.
 *
 * @author mke
 */
public class  LasalleSlowDownApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication {

    private final static float SPEED = 25 / 3.6f;

    private boolean hazardousArea = false;

    /**
     * This method is used to request new data from the sensors and in that case
     * react on the retrieved data.
     * It is called at each simulation step when the vehicle info has been updated for
     * the vehicle that has this application equipped.
     */
    @Override
    public void afterUpdateVehicleInfo() {

        // Enumeration of possible environment sensor types that are available in a vehicle
        SensorType[] types = SensorType.values();

        // Initialize sensor strength
        int strength = 0;

        /*
         * The current strength of each environment sensor is examined here.
         * If one is higher than zero, we reason that we are in a hazardous area with the
         * given hazard.
         */
        for (SensorType currentType : types) {
            // The strength of a detected sensor
            strength = getOperatingSystem().getStateOfEnvironmentSensor(currentType);

            if (strength > 0) {
                break;
            }
        }

        if (strength > 0 && !hazardousArea) {
            // Reduce speed when entering potentially hazardous area
            getOperatingSystem().changeSpeedWithInterval(SPEED, 5000);
            hazardousArea = true;
        }

        if (strength == 0 && hazardousArea) {
            // Reset speed when leaving potentially hazardous area
            getOperatingSystem().resetSpeed();
            hazardousArea = false;
        }

    }

    @Override
    public void processEvent(Event event) throws Exception {

    }

    @Override
    public void setUp() {

    }

    @Override
    public void tearDown() {

    }

    @Override
    public void beforeUpdateConnection() {

    }

    @Override
    public void afterUpdateConnection() {

    }

    @Override
    public void beforeUpdateVehicleInfo() {

    }

}
