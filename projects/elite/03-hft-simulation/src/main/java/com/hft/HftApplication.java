package com.hft;

import com.hft.service.HftSystem;

public class HftApplication {
    public static void main(String[] args) {
        HftSystem hft = new HftSystem();
        hft.start();
        System.out.println("HFT Trading Simulation started. Press Ctrl+C to stop.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hft.stop();
            System.out.println("HFT System stopped.");
        }));
    }
}
