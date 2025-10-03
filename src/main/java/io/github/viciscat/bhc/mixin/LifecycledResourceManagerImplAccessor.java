package io.github.viciscat.bhc.mixin;

import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.NamespaceResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(LifecycledResourceManagerImpl.class)
public interface LifecycledResourceManagerImplAccessor {

    @Accessor("subManagers")
    Map<String, NamespaceResourceManager> getSubManagers();
}
