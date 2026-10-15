package mutatia.tether.server.local;

import java.util.function.Consumer;
import mutatia.tether.common.local.Scope;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class LevelScope implements Scope<ServerLevel, ServerLevel> {
  @Override
  public ServerLevel keyOf(ServerLevel level) {
    return level;
  }

  @Override
  public void onStart(IEventBus bus, Consumer<? super ServerLevel> started) {
    bus.addListener(
        EventPriority.HIGH,
        false,
        LevelEvent.Load.class,
        (event) -> {
          if (event.getLevel() instanceof ServerLevel level) {
            started.accept(level);
          }
        });
  }

  @Override
  public void onEnd(IEventBus bus, Consumer<? super ServerLevel> ended) {
    bus.addListener(
        EventPriority.LOW,
        false,
        LevelEvent.Unload.class,
        (event) -> {
          if (event.getLevel() instanceof ServerLevel level) {
            ended.accept(level);
          }
        });
  }
}
