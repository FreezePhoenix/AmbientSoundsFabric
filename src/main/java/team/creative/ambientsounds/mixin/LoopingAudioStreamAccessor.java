package team.creative.ambientsounds.mixin;

import net.minecraft.client.sounds.LoopingAudioStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.BufferedInputStream;

@Mixin(LoopingAudioStream.class)
public interface LoopingAudioStreamAccessor {
	@Accessor
	BufferedInputStream getBufferedInputStream();
}
