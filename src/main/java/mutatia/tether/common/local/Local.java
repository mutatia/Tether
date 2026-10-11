package mutatia.tether.common.local;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public final class Local<S, T extends AutoCloseable> extends AbstractLocal<S, T> {
  private final Function<S, T> factory;

  Local(Scope<S, ?> scope, ResourceLocation id, Function<S, T> factory) {
    super(scope, id);
    this.factory = factory;
  }

  public T get(S scope) {
    this.requireActive(scope);
    return this.storage
        .get(this.scope.keyOf(scope))
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Local " + this.id + " has no value for " + this.scope.keyOf(scope)));
  }

  @Override
  void start(S scope) {
    this.storage.put(this.scope.keyOf(scope), this.factory.apply(scope));
  }
}
