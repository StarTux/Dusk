package com.winthier.dusk;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

final class Metadata {
    private final JavaPlugin plugin;

    Metadata(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    <T> T get(final Metadatable entity,
              final String key,
              final Class<T> theClass) {
        for (MetadataValue meta : entity.getMetadata(key)) {
            if (meta.getOwningPlugin() == plugin) {
                Object value = meta.value();
                if (!theClass.isInstance(value)) {
                    return null;
                }
                return theClass.cast(value);
            }
        }
        return null;
    }

    void set(final Metadatable entity,
             final String key,
             final Object value) {
        entity.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    void remove(final Metadatable entity, final String key) {
        entity.removeMetadata(key, plugin);
    }

    /**
     * {@link Metadatable::hasMetadata(String)} may be preferable.
     */
    boolean has(final Metadatable entity,
                final String key) {
        for (MetadataValue meta : entity.getMetadata(key)) {
            if (meta.getOwningPlugin() == plugin) {
                return true;
            }
        }
        return false;
    }
}
