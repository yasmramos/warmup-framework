package io.warmup.framework.core;

import io.warmup.framework.common.ClassMetadata;
import io.warmup.framework.config.PropertySource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileManager {
    private static final Logger log = Logger.getLogger(ProfileManager.class.getName());

    private final Set<String> activeProfiles = new HashSet<>();
    private final PropertySource propertySource; // Necesario para cargar perfiles iniciales desde propiedades

    public ProfileManager(PropertySource propertySource, String... initialProfiles) {
        this.propertySource = propertySource;
        initializeProfiles(initialProfiles);
    }

    private void initializeProfiles(String... profiles) {
        Set<String> allActiveProfiles = new HashSet<>();
        // 1. Perfiles del parámetro del constructor
        if (profiles != null) {
            for (String p : profiles) {
                if (p != null && !p.trim().isEmpty()) {
                    allActiveProfiles.add(p.trim());
                }
            }
        }
        // 2. Cargar perfiles desde propiedades (si PropertySource existe)
        if (this.propertySource != null) {
            String activeProfilesStr = this.propertySource.getProperty("warmup.profiles");
            if (activeProfilesStr != null && !activeProfilesStr.isEmpty()) {
                log.log(Level.INFO, "Cargando perfiles desde propiedades: {0}", activeProfilesStr);
                String[] profileArray = activeProfilesStr.split(",");
                for (String profile : profileArray) {
                    if (profile != null && !profile.trim().isEmpty()) {
                        allActiveProfiles.add(profile.trim());
                    }
                }
            }
        } else {
            log.log(Level.WARNING, "PropertySource es NULL en el momento de cargar perfiles desde propiedades");
        }
        // 3. Si no hay perfiles definidos, se usa "default"
        if (allActiveProfiles.isEmpty()) {
            allActiveProfiles.add("default");
        }
        // 4. Inicializar perfiles en el manager
        this.activeProfiles.clear();
        this.activeProfiles.addAll(allActiveProfiles);
        log.log(Level.INFO, "Perfiles activos: {0}", this.activeProfiles);
    }

    public void setActiveProfiles(String... profiles) {
        this.activeProfiles.clear();
        if (profiles.length == 0) {
            this.activeProfiles.add("default");
        } else {
            Collections.addAll(this.activeProfiles, profiles);
        }
        log.log(Level.INFO, "Perfiles activos: {0}", this.activeProfiles);
    }

    public void addActiveProfile(String profile) {
        this.activeProfiles.add(profile);
        System.out.println("Perfil agregado: " + profile);
    }

    public boolean isProfileActive(String profile) {
        return this.activeProfiles.contains(profile);
    }

    public Set<String> getActiveProfiles() {
        return new HashSet<>(this.activeProfiles); // Devolver copia para inmutabilidad
    }

    // Método para verificar si una clase debe registrarse según sus perfiles
    // (esta lógica residía en ComponentScanner, pero ComponentScanner puede recibir ProfileManager)
    public boolean shouldRegisterClass(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(io.warmup.framework.annotation.Profile.class)) {
            return true;
        }
        io.warmup.framework.annotation.Profile profileAnnotation = clazz.getAnnotation(io.warmup.framework.annotation.Profile.class);
        String[] requiredProfiles = profileAnnotation.value();
        if (requiredProfiles.length == 0) {
            return true;
        }
        // LOG PARA DEPURAR
        System.out.println("Verificando perfiles para " + clazz.getSimpleName() + ": " + Arrays.toString(requiredProfiles));
        System.out.println("Perfiles activos en el manager: " + getActiveProfiles());
        for (String requiredProfile : requiredProfiles) {
            if (requiredProfile != null && isProfileActive(requiredProfile)) { // Usar isProfileActive
                System.out.println("✅ Perfil '" + requiredProfile + "' coincide para " + clazz.getSimpleName());
                return true;
            }
        }
        System.out.println("Ningún perfil coincide para " + clazz.getSimpleName() + ", NO se registrará.");
        return false;
    }
    
    public boolean shouldRegisterClass(ClassMetadata metadata) {
        if (!metadata.profiles.isEmpty()) {
            return metadata.profiles.stream().anyMatch(this::isProfileActive);
        }
        return true; // Sin @Profile, siempre registrar
    }
}