package mutatia.tether.common.local;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

final class LocalStorage<T extends AutoCloseable> {
  private static final Logger LOGGER = LogUtils.getLogger();

  private final ResourceLocation id;
  private final Map<Object, T> values = new ConcurrentHashMap<>();

  LocalStorage(ResourceLocation id) {
    this.id = id;
  }

  void put(Object key, T value) {
    if (this.values.putIfAbsent(key, value) != null) {
      this.close(key, value);
      throw new IllegalStateException(
          "Local "
              + this.id
              + " already has a value for "
              + key
              + "; its scope started again without ending.");
    }
  }

  Optional<T> get(Object key) {
    return Optional.ofNullable(this.values.get(key));
  }

  void end(Object key) {
    T value = this.values.remove(key);
    if (value != null) {
      this.close(key, value);
    }
  }

  private void close(Object key, AutoCloseable value) {
    try {
      value.close();
    } catch (Exception e) {
      LOGGER.error("Failed to close local {} for {}", this.id, key, e);
    }
  }
}
