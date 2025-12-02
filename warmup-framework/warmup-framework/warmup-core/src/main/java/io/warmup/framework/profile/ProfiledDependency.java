package io.warmup.framework.profile;

import io.warmup.framework.core.Dependency;
import java.util.Set;

public class ProfiledDependency {

    private final Dependency dependency;
    private final Set<String> profiles;
    private final boolean isDefault; // Sin @Profile

    public ProfiledDependency(Dependency dependency, Set<String> profiles, boolean isDefault) {
        this.dependency = dependency;
        this.profiles = profiles;
        this.isDefault = isDefault;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public Set<String> getProfiles() {
        return profiles;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
