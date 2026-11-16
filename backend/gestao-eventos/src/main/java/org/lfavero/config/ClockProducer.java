package org.lfavero.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.ZoneId;

@ApplicationScoped
public class ClockProducer {

    @ConfigProperty(name = "eventos.timezone", defaultValue = "America/Sao_Paulo")
    String timezone;

    @Produces
    @Singleton
    public Clock clock() {
        return Clock.system(ZoneId.of(timezone));
    }
}