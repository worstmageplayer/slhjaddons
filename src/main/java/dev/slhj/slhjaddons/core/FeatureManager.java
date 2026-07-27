package dev.slhj.slhjaddons.core;

import dev.slhj.slhjaddons.SlhjAddons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FeatureManager {

    private final List<Feature> features = new ArrayList<>();
    private final Map<Class<?>, Feature> byType = new HashMap<>();
    private final Map<String, Feature> byId = new HashMap<>();

    public void register(Feature feature) {
        features.add(feature);
        byType.put(feature.getClass(), feature);
        byId.put(feature.id(), feature);
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

    public boolean isEnabled(Class<? extends Feature> type) {
        Feature f = byType.get(type);
        return f != null && f.isEnabled();
    }

    public Feature getById(String id) {
        return byId.get(id);
    }
}