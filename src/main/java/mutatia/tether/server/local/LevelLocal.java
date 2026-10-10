package mutatia.tether.server.local;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import mutatia.tether.common.local.ScopedLocal;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;

public final class LevelLocal<T extends AutoCloseable> implements ScopedLocal {
  private static final Logger LOGGER = LogUtils.getLogger();

  private final Map<ResourceKey<Level>, T> registry = new ConcurrentHashMap<>();
  private final Function<ServerLevel, T> factory;

  private LevelLocal(Function<ServerLevel, T> factory) {
    this.factory = factory;
  }

  public static <T extends AutoCloseable> LevelLocal<T> withInitial(
      Function<ServerLevel, T> factory) {
    return new LevelLocal<>(factory);
  }

  public T getOrCreate(ServerLevel level) {
    if (!level.getServer().isRunning()) {
      throw new IllegalStateException(
          "Attempted to access LevelLocal registry for "
              + level.dimension().location()
              + " while the server isn't running.");
    }
    return this.registry.computeIfAbsent(level.dimension(), (k) -> this.factory.apply(level));
  }

  @Override
  public void subscribe(IEventBus bus, ResourceLocation id) {
    bus.addListener(
        EventPriority.NORMAL,
        false,
        LevelEvent.Unload.class,
        (event) -> this.onLevelUnload(event, id));
  }

  private void onLevelUnload(LevelEvent.Unload event, ResourceLocation id) {
    if (event.getLevel() instanceof ServerLevel level) {
      this.unload(level.dimension(), id);
    }
  }

  private void unload(ResourceKey<Level> key, ResourceLocation id) {
    T resource = this.registry.remove(key);
    if (resource != null) {
      try {
        resource.close();
      } catch (Exception e) {
        LOGGER.error("Failed to close LevelLocal {} for {}", id, key.location(), e);
      }
    }
  }
}
