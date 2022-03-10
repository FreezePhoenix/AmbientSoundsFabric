package team.creative.ambientsounds.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import team.creative.ambientsounds.AmbientEngine;
import team.creative.ambientsounds.mixin.BiomeAccessor;

public class BiomeEnviroment {
    
    public final LinkedHashMap<BiomeArea, Float> biomes = new LinkedHashMap<>();
    
    public BiomeEnviroment() {}
    
    public BiomeEnviroment(AmbientEngine engine, Player player, Level level, double volume, double surface) {
        if (volume > 0.0) {
            BlockPos center = player.eyeBlockPosition();
            MutableBlockPos pos = new MutableBlockPos();
            for (int x = -engine.biomeScanCount; x <= engine.biomeScanCount; x++) {
                for (int z = -engine.biomeScanCount; z <= engine.biomeScanCount; z++) {
                    pos.set(center.getX() + x * engine.biomeScanDistance, center.getY(), center.getZ() + z * engine.biomeScanDistance);
                    Biome biome = level.getBiome(pos).value();
                    
                    float biomeVolume = (float) ((1 - Math.sqrt(center.distSqr(pos)) / (engine.biomeScanCount * engine.biomeScanDistance * 2)) * volume);
                    if (((BiomeAccessor) (Object) biome).invokeGetBiomeCategory() != BiomeCategory.UNDERGROUND)
                        biomeVolume *= surface;
                    BiomeArea area = new BiomeArea(level, biome, pos);
                    Float before = biomes.get(area);
                    if (before == null)
                        before = 0F;
                    biomes.put(area, Math.max(before, biomeVolume));
                }
            }
            
            List<Entry<BiomeArea, Float>> entries = new ArrayList<>(biomes.entrySet());
            Collections.sort(entries, new Comparator<Entry<BiomeArea, Float>>() {
                @Override
                public int compare(Entry<BiomeArea, Float> o1, Entry<BiomeArea, Float> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            for (Entry<BiomeArea, Float> entry : entries)
                this.biomes.put(entry.getKey(), entry.getValue());
        }
    }
    
    public static class BiomeArea {
        
        public final Biome biome;
        public final ResourceLocation location;
        public final BlockPos pos;
        
        public BiomeArea(Level level, Biome biome, BlockPos pos) {
            this.biome = biome;
            this.location = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
            this.pos = pos;
        }
        
        public boolean checkBiome(String[] names) {
            for (String name : names) {
                String biomename = ((BiomeAccessor) (Object) biome).invokeGetBiomeCategory().getName().toLowerCase().replace("_", " ");
                if (biomename.matches(".*" + name.replace("*", ".*") + ".*"))
                    return true;
                
                if (location.getPath().matches(".*" + name.replace("*", ".*") + ".*"))
                    return true;
            }
            return false;
        }
        
        @Override
        public boolean equals(Object object) {
            if (object instanceof BiomeArea)
                return ((BiomeArea) object).biome == biome;
            return false;
        }
        
        @Override
        public int hashCode() {
            return biome.hashCode();
        }
        
    }
    
}
