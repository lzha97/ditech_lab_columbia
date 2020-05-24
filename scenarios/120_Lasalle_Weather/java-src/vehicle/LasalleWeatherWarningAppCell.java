package com.columbia.vsimrti.applications.lasalle.vehicle;


public class LasalleWeatherWarningAppCell extends LasalleWeatherWarningApp {
    @Override
    protected boolean useCellNetwork() {
        return true;
    }
}