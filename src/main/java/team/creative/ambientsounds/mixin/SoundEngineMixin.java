package team.creative.ambientsounds.mixin;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import team.creative.ambientsounds.AmbientSounds;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
	@Debug(print = true)
	@Dynamic
	@Shadow
	public static void method_19755(AudioStream a, Channel b) {
		AmbientSounds.tickHandler.soundEngine.play(a,b);
	}

	@Shadow
	public abstract void play(SoundInstance soundInstance);

}
