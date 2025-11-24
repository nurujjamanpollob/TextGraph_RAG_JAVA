package com.nurujjamanpollob.textenginejava.rag.utils;

import javadev.stringcollections.textreplacor.console.ColoredConsoleOutput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RagLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        ColoredConsoleOutput.printBlueText(formatLog("INFO", message));
    }

    public static void warn(String message) {
        ColoredConsoleOutput.printYellowText(formatLog("WARN", message));
    }

    public static void error(String message) {
        ColoredConsoleOutput.printRedText(formatLog("ERROR", message));
    }

    public static void debug(String message) {
        // Toggle this based on configuration in a real app
        ColoredConsoleOutput.printGreenText(formatLog("DEBUG", message));
    }

    private static String formatLog(String level, String message) {
        return String.format("[%s] [%s] %s", LocalDateTime.now().format(formatter), level, message);
    }
}