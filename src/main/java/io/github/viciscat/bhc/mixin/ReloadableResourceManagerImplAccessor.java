package io.github.viciscat.bhc.mixin;

import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReloadableResourceManagerImpl.class)
public interface ReloadableResourceManagerImplAccessor {

    @Accessor("activeManager")
    LifecycledResourceManager getActiveManager();
}
