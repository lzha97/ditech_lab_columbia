package com.columbia.vsimrti.applications.lasalle.vehicle;

import com.columbia.vsimrti.applications.lasalle.message.IntraVehicleMsg;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.Application;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.CommunicationApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.VehicleApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.communication.AdHocModuleConfiguration;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.VehicleOperatingSystem;
import com.dcaiti.vsimrti.rti.enums.SensorType;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.network.AdHocChannel;
import com.dcaiti.vsimrti.rti.objects.v2x.AckV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;

import java.util.List;

public class LasalleVehicle extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication, CommunicationApplication {
    /**
     * Used for choosing a RAND id for the message that is sent intra-vehicle.
     */
    private final static int     MAX_ID = 1000;

    @Override
    public void setUp() {
        getLog().infoSimTime(this, "Initialize application");
        getOperatingSystem().getAdHocModule().enable(new AdHocModuleConfiguration()
            .addRadio()
            .channel(AdHocChannel.CCH)
            .power(50)
            .create());
        getLog().infoSimTime(this, "Activated AdHoc Module");
    }

    @Override
    public void afterUpdateVehicleInfo() {        
        final List<? extends Application> applications = getOperatingSystem().getApplications();
        final IntraVehicleMsg message = new IntraVehicleMsg(getOperatingSystem().getId(), getRandom().nextInt(0, MAX_ID));

        // Example usage for how to detect sensor readings
        if (getOperatingSystem().getStateOfEnvironmentSensor(SensorType.Obstacle) > 0) {
            getLog().infoSimTime(this, "Reading sensor");
        }

        for (Application application : applications) {
            final Event event = new Event(getOperatingSystem().getSimulationTime() + 10, application, message);
            this.getOperatingSystem().getEventManager().addEvent(event);
        }
    }

    @Override
    public void receiveV2XMessage(ReceivedV2XMessage receivedV2XMessage) {
        getLog().infoSimTime(this, "Received V2X Message from {}", receivedV2XMessage.getMessage().getRouting().getSourceAddressContainer().getSourceName());
    }

    @Override
    public void processEvent(Event event) throws Exception {
        getLog().infoSimTime(this, "Received event: {}", getOperatingSystem().getSimulationTimeMs(), event.getResourceClassSimpleName());
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

    @Override
    public void tearDown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    @Override
    public void receiveV2XMessageAcknowledgement(AckV2XMessage ackV2XMessage) {
    }

    @Override
    public void beforeGetAndResetUserTaggedValue() {
    }

    @Override
    public void afterGetAndResetUserTaggedValue() {
    }

    @Override
    public void beforeSendCAM() {
    }

    @Override
    public void afterSendCAM() {
    }

    @Override
    public void beforeSendV2XMessage() {
    }

    @Override
    public void afterSendV2XMessage() {
    }
}
