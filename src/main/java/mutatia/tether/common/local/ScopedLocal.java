package mutatia.tether.common.local;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

public interface ScopedLocal {
  void subscribe(IEventBus bus, ResourceLocation id);
}
