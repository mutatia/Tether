package mutatia.tether.common.local;

import net.minecraft.resources.ResourceLocation;

abstract sealed class AbstractLocal<S, T extends AutoCloseable> permits Local, OptionalLocal {
  final Scope<S, ?> scope;
  final ResourceLocation id;
  final LocalStorage<T> storage;

  AbstractLocal(Scope<S, ?> scope, ResourceLocation id) {
    this.scope = scope;
    this.id = id;
    this.storage = new LocalStorage<>(id);
  }

  abstract void start(S scope);

  void end(Object key) {
    this.storage.end(key);
  }

  void requireActive(S scope) {
    if (!this.storage.isActive(this.scope.keyOf(scope))) {
      throw new IllegalStateException(
          "Attempted to access local "
              + this.id
              + " for "
              + this.scope.keyOf(scope)
              + " outside its scope.");
    }
  }
}
