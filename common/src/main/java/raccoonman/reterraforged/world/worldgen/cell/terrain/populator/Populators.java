package raccoonman.reterraforged.world.worldgen.cell.terrain.populator;

import raccoonman.reterraforged.data.worldgen.preset.settings.MiscellaneousSettings;
import raccoonman.reterraforged.data.worldgen.preset.settings.TerrainSettings;
import raccoonman.reterraforged.world.worldgen.biome.Erosion;
import raccoonman.reterraforged.world.worldgen.biome.Weirdness;
import raccoonman.reterraforged.world.worldgen.cell.CellPopulator;
import raccoonman.reterraforged.world.worldgen.cell.heightmap.Levels;
import raccoonman.reterraforged.world.worldgen.cell.terrain.Terrain;
import raccoonman.reterraforged.world.worldgen.cell.terrain.TerrainType;
import raccoonman.reterraforged.world.worldgen.noise.domain.Domain;
import raccoonman.reterraforged.world.worldgen.noise.domain.Domains;
import raccoonman.reterraforged.world.worldgen.noise.function.DistanceFunction;
import raccoonman.reterraforged.world.worldgen.noise.function.EdgeFunction;
import raccoonman.reterraforged.world.worldgen.noise.function.Interpolation;
import raccoonman.reterraforged.world.worldgen.noise.module.Erosion.BlendMode;
import raccoonman.reterraforged.world.worldgen.noise.module.Noise;
import raccoonman.reterraforged.world.worldgen.noise.module.Noises;
import raccoonman.reterraforged.world.worldgen.util.Seed;

public class Populators {
	@Deprecated
	public static final Noise DEFAULT_EROSION = Erosion.LEVEL_4.source();
	@Deprecated
	public static final Noise DEFAULT_WEIRDNESS = Weirdness.LOW_SLICE_NORMAL_DESCENDING.source();

	public static CellPopulator makeDeepOcean(@Deprecated int seed, float seaLevel) {
		Noise hills = Noises.perlin(++seed, 150, 3);
		hills = Noises.mul(hills, seaLevel * 0.7F);

		Noise hillBias = Noises.perlin(++seed, 200, 1);
		hillBias = Noises.mul(hillBias, seaLevel * 0.2F);
		
		hills = Noises.add(hills, hillBias);
		
		Noise canyons = Noises.perlin(++seed, 150, 4);
		canyons = Noises.powCurve(canyons, 0.2F);
		canyons = Noises.invert(canyons);
		canyons = Noises.mul(canyons, seaLevel * 0.7F);
		
		Noise canyonBias = Noises.perlin(++seed, 170, 1);
		canyonBias = Noises.mul(canyonBias, seaLevel * 0.15F);
		
		canyons = Noises.add(canyons, canyonBias);
		
		Noise selector = Noises.perlin(++seed, 500, 1);
		
		Noise height = Noises.blend(selector, hills, canyons, 0.6F, 0.65F);
		height = Noises.warpPerlin(height, ++seed, 50, 2, 50.0F);
		return new OceanPopulator(TerrainType.DEEP_OCEAN, height);
	}
    
	public static CellPopulator makeShallowOcean(Levels levels) {
		 return new OceanPopulator(TerrainType.SHALLOW_OCEAN, Noises.constant(levels.water(-7)));
	}
	
	public static CellPopulator makeCoast(Levels levels) {
		return new OceanPopulator(TerrainType.COAST, Noises.constant(levels.water));
	}
	
    public static TerrainPopulator makeSteppe(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings) {
        int scaleH = Math.round(250.0F * settings.horizontalScale);

        Noise erosion = Noises.perlin(seed.next(), scaleH * 2, 3, 3.75F);
        erosion = Noises.alpha(erosion, 0.45F);
        
        Noise warpX = Noises.perlin(seed.next(), scaleH / 4, 3, 3.0F);
        Noise warpZ = Noises.perlin(seed.next(), scaleH / 4, 3, 3.0F);
        
        Noise weirdness = Noises.perlin(seed.next(), scaleH, 1);
        weirdness = Noises.mul(weirdness, erosion);
        weirdness = Noises.warp(weirdness, warpX, warpZ, scaleH / 4.0F);
        weirdness = Noises.warpPerlin(weirdness, seed.next(), 256, 1, 200.0F);
        weirdness = Noises.cache2d(weirdness);
        
        Noise height = Noises.mul(weirdness, 0.08F);
        height = Noises.add(height, -0.02F);
		return TerrainPopulator.make(TerrainType.FLATS, ground, height, Erosion.LEVEL_4.source(), Noises.mul(weirdness, Noises.constant(-1.0F)), settings);
    }
    
    private static TerrainPopulator makePlains(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain noiseSettings, TerrainSettings.Terrain scalingSettings, float verticalScale) {
    	int scaleH = Math.round(250.0F * noiseSettings.horizontalScale);
      	
		Noise erosion = Noises.perlin(seed.next(), scaleH * 2, 3, 3.75F);
      	erosion = Noises.alpha(erosion, 0.45F);
      	
      	Noise warpX = Noises.perlin(seed.next(), scaleH / 4, 3, 3.5F);
      	Noise warpZ = Noises.perlin(seed.next(), scaleH / 4, 3, 3.5F);
      	
      	Noise weirdness = Noises.perlin(seed.next(), scaleH, 1);
      	weirdness = Noises.mul(weirdness, erosion);
      	weirdness = Noises.warp(weirdness, warpX, warpZ, scaleH / 4.0F);
      	weirdness = Noises.warpPerlin(weirdness, seed.next(), 256, 1, 256.0F);
        weirdness = Noises.cache2d(weirdness);
        
      	Noise height = Noises.mul(weirdness, 0.15F * verticalScale);
      	height = Noises.add(height, -0.02F);
      	return TerrainPopulator.make(TerrainType.FLATS, ground, height, Erosion.LEVEL_4.source(), Noises.mul(Noises.add(weirdness, 0.025F), Noises.constant(-1.0F)), scalingSettings);
    }

    public static TerrainPopulator makePlains(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings, float verticalScale) {
    	return makePlains(seed, ground, settings, settings, verticalScale);
    }
	
	public static TerrainPopulator makePlateau(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings, float verticalScale) {
		Noise valley = Noises.perlinRidge(seed.next(), 500, 1);
		valley = Noises.invert(valley);
		valley = Noises.warpPerlin(valley, seed.next(), 100, 1, 150.0F);
		valley = Noises.warpPerlin(valley, seed.next(), 20, 1, 15.0F);
		
		Noise top = Noises.perlinRidge(seed.next(), 150, 3, 2.45F);
		top = Noises.warpPerlin(top, seed.next(), 300, 1, 150.0F);
		top = Noises.warpPerlin(top, seed.next(), 40, 2, 20.0F);
		top = Noises.mul(top, 0.15F);
		
		Noise valleyScaler = Noises.clamp(valley, 0.02F, 0.1F);
		valleyScaler = Noises.map(valleyScaler, 0.0F, 1.0F);
		
		top = Noises.mul(top, valleyScaler);
		
		Noise surface = Noises.perlin(seed.next(), 20, 3);
		surface = Noises.mul(surface, 0.05F);
		surface = Noises.warpPerlin(surface, seed.next(), 40, 2, 20.0F);
		
		Noise cubic = Noises.cubic(seed.next(), 500, 1);
		cubic = Noises.mul(cubic, 0.6F);
		cubic = Noises.add(cubic, 0.3F);
		
		Noise valleyBase = Noises.mul(valley, cubic);
		valleyBase = Noises.add(valleyBase, top);
		
		Noise height = Noises.terrace(valleyBase, 0.9F, 0.15F, 0.35F, 0.1F, 4);
		height = Noises.add(height, surface);
		height = Noises.mul(height, 0.475F * verticalScale);
		
		Noise weirdness = Noises.clamp(valleyBase, 0.0F, 0.415F);
		weirdness = Noises.map(weirdness, 0.0F, 1.0F);
		weirdness = Noises.map(weirdness, Weirdness.LOW_SLICE_NORMAL_DESCENDING.mid(), -0.42F);
		return TerrainPopulator.make(TerrainType.PLATEAU, ground, height, Noises.constant(-0.15F), weirdness, settings);
	}
	
	public static TerrainPopulator makeHills1(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings, float verticalScale) {
		Noise height = Noises.perlin(seed.next(), 200, 3);
		
		Noise scaler = Noises.billow(seed.next(), 400, 3);
		scaler = Noises.alpha(scaler, 0.5F);
		
		height = Noises.mul(height, scaler);
		height = Noises.warpPerlin(height, seed.next(), 30, 3, 20.0F);
		height = Noises.warpPerlin(height, seed.next(), 400, 3, 200.0F);
		height = Noises.mul(height, 0.6F * verticalScale);
		return TerrainPopulator.make(TerrainType.HILLS, ground, height, DEFAULT_EROSION, DEFAULT_WEIRDNESS, settings);
	}

	public static TerrainPopulator makeHills2(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings, float verticalScale) {
		Noise height = Noises.cubic(seed.next(), 128, 2);

		Noise scaler1 = Noises.perlin(seed.next(), 32, 4);
		scaler1 = Noises.alpha(scaler1, 0.075F);
		height = Noises.mul(height, scaler1);
		
		height = Noises.warpPerlin(height, seed.next(), 30, 3, 20.0F);
		height = Noises.warpPerlin(height, seed.next(), 400, 3, 200.0F);

		Noise scaler2 = Noises.perlinRidge(seed.next(), 512, 2);
		scaler2 = Noises.alpha(scaler2, 0.8F);
		height = Noises.mul(height, scaler2);
		
		height = Noises.mul(height, 0.55F * verticalScale);
		return TerrainPopulator.make(TerrainType.HILLS, ground, height, DEFAULT_EROSION, DEFAULT_WEIRDNESS, settings);
	}

	public static TerrainPopulator makeDales(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings) {
		Noise hills1 = Noises.billow(seed.next(), 300, 4, 4.0F, 0.8F);
		hills1 = Noises.powCurve(hills1, 0.5F);
		hills1 = Noises.mul(hills1, 0.75F);
		
		Noise hills2 = Noises.billow(seed.next(), 350, 3, 4.0F, 0.8F);
		hills2 = Noises.pow(hills2, 1.25F);
		
		Noise selector = Noises.perlin(seed.next(), 400, 1);
		selector = Noises.clamp(selector, 0.3F, 0.6F);
		selector = Noises.map(selector, 0.0F, 1.0F);
		
		int warpSeed = seed.next();
		
		Noise hillsBlend = Noises.blend(selector, hills1, hills2, 0.4F, 0.75F);
		
		Noise height = hillsBlend;
		height = Noises.pow(height, 1.125F);
		height = Noises.warpPerlin(height, warpSeed, 300, 1, 100.0F);
		return TerrainPopulator.make(TerrainType.HILLS, ground, Noises.mul(height, 0.4F), Noises.threshold(selector, Erosion.LEVEL_2.mid(), Erosion.LEVEL_4.mid(), 0.5F), Noises.min(Noises.mul(height, -1.0F), Noises.constant(-0.06F)), settings);
	}

	public static TerrainPopulator makeBadlands(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings) {
		Noise mask = Noises.perlin(seed.next(), 270, 3);
		mask = Noises.clamp(mask, 0.35F, 0.65F);
		mask = Noises.map(mask, 0.0F, 1.0F);
		
		Noise hills = Noises.perlinRidge(seed.next(), 275, 4);
		hills = Noises.warpPerlin(hills, seed.next(), 400, 2, 100.0F);
		hills = Noises.warpPerlin(hills, seed.next(), 18, 1, 20.0F);
		hills = Noises.mul(hills, mask);
		
		float modulation = 0.4F;
		float alpha = 1.0F - modulation;
		
		Noise mod1 = Noises.warpPerlin(hills, seed.next(), 100, 1, 50.0F);
		mod1 = Noises.mul(mod1, modulation);
		
		Noise lowFreq = Noises.steps(hills, 4, 0.6F, 0.7F);
		lowFreq = Noises.mul(lowFreq, alpha);
		lowFreq = Noises.add(lowFreq, mod1);
		
		Noise highFreq = Noises.steps(hills, 10, 0.6F, 0.7F);
		highFreq = Noises.mul(highFreq, alpha);
		highFreq = Noises.add(highFreq, mod1);
		
		Noise detail = Noises.add(lowFreq, highFreq);
		detail = Noises.alpha(detail, 0.5F);
		
		Noise scaler = Noises.perlin(seed.next(), 200, 3);
		scaler = Noises.mul(scaler, modulation);
		
		Noise mod2 = Noises.mul(hills, scaler);
		
		Noise shape = Noises.steps(hills, 4, 0.65F, 0.75F, Interpolation.CURVE3);
		shape = Noises.mul(shape, alpha);
		shape = Noises.add(shape, mod2);
		shape = Noises.mul(shape, alpha);
		
		Noise height = Noises.mul(shape, detail);
		height = Noises.mul(height, 0.55F);
		height = Noises.add(height, 0.025F);
		return TerrainPopulator.make(TerrainType.BADLANDS, ground, height, DEFAULT_EROSION, DEFAULT_WEIRDNESS, settings);
	}
	
	public static TerrainPopulator makeTorridonian(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain settings) {
		Noise plains = Noises.perlin(seed.next(), 100, 3);
		plains = Noises.warpPerlin(plains, seed.next(), 300, 1, 150.0F);
		plains = Noises.warpPerlin(plains, seed.next(), 20, 1, 40.0F);
		plains = Noises.mul(plains, 0.15F);
		
		Noise hills = Noises.perlin(seed.next(), 150, 4);
		hills = Noises.warpPerlin(hills, seed.next(), 300, 1, 200.0F);
		hills = Noises.warpPerlin(hills, seed.next(), 20, 2, 20.0F);
		hills = Noises.boost(hills);
		
		Noise selector = Noises.perlin(seed.next(), 200, 3);
		
		Noise modulation = Noises.perlin(seed.next(), 120, 1);
		modulation = Noises.mul(modulation, 0.25F);
		
		Noise mask = Noises.perlin(seed.next(), 200, 1);
		mask = Noises.mul(mask, 0.5F);
		mask = Noises.add(mask, 0.5F);
		
		Noise slope = Noises.constant(0.5F);
		
		Noise blend = Noises.blend(selector, plains, hills, 0.6F, 0.6F);
		blend = Noises.advancedTerrace(blend, modulation, mask, slope, 0.0F, 0.3F, 6, 1);
		Noise height = Noises.boost(blend);
		height = Noises.mul(height, 0.5F);

		Noise weirdness = Noises.negative(blend);
		weirdness = Noises.min(weirdness, Noises.constant(Weirdness.LOW_SLICE_NORMAL_DESCENDING.max() - 0.01F));
		
		return TerrainPopulator.make(TerrainType.HILLS, ground, height, Erosion.LEVEL_5.source(), weirdness, settings);
	}

    private static final int MOUNTAINS_H = 610;
    private static final float MOUNTAINS_V = 1.3F;
    private static final int MOUNTAINS3_H = 600;
    private static final float MOUNTAINS3_V = 1.185F;
	private static TerrainPopulator makeMountains(Terrain terrainType, @Deprecated Seed seed, Noise ground, TerrainSettings.Terrain terrainSettings, MiscellaneousSettings miscellaneousSettings,  float verticalScale, boolean makeFancy, boolean legacyScaling) {
		int scaleH = legacyScaling ? Math.round(410.0F * terrainSettings.horizontalScale) : Math.round(MOUNTAINS_H * terrainSettings.horizontalScale * 1.5F);

		Noise height = Noises.perlinRidge(seed.next(), scaleH, 4, 2.35F, 1.15F);

		Noise scaler = Noises.perlin(seed.next(), 24, 4);
		scaler = Noises.alpha(scaler, 0.075F);
		
		height = Noises.mul(height, scaler);
		height = Noises.warpPerlin(height, seed.next(), 350, 1, 150.0F);
		if(makeFancy) {
			height = makeFancy(seed, height);
		}
		height = Noises.cache2d(height);
		return TerrainPopulator.make(terrainType, ground, Noises.mul(height, (legacyScaling ? 0.7F : MOUNTAINS_V) * verticalScale), Erosion.LEVEL_1.source(), Noises.clamp(Noises.mul(height, mountainUsage(miscellaneousSettings, -1.0F)), -0.76F, -0.08F), terrainSettings);
	}

	public static TerrainPopulator makeMountains(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain terrainSettings, MiscellaneousSettings miscellaneousSettings, float verticalScale, boolean makeFancy, boolean legacyScaling) { 
		return makeMountains(TerrainType.MOUNTAINS_1, seed, ground, terrainSettings, miscellaneousSettings,  verticalScale, makeFancy, legacyScaling);
	}
	
	public static TerrainPopulator makeMountainChain(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain terrainSettings, MiscellaneousSettings miscellaneousSettings, float verticalScale, boolean makeFancy, boolean legacyScaling) { 
		return makeMountains(TerrainType.MOUNTAIN_CHAIN, seed, ground, terrainSettings, miscellaneousSettings, verticalScale, makeFancy, legacyScaling);
	}
	
	public static TerrainPopulator makeMountains2(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain terrainSettings, MiscellaneousSettings miscellaneousSettings, float verticalScale, boolean makeFancy, boolean legacyScaling) {
		Noise cell = Noises.worleyEdge(seed.next(), legacyScaling ? 360 : Math.round(360 * terrainSettings.horizontalScale), EdgeFunction.DISTANCE_2, DistanceFunction.EUCLIDEAN);
		cell = Noises.mul(cell, 1.2F);
		cell = Noises.clamp(cell, 0.0F, 1.0F);
		cell = Noises.warpPerlin(cell, seed.next(), 200, 2, 100.0F);
		
		Noise blur = Noises.perlin(seed.next(), 10, 1);
		blur = Noises.alpha(blur, 0.025F);
		
		Noise surface = Noises.perlinRidge(seed.next(), 125, 4);
		surface = Noises.alpha(surface, 0.37F);
		
		Noise height = Noises.clamp(cell, 0.0F, 1.0F);
		height = Noises.mul(height, blur);
		height = Noises.mul(height, surface);
		height = Noises.pow(height, 1.1F);
		if(makeFancy) { 
			height = makeFancy(seed, height);
		}
		height = Noises.cache2d(height);
		return TerrainPopulator.make(TerrainType.MOUNTAINS_2, ground, Noises.mul(height, 0.645F * verticalScale), Erosion.LEVEL_2.source(), Noises.clamp(Noises.mul(height, mountainUsage(miscellaneousSettings, -1.0F)), -0.76F, -0.08F), terrainSettings);
	}
	
    public static TerrainPopulator makeMountains3(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain terrainSettings, MiscellaneousSettings miscellaneousSettings, float verticalScale, boolean makeFancy, boolean legacyScaling) {
    	Noise cell = Noises.worleyEdge(seed.next(), legacyScaling ? 400 : Math.round(MOUNTAINS3_H * terrainSettings.horizontalScale), EdgeFunction.DISTANCE_2, DistanceFunction.EUCLIDEAN);
    	cell = Noises.mul(cell, 1.2F);
    	cell = Noises.clamp(cell, 0.0F, 1.0F);
    	cell = Noises.warpPerlin(cell, seed.next(), 200, 2, 100.0F);

    	Noise blur = Noises.perlin(seed.next(), 10, 1);
    	blur = Noises.alpha(blur, 0.025F);
    	
    	Noise surface = Noises.perlinRidge(seed.next(), 125, 4);
    	surface = Noises.alpha(surface, 0.37F);
    	
    	Noise mountains = Noises.clamp(cell, 0.0F, 1.0F);
    	mountains = Noises.mul(mountains, blur);
    	mountains = Noises.mul(mountains, surface);
    	mountains = Noises.pow(mountains, 1.1F);
    	
    	Noise modulation = Noises.perlin(seed.next(), 50, 1);
    	modulation = Noises.mul(modulation, 0.5F);
    	
    	Noise mask = Noises.perlin(seed.next(), 100, 1);
    	mask = Noises.clamp(mask, 0.5F, 0.95F);
    	mask = Noises.map(mask, 0.0F, 1.0F);
    	
    	Noise slope = Noises.constant(0.45F);
    	
    	Noise height = Noises.advancedTerrace(mountains, modulation, mask, slope, 0.20000000298023224F, 0.44999998807907104F, 24, 1);
    	if(makeFancy) {
        	height = makeFancy(seed, height);
    	}
		height = Noises.cache2d(height);
		return TerrainPopulator.make(TerrainType.MOUNTAINS_3, ground, Noises.mul(height, (legacyScaling ? 0.645F : MOUNTAINS3_V) * verticalScale), Erosion.LEVEL_1.source(), Noises.clamp(Noises.mul(height, mountainUsage(miscellaneousSettings, -1.0F)), -0.76F, -0.08F), terrainSettings);
    }
    
	public static Noise makeFancy(@Deprecated Seed seed, Noise input) {
		Domain domain = Domains.direction(
			Noises.perlin(seed.next(), 10, 1),
			Noises.constant(2.0F)
		);
		Noise erosion = Noises.erosion(input, seed.next(), 2, 0.65F, 128.0F, 0.15F, 3.1F, 0.8F, BlendMode.CONSTANT);
		erosion = Noises.warp(erosion, domain);
		return erosion;
	}
	
	public static TerrainPopulator makeBorder(@Deprecated Seed seed, Noise ground, TerrainSettings.Terrain plainsSettings, TerrainSettings.Terrain steppeSettings, float verticalScale) {
		return makePlains(seed, ground, plainsSettings, steppeSettings, verticalScale);
	}
	
	private static Noise mountainUsage(MiscellaneousSettings miscellaneousSettings, float peak) {
		return Noises.constant(peak * (miscellaneousSettings.mountainBiomeUsage / 0.4F));
	}
}