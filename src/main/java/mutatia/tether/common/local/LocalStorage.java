package mutatia.tether.common.local;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

final class LocalStorage<T extends AutoCloseable> {
  private static final Logger LOGGER = LogUtils.getLogger();

  private final ResourceLocation id;
  private final Map<Object, Optional<T>> values = new ConcurrentHashMap<>();

  LocalStorage(ResourceLocation id) {
    this.id = id;
  }

  void start(Object key, @Nullable T value) {
    if (this.values.containsKey(key)) {
      if (value != null) {
        this.close(key, value);
      }
      throw new IllegalStateException(
          "Local "
              + this.id
              + " already started for "
              + key
              + "; its scope started again without ending.");
    }
    this.values.put(key, Optional.ofNullable(value));
  }

  boolean isActive(Object key) {
    return this.values.containsKey(key);
  }

  Optional<T> get(Object key) {
    return this.values.getOrDefault(key, Optional.empty());
  }

  void end(Object key) {
    this.values
        .getOrDefault(key, Optional.empty())
        .ifPresent((present) -> this.close(key, present));
    this.values.remove(key);
  }

  private void close(Object key, AutoCloseable value) {
    try {
      value.close();
    } catch (Exception e) {
      LOGGER.error("Failed to close local {} for {}", this.id, key, e);
    }
  }
}
