package mutatia.tether.common.local;

import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;

public interface Scope<S, K> {
  K keyOf(S scope);

  void onStart(IEventBus bus, Consumer<? super S> started);

  void onEnd(IEventBus bus, Consumer<? super K> ended);
}
