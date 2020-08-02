package com.dw;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.temperature.TemperatureScale;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EnviroPhatDevice {

    private static final double DEFAULT_TEMP = Double.MIN_VALUE;

    private I2CDevice device;

    public EnviroPhatDevice() {
        try {
            I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
            device = bus.getDevice(0x77);
        } catch (IOException | I2CFactory.UnsupportedBusNumberException e) {
            throw new IllegalStateException("Can't init Enviro pHat", e);
        }
    }

    public double getTemp(TemperatureScale tempType) {

        double result = DEFAULT_TEMP;

        try {
            // Read 24 bytes of data from address 0x88(136)
            byte[] b1 = new byte[24];
            device.read(0x88, b1, 0, 24);

            // Convert the data
            // temp coefficients
            int dig_T1 = (b1[0] & 0xFF) + ((b1[1] & 0xFF) * 256);
            int dig_T2 = (b1[2] & 0xFF) + ((b1[3] & 0xFF) * 256);
            if (dig_T2 > 32767) {
                dig_T2 -= 65536;
            }
            int dig_T3 = (b1[4] & 0xFF) + ((b1[5] & 0xFF) * 256);
            if (dig_T3 > 32767) {
                dig_T3 -= 65536;
            }

            // Select control measurement register
            // Normal mode, temp and pressure over sampling rate = 1
            device.write(0xF4, (byte) 0x27);

            // Select config register
            // Stand_by time = 1000 ms
            device.write(0xF5, (byte) 0xA0);
            Thread.sleep(500);

            // Read 8 bytes of data from address 0xF7(247)
            // pressure msb1, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
            byte[] data = new byte[8];
            device.read(0xF7, data, 0, 8);

            // Convert pressure and temperature data to 19-bits
            long adc_t = (((long) (data[3] & 0xFF) * 65536) + ((long) (data[4] & 0xFF) * 256) + (long) (data[5] & 0xF0)) / 16;

            // Temperature offset calculations
            double var1 = (((double) adc_t) / 16384.0 - ((double) dig_T1) / 1024.0) * ((double) dig_T2);
            double var2 = ((((double) adc_t) / 131072.0 - ((double) dig_T1) / 8192.0) *
                    (((double) adc_t) / 131072.0 - ((double) dig_T1) / 8192.0)) * ((double) dig_T3);

            double cTemp = (var1 + var2) / 5120.0;
            double fTemp = cTemp * 1.8 + 32;

            switch (tempType) {
                case CELSIUS:
                    result = cTemp;
                    break;
                case FARENHEIT:
                    result = fTemp;
                    break;
            }

        } catch (InterruptedException | IOException e) {
            throw new IllegalStateException("Can't load data from Enviro pHat", e);
        }

        return result;
    }

}
