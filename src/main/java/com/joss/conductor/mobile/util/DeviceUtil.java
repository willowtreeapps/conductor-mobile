package com.joss.conductor.mobile.util;

import com.joss.conductor.mobile.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DeviceUtil {

    public static String[] getConnectedDevices(Platform platform) {
        switch (platform) {
            case IOS:
                return getConnectedIosDevices();
        }

        return null;
    }

    private static String[] getConnectedIosDevices() {
        ProcessBuilder pb = new ProcessBuilder("idevice_id", "--list");

        ArrayList<String> ids = new ArrayList<>();

        final Process process;
        try {
            process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                ids.add(line);
            }
        } catch (IOException e) {
            Log.log.severe("Could not find execute idevice_id: " + e.toString());
        }

        return (String[])ids.toArray();
    }
}
