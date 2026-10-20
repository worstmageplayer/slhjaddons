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
        if (byId.containsKey(feature.id()))
            throw new IllegalArgumentException("Duplicate feature id: " + feature.id());
        features.add(feature);
        byType.put(feature.getClass(), feature);
        byId.put(feature.id(), feature);
    }

    public List<Feature> all() {
        return List.copyOf(features);
    }

    public void bootstrap() {
        for (Feature feature : features) {
            try {
                feature.init();
            } catch (Throwable t) {
                SlhjAddons.LOGGER.error("Failed to init feature {}", feature.id(), t);
            }
        }
        syncFromConfig();
    }

    public void syncFromConfig() {
        for (Feature feature : features) {
            feature.setEnabled(SlhjAddons.config().isFeatureEnabled(feature.id()));
        }
    }

    public <T extends Feature> T get(Class<T> type) {
        return type.cast(byType.get(type));
    }

    public boolean isEnabled(Class<? extends Feature> type) {
        Feature feature = byType.get(type);
        return feature != null && feature.isEnabled();
    }

    public Feature getById(String id) {
        return byId.get(id);
    }
}
