package org.sunflow;

import org.sunflow.core.AccelerationStructure;
import org.sunflow.core.BucketOrder;
import org.sunflow.core.CameraLens;
import org.sunflow.core.CausticPhotonMapInterface;
import org.sunflow.core.Filter;
import org.sunflow.core.GIEngine;
import org.sunflow.core.GlobalPhotonMapInterface;
import org.sunflow.core.ImageSampler;
import org.sunflow.core.LightSource;
import org.sunflow.core.Modifier;
import org.sunflow.core.PrimitiveList;
import org.sunflow.core.SceneParser;
import org.sunflow.core.Shader;
import org.sunflow.core.Tesselatable;
import org.sunflow.core.accel.BoundingIntervalHierarchy;
import org.sunflow.core.accel.KDTree;
import org.sunflow.core.accel.NullAccelerator;
import org.sunflow.core.accel.UniformGrid;
import org.sunflow.core.bucket.ColumnBucketOrder;
import org.sunflow.core.bucket.DiagonalBucketOrder;
import org.sunflow.core.bucket.HilbertBucketOrder;
import org.sunflow.core.bucket.RandomBucketOrder;
import org.sunflow.core.bucket.RowBucketOrder;
import org.sunflow.core.bucket.SpiralBucketOrder;
import org.sunflow.core.camera.FisheyeLens;
import org.sunflow.core.camera.PinholeLens;
import org.sunflow.core.camera.SphericalLens;
import org.sunflow.core.camera.ThinLens;
import org.sunflow.core.filter.BlackmanHarrisFilter;
import org.sunflow.core.filter.BoxFilter;
import org.sunflow.core.filter.CatmullRomFilter;
import org.sunflow.core.filter.CubicBSpline;
import org.sunflow.core.filter.GaussianFilter;
import org.sunflow.core.filter.LanczosFilter;
import org.sunflow.core.filter.MitchellFilter;
import org.sunflow.core.filter.SincFilter;
import org.sunflow.core.filter.TriangleFilter;
import org.sunflow.core.gi.AmbientOcclusionGIEngine;
import org.sunflow.core.gi.FakeGIEngine;
import org.sunflow.core.gi.InstantGI;
import org.sunflow.core.gi.IrradianceCacheGIEngine;
import org.sunflow.core.gi.PathTracingGIEngine;
import org.sunflow.core.light.DirectionalSpotlight;
import org.sunflow.core.light.ImageBasedLight;
import org.sunflow.core.light.PointLight;
import org.sunflow.core.light.SphereLight;
import org.sunflow.core.light.SunSkyLight;
import org.sunflow.core.light.TriangleMeshLight;
import org.sunflow.core.modifiers.BumpMappingModifier;
import org.sunflow.core.modifiers.NormalMapModifier;
import org.sunflow.core.modifiers.PerlinModifier;
import org.sunflow.core.parser.RA2Parser;
import org.sunflow.core.parser.RA3Parser;
import org.sunflow.core.parser.SCAsciiParser;
import org.sunflow.core.parser.SCBinaryParser;
import org.sunflow.core.parser.SCParser;
import org.sunflow.core.parser.ShaveRibParser;
import org.sunflow.core.photonmap.CausticPhotonMap;
import org.sunflow.core.photonmap.GlobalPhotonMap;
import org.sunflow.core.photonmap.GridPhotonMap;
import org.sunflow.core.primitive.Background;
import org.sunflow.core.primitive.BanchoffSurface;
import org.sunflow.core.primitive.Box;
import org.sunflow.core.primitive.CornellBox;
import org.sunflow.core.primitive.Cylinder;
import org.sunflow.core.primitive.Hair;
import org.sunflow.core.primitive.JuliaFractal;
import org.sunflow.core.primitive.ParticleSurface;
import org.sunflow.core.primitive.Plane;
import org.sunflow.core.primitive.QuadMesh;
import org.sunflow.core.primitive.Sphere;
import org.sunflow.core.primitive.SphereFlake;
import org.sunflow.core.primitive.Torus;
import org.sunflow.core.primitive.TriangleMesh;
import org.sunflow.core.renderer.BucketRenderer;
import org.sunflow.core.renderer.MultipassRenderer;
import org.sunflow.core.renderer.ProgressiveRenderer;
import org.sunflow.core.renderer.SimpleRenderer;
import org.sunflow.core.shader.AmbientOcclusionShader;
import org.sunflow.core.shader.AnisotropicWardShader;
import org.sunflow.core.shader.ConstantShader;
import org.sunflow.core.shader.DiffuseShader;
import org.sunflow.core.shader.GlassShader;
import org.sunflow.core.shader.IDShader;
import org.sunflow.core.shader.MirrorShader;
import org.sunflow.core.shader.NormalShader;
import org.sunflow.core.shader.PhongShader;
import org.sunflow.core.shader.PrimIDShader;
import org.sunflow.core.shader.QuickGrayShader;
import org.sunflow.core.shader.ShinyDiffuseShader;
import org.sunflow.core.shader.SimpleShader;
import org.sunflow.core.shader.TexturedAmbientOcclusionShader;
import org.sunflow.core.shader.TexturedDiffuseShader;
import org.sunflow.core.shader.TexturedPhongShader;
import org.sunflow.core.shader.TexturedShinyDiffuseShader;
import org.sunflow.core.shader.TexturedWardShader;
import org.sunflow.core.shader.UVShader;
import org.sunflow.core.shader.UberShader;
import org.sunflow.core.shader.ViewCausticsShader;
import org.sunflow.core.shader.ViewGlobalPhotonsShader;
import org.sunflow.core.shader.ViewIrradianceShader;
import org.sunflow.core.shader.WireframeShader;
import org.sunflow.core.tesselatable.BezierMesh;
import org.sunflow.core.tesselatable.FileMesh;
import org.sunflow.core.tesselatable.Gumbo;
import org.sunflow.core.tesselatable.Teapot;
import org.sunflow.image.BitmapReader;
import org.sunflow.image.BitmapWriter;
import org.sunflow.image.readers.BMPBitmapReader;
import org.sunflow.image.readers.HDRBitmapReader;
import org.sunflow.image.readers.IGIBitmapReader;
import org.sunflow.image.readers.JPGBitmapReader;
import org.sunflow.image.readers.PNGBitmapReader;
import org.sunflow.image.readers.TGABitmapReader;
import org.sunflow.image.writers.EXRBitmapWriter;
import org.sunflow.image.writers.HDRBitmapWriter;
import org.sunflow.image.writers.IGIBitmapWriter;
import org.sunflow.image.writers.PNGBitmapWriter;
import org.sunflow.image.writers.TGABitmapWriter;
import org.sunflow.system.Plugins;

/**
 * This class acts as the central repository for all user extensible types in
 * Sunflow, even built-in types are registered here. This class is static so
 * that new plugins may be reused by an application across several render
 * scenes.
 */
public final class PluginRegistry {
    // base types - needed by SunflowAPI

    public static final Plugins<PrimitiveList> PRIMITIVE_PLUGINS = new Plugins<PrimitiveList>(PrimitiveList.class);
    public static final Plugins<Tesselatable> TESSELATABLE_PLUGINS = new Plugins<Tesselatable>(Tesselatable.class);
    public static final Plugins<Shader> SHADER_PLUGINS = new Plugins<Shader>(Shader.class);
    public static final Plugins<Modifier> MODIFIER_PLUGINS = new Plugins<Modifier>(Modifier.class);
    public static final Plugins<LightSource> LIGHT_SOURCE_PLUGINS = new Plugins<LightSource>(LightSource.class);
    public static final Plugins<CameraLens> CAMERA_LENS_PLUGINS = new Plugins<CameraLens>(CameraLens.class);
    // advanced types - used inside the Sunflow core
    public static final Plugins<AccelerationStructure> ACCEL_PLUGINS = new Plugins<AccelerationStructure>(AccelerationStructure.class);
    public static final Plugins<BucketOrder> BUCKET_ORDER_PLUGINS = new Plugins<BucketOrder>(BucketOrder.class);
    public static final Plugins<Filter> FILTER_PLUGINS = new Plugins<Filter>(Filter.class);
    public static final Plugins<GIEngine> GI_ENGINE_PLUGINS = new Plugins<GIEngine>(GIEngine.class);
    public static final Plugins<CausticPhotonMapInterface> CAUSTIC_PHOTON_MAP_PLUGINS = new Plugins<CausticPhotonMapInterface>(CausticPhotonMapInterface.class);
    public static final Plugins<GlobalPhotonMapInterface> GLOBAL_PHOTON_MAP_PLUGINS = new Plugins<GlobalPhotonMapInterface>(GlobalPhotonMapInterface.class);
    public static final Plugins<ImageSampler> IMAGE_SAMPLE_PLUGINS = new Plugins<ImageSampler>(ImageSampler.class);
    public static final Plugins<SceneParser> PARSER_PLUGINS = new Plugins<SceneParser>(SceneParser.class);
    public static final Plugins<BitmapReader> BITMAP_READER_PLUGINS = new Plugins<BitmapReader>(BitmapReader.class);
    public static final Plugins<BitmapWriter> BITMAP_WRITER_PLUGINS = new Plugins<BitmapWriter>(BitmapWriter.class);

    // Register all plugins on startup:
    static {
        // primitives
        PRIMITIVE_PLUGINS.registerPlugin("triangle_mesh", TriangleMesh.class);
        PRIMITIVE_PLUGINS.registerPlugin("sphere", Sphere.class);
        PRIMITIVE_PLUGINS.registerPlugin("cylinder", Cylinder.class);
        PRIMITIVE_PLUGINS.registerPlugin("box", Box.class);
        PRIMITIVE_PLUGINS.registerPlugin("banchoff", BanchoffSurface.class);
        PRIMITIVE_PLUGINS.registerPlugin("hair", Hair.class);
        PRIMITIVE_PLUGINS.registerPlugin("julia", JuliaFractal.class);
        PRIMITIVE_PLUGINS.registerPlugin("particles", ParticleSurface.class);
        PRIMITIVE_PLUGINS.registerPlugin("plane", Plane.class);
        PRIMITIVE_PLUGINS.registerPlugin("quad_mesh", QuadMesh.class);
        PRIMITIVE_PLUGINS.registerPlugin("torus", Torus.class);
        PRIMITIVE_PLUGINS.registerPlugin("background", Background.class);
        PRIMITIVE_PLUGINS.registerPlugin("sphereflake", SphereFlake.class);
    }

    static {
        // tesslatable
        TESSELATABLE_PLUGINS.registerPlugin("bezier_mesh", BezierMesh.class);
        TESSELATABLE_PLUGINS.registerPlugin("file_mesh", FileMesh.class);
        TESSELATABLE_PLUGINS.registerPlugin("gumbo", Gumbo.class);
        TESSELATABLE_PLUGINS.registerPlugin("teapot", Teapot.class);
    }

    static {
        // shaders
        SHADER_PLUGINS.registerPlugin("ambient_occlusion", AmbientOcclusionShader.class);
        SHADER_PLUGINS.registerPlugin("constant", ConstantShader.class);
        SHADER_PLUGINS.registerPlugin("diffuse", DiffuseShader.class);
        SHADER_PLUGINS.registerPlugin("glass", GlassShader.class);
        SHADER_PLUGINS.registerPlugin("mirror", MirrorShader.class);
        SHADER_PLUGINS.registerPlugin("phong", PhongShader.class);
        SHADER_PLUGINS.registerPlugin("shiny_diffuse", ShinyDiffuseShader.class);
        SHADER_PLUGINS.registerPlugin("uber", UberShader.class);
        SHADER_PLUGINS.registerPlugin("ward", AnisotropicWardShader.class);
        SHADER_PLUGINS.registerPlugin("wireframe", WireframeShader.class);

        // textured shaders
        SHADER_PLUGINS.registerPlugin("textured_ambient_occlusion", TexturedAmbientOcclusionShader.class);
        SHADER_PLUGINS.registerPlugin("textured_diffuse", TexturedDiffuseShader.class);
        SHADER_PLUGINS.registerPlugin("textured_phong", TexturedPhongShader.class);
        SHADER_PLUGINS.registerPlugin("textured_shiny_diffuse", TexturedShinyDiffuseShader.class);
        SHADER_PLUGINS.registerPlugin("textured_ward", TexturedWardShader.class);

        // preview shaders
        SHADER_PLUGINS.registerPlugin("quick_gray", QuickGrayShader.class);
        SHADER_PLUGINS.registerPlugin("simple", SimpleShader.class);
        SHADER_PLUGINS.registerPlugin("show_normals", NormalShader.class);
        SHADER_PLUGINS.registerPlugin("show_uvs", UVShader.class);
        SHADER_PLUGINS.registerPlugin("show_instance_id", IDShader.class);
        SHADER_PLUGINS.registerPlugin("show_primitive_id", PrimIDShader.class);
        SHADER_PLUGINS.registerPlugin("view_caustics", ViewCausticsShader.class);
        SHADER_PLUGINS.registerPlugin("view_global", ViewGlobalPhotonsShader.class);
        SHADER_PLUGINS.registerPlugin("view_irradiance", ViewIrradianceShader.class);
    }

    static {
        // modifiers
        MODIFIER_PLUGINS.registerPlugin("bump_map", BumpMappingModifier.class);
        MODIFIER_PLUGINS.registerPlugin("normal_map", NormalMapModifier.class);
        MODIFIER_PLUGINS.registerPlugin("perlin", PerlinModifier.class);
    }

    static {
        // light sources
        LIGHT_SOURCE_PLUGINS.registerPlugin("directional", DirectionalSpotlight.class);
        LIGHT_SOURCE_PLUGINS.registerPlugin("ibl", ImageBasedLight.class);
        LIGHT_SOURCE_PLUGINS.registerPlugin("point", PointLight.class);
        LIGHT_SOURCE_PLUGINS.registerPlugin("sphere", SphereLight.class);
        LIGHT_SOURCE_PLUGINS.registerPlugin("sunsky", SunSkyLight.class);
        LIGHT_SOURCE_PLUGINS.registerPlugin("triangle_mesh", TriangleMeshLight.class);
        LIGHT_SOURCE_PLUGINS.registerPlugin("cornell_box", CornellBox.class);
    }

    static {
        // camera lenses
        CAMERA_LENS_PLUGINS.registerPlugin("pinhole", PinholeLens.class);
        CAMERA_LENS_PLUGINS.registerPlugin("thinlens", ThinLens.class);
        CAMERA_LENS_PLUGINS.registerPlugin("fisheye", FisheyeLens.class);
        CAMERA_LENS_PLUGINS.registerPlugin("spherical", SphericalLens.class);
    }

    static {
        // accels
        ACCEL_PLUGINS.registerPlugin("bih", BoundingIntervalHierarchy.class);
        ACCEL_PLUGINS.registerPlugin("kdtree", KDTree.class);
        ACCEL_PLUGINS.registerPlugin("null", NullAccelerator.class);
        ACCEL_PLUGINS.registerPlugin("uniformgrid", UniformGrid.class);
    }

    static {
        // bucket orders
        BUCKET_ORDER_PLUGINS.registerPlugin("column", ColumnBucketOrder.class);
        BUCKET_ORDER_PLUGINS.registerPlugin("diagonal", DiagonalBucketOrder.class);
        BUCKET_ORDER_PLUGINS.registerPlugin("hilbert", HilbertBucketOrder.class);
        BUCKET_ORDER_PLUGINS.registerPlugin("random", RandomBucketOrder.class);
        BUCKET_ORDER_PLUGINS.registerPlugin("row", RowBucketOrder.class);
        BUCKET_ORDER_PLUGINS.registerPlugin("spiral", SpiralBucketOrder.class);
    }

    static {
        // filters
        FILTER_PLUGINS.registerPlugin("blackman-harris", BlackmanHarrisFilter.class);
        FILTER_PLUGINS.registerPlugin("box", BoxFilter.class);
        FILTER_PLUGINS.registerPlugin("catmull-rom", CatmullRomFilter.class);
        FILTER_PLUGINS.registerPlugin("gaussian", GaussianFilter.class);
        FILTER_PLUGINS.registerPlugin("lanczos", LanczosFilter.class);
        FILTER_PLUGINS.registerPlugin("mitchell", MitchellFilter.class);
        FILTER_PLUGINS.registerPlugin("sinc", SincFilter.class);
        FILTER_PLUGINS.registerPlugin("triangle", TriangleFilter.class);
        FILTER_PLUGINS.registerPlugin("bspline", CubicBSpline.class);
    }

    static {
        // gi engines
        GI_ENGINE_PLUGINS.registerPlugin("ambocc", AmbientOcclusionGIEngine.class);
        GI_ENGINE_PLUGINS.registerPlugin("fake", FakeGIEngine.class);
        GI_ENGINE_PLUGINS.registerPlugin("igi", InstantGI.class);
        GI_ENGINE_PLUGINS.registerPlugin("irr-cache", IrradianceCacheGIEngine.class);
        GI_ENGINE_PLUGINS.registerPlugin("path", PathTracingGIEngine.class);
    }

    static {
        // caustic photon maps
        CAUSTIC_PHOTON_MAP_PLUGINS.registerPlugin("kd", CausticPhotonMap.class);
    }

    static {
        // global photon maps
        GLOBAL_PHOTON_MAP_PLUGINS.registerPlugin("grid", GridPhotonMap.class);
        GLOBAL_PHOTON_MAP_PLUGINS.registerPlugin("kd", GlobalPhotonMap.class);
    }

    static {
        // image samplers
        IMAGE_SAMPLE_PLUGINS.registerPlugin("bucket", BucketRenderer.class);
        IMAGE_SAMPLE_PLUGINS.registerPlugin("ipr", ProgressiveRenderer.class);
        IMAGE_SAMPLE_PLUGINS.registerPlugin("fast", SimpleRenderer.class);
        IMAGE_SAMPLE_PLUGINS.registerPlugin("multipass", MultipassRenderer.class);
    }

    static {
        // parsers
        PARSER_PLUGINS.registerPlugin("sc", SCParser.class);
        PARSER_PLUGINS.registerPlugin("sca", SCAsciiParser.class);
        PARSER_PLUGINS.registerPlugin("scb", SCBinaryParser.class);
        PARSER_PLUGINS.registerPlugin("rib", ShaveRibParser.class);
        PARSER_PLUGINS.registerPlugin("ra2", RA2Parser.class);
        PARSER_PLUGINS.registerPlugin("ra3", RA3Parser.class);
    }

    static {
        // bitmap readers
        BITMAP_READER_PLUGINS.registerPlugin("hdr", HDRBitmapReader.class);
        BITMAP_READER_PLUGINS.registerPlugin("tga", TGABitmapReader.class);
        BITMAP_READER_PLUGINS.registerPlugin("png", PNGBitmapReader.class);
        BITMAP_READER_PLUGINS.registerPlugin("jpg", JPGBitmapReader.class);
        BITMAP_READER_PLUGINS.registerPlugin("bmp", BMPBitmapReader.class);
        BITMAP_READER_PLUGINS.registerPlugin("igi", IGIBitmapReader.class);
    }

    static {
        // bitmap writers
        BITMAP_WRITER_PLUGINS.registerPlugin("png", PNGBitmapWriter.class);
        BITMAP_WRITER_PLUGINS.registerPlugin("hdr", HDRBitmapWriter.class);
        BITMAP_WRITER_PLUGINS.registerPlugin("tga", TGABitmapWriter.class);
        BITMAP_WRITER_PLUGINS.registerPlugin("exr", EXRBitmapWriter.class);
        BITMAP_WRITER_PLUGINS.registerPlugin("igi", IGIBitmapWriter.class);
    }
}