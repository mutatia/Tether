package mutatia.tether.common.local;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public final class OptionalLocal<S, T extends AutoCloseable> extends AbstractLocal<S, T> {
  private final Function<S, Optional<T>> factory;

  OptionalLocal(Scope<S, ?> scope, ResourceLocation id, Function<S, Optional<T>> factory) {
    super(scope, id);
    this.factory = factory;
  }

  public Optional<T> get(S scope) {
    this.requireActive(scope);
    return this.storage.get(this.scope.keyOf(scope));
  }

  @Override
  void start(S scope) {
    this.factory
        .apply(scope)
        .ifPresent((value) -> this.storage.put(this.scope.keyOf(scope), value));
  }
}
