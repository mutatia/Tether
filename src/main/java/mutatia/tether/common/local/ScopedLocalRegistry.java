package mutatia.tether.common.local;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

public final class ScopedLocalRegistry {
  private final String modId;
  private final Map<ResourceLocation, ScopedLocal> locals = new ConcurrentHashMap<>();
  private final AtomicBoolean subscribed = new AtomicBoolean();

  private ScopedLocalRegistry(String modId) {
    this.modId = modId;
  }

  public static ScopedLocalRegistry create(String modId) {
    return new ScopedLocalRegistry(modId);
  }

  public <L extends ScopedLocal> L register(String name, L local) {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modId, name);
    if (this.locals.putIfAbsent(id, local) != null) {
      throw new IllegalArgumentException("Duplicate ScopedLocal registration: " + id);
    }
    if (this.subscribed.get()) {
      throw new IllegalStateException(
          "Cannot register " + id + " after its registry has been subscribed to an event bus.");
    }
    return local;
  }

  public void register(IEventBus bus) {
    if (!this.subscribed.compareAndSet(false, true)) {
      throw new IllegalStateException(
          "Cannot subscribe the "
              + this.modId
              + " ScopedLocalRegistry to more than one event bus.");
    }
    this.locals.forEach((id, local) -> local.subscribe(bus, id));
  }
}
