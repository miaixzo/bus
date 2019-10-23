package org.aoju.bus.boot.banner;

import org.aoju.bus.Version;
import org.aoju.bus.boot.consts.BootConsts;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

public class BusBanner implements Banner {

    private static final String SPRING_BOOT = "::Spring Boot::";
    
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream printStream) {
        for (Object line : BootConsts.BUS_BANNER) {
            printStream.println(AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW, line));
        }

        printStream.println();
        printStream.println(AnsiOutput.toString(
                AnsiColor.BRIGHT_MAGENTA, SPRING_BOOT + String.format(" (v%s)", SpringBootVersion.getVersion()),
                AnsiColor.BRIGHT_MAGENTA, "      " + BootConsts.BUS_BOOT + String.format(" (v%s)", Version.get())));
        printStream.println();
    }

}