package dev.slhj.slhjaddons.core;

import dev.slhj.slhjaddons.SlhjAddons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FeatureManager {

    private final List<Feature> features = new ArrayList<>();
    private final Map<Class<?>, Feature> byType = new HashMap<>();

    public void register(Feature feature) {
        features.add(feature);
        byType.put(feature.getClass(), feature);
    }

    public List<Feature> all() {
        return features;
    }

    public void bootstrap() {
        for (Feature f : features) {
            try {
                f.init();
            } catch (Throwable t) {
                SlhjAddons.LOGGER.error("Failed to init feature {}", f.id(), t);
            }
        }
        syncFromConfig();
    }

    public void syncFromConfig() {
        for (Feature f : features) {
            f.setEnabled(SlhjAddons.config().isFeatureEnabled(f.id()));
        }
    }

    public <T extends Feature> T get(Class<T> type) {
        return (T) byType.get(type);
    }
}