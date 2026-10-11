package mutatia.tether.common.local;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

public final class LocalRegistry<S> {
  private final Scope<S, ?> scope;
  private final String modId;
  private final List<AbstractLocal<S, ?>> locals = new CopyOnWriteArrayList<>();
  private final AtomicBoolean subscribed = new AtomicBoolean();

  private LocalRegistry(Scope<S, ?> scope, String modId) {
    this.scope = scope;
    this.modId = modId;
  }

  public static <S> LocalRegistry<S> create(Scope<S, ?> scope, String modId) {
    return new LocalRegistry<>(scope, modId);
  }

  public <T extends AutoCloseable> Local<S, T> register(String name, Function<S, T> factory) {
    ResourceLocation id = this.claim(name);
    return this.add(new Local<>(this.scope, id, factory));
  }

  public <T extends AutoCloseable> OptionalLocal<S, T> registerOptional(
      String name, Function<S, Optional<T>> factory) {
    ResourceLocation id = this.claim(name);
    return this.add(new OptionalLocal<>(this.scope, id, factory));
  }

  public void register(IEventBus bus) {
    if (!this.subscribed.compareAndSet(false, true)) {
      throw new IllegalStateException(
          "Cannot subscribe the " + this.modId + " LocalRegistry to more than one event bus.");
    }
    this.scope.onStart(bus, this::start);
    this.scope.onEnd(bus, this::end);
  }

  private void start(S scope) {
    this.locals.forEach((local) -> local.start(scope));
  }

  private void end(Object key) {
    this.locals.reversed().forEach((local) -> local.end(key));
  }

  private ResourceLocation claim(String name) {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modId, name);
    if (this.subscribed.get()) {
      throw new IllegalStateException(
          "Cannot register " + id + " after its registry has been subscribed to an event bus.");
    }
    if (this.locals.stream().anyMatch((local) -> local.id.equals(id))) {
      throw new IllegalArgumentException("Duplicate local registration: " + id);
    }
    return id;
  }

  private <L extends AbstractLocal<S, ?>> L add(L local) {
    this.locals.add(local);
    return local;
  }
}
