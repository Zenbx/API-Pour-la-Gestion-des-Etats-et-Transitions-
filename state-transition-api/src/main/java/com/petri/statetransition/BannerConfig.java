package com.petri.statetransition;

/**
 * Configuration du bannière personnalisée
 */
@org.springframework.context.annotation.Configuration
class BannerConfig implements org.springframework.boot.Banner {

    @Override
    public void printBanner(org.springframework.core.env.Environment environment,
                            Class<?> sourceClass,
                            java.io.PrintStream out) {
        out.println();
        out.println("  ____  _        _         _____                    _ _   _             ");
        out.println(" / ___|| |_ __ _| |_ ___  |_   _| __ __ _ _ __  ___(_) |_(_) ___  _ __  ");
        out.println(" \\___ \\| __/ _` | __/ _ \\   | || '__/ _` | '_ \\/ __| | __| |/ _ \\| '_ \\ ");
        out.println("  ___) | || (_| | ||  __/   | || | | (_| | | | \\__ \\ | |_| | (_) | | | |");
        out.println(" |____/ \\__\\__,_|\\__\\___|   |_||_|  \\__,_|_| |_|___/_|\\__|_|\\___/|_| |_|");
        out.println();
        out.println("                    API Réactive - Réseaux de Petri");
        out.println("                         Version 1.0.0");
        out.println();
    }
}

