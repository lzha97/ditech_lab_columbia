package com.columbia.vsimrti.applications.lasalle.trafficLight;


import com.columbia.vsimrti.applications.lasalle.message.GreenWaveMsg;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.CommunicationApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.TrafficLightOperatingSystem;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.objects.TIME;
import com.dcaiti.vsimrti.rti.objects.v2x.AckV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;

public final class TrafficLightApp extends AbstractApplication<TrafficLightOperatingSystem> implements CommunicationApplication {
    public final static String SECRET = "open sesame!";
    private final static long TIME_INTERVAL = TIME.SECOND;
    private final static short GREEN_DURATION = 20;
    private boolean switched = false;
    private short ctr = 0;

    @Override
    public void processEvent(Event event) throws Exception {
        sample();
    }

    @Override
    public void setUp() {
        getLog().infoSimTime(this, "Initialize application");
        getOperatingSystem().getAdHocModule().enable();
        getLog().infoSimTime(this, "Activated Wifi Module");
        setRed();
        sample();
    }

    private void sample() {
        getOperatingSystem().getEventManager().addEvent(new Event(getOperatingSystem().getSimulationTime() + TIME_INTERVAL, this));
        if (switched) {
            if (++ctr == GREEN_DURATION) {
                setRed();
            }
        }
    }

    @Override
    public void tearDown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    private void setGreen() {
        getOperatingSystem().setProgramById("0");
        getLog().infoSimTime(this, "Setting traffic lights to GREEN");

    }

    private void setRed() {
        getOperatingSystem().setProgramById("1");
        getLog().infoSimTime(this, "Setting traffic lights to RED");

    }

    @Override
    public void receiveV2XMessage(ReceivedV2XMessage receivedV2XMessage) {
        if (switched) {
            return;
        }
        if (receivedV2XMessage.getMessage() instanceof GreenWaveMsg) {
            getLog().infoSimTime(this, "Received GreenWaveMsg");
            if (((GreenWaveMsg) receivedV2XMessage.getMessage()).getMessage().equals(SECRET)) {
                getLog().infoSimTime(this, "Received correct passphrase: {}", SECRET);
                setGreen();
                switched = true;
            }
        }
    }

    @Override
    public void receiveV2XMessageAcknowledgement(AckV2XMessage ackV2XMessage) {
        // nop
    }

    @Override
    public void beforeGetAndResetUserTaggedValue() {
        // nop
    }

    @Override
    public void afterGetAndResetUserTaggedValue() {
        // nop
    }

    @Override
    public void beforeSendCAM() {
        // nop
    }

    @Override
    public void afterSendCAM() {
        // nop
    }

    @Override
    public void beforeSendV2XMessage() {
        // nop
    }

    @Override
    public void afterSendV2XMessage() {
        // nop
    }
}



