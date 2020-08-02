package com.dw;

import com.pi4j.temperature.TemperatureScale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EnviroPhatTempCollector {

    private final EnviroPhatDevice enviroPhatDevice;

    private final Logger logger = LoggerFactory.getLogger(EnviroPhatTempCollector.class);

    @Autowired
    public EnviroPhatTempCollector(EnviroPhatDevice device) {
        enviroPhatDevice = device;
    }

    @Scheduled(fixedDelay = 2000)
    public void readTemp() {
        final double temp = enviroPhatDevice.getTemp(TemperatureScale.CELSIUS);
        logger.info("Temperature: {}", temp);
    }
}
