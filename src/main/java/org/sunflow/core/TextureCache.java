package org.sunflow.core;
import java.util.HashMap;
import org.sunflow.system.UI;
import org.sunflow.system.UI.Module;

/**
 * Maintains a cache of all loaded texture maps. This is usefull if the same
 * texture might be used more than once in your scene.
 */
public final class TextureCache {
    private static final HashMap<String, Texture> TEXTURES = new HashMap<>();

    private TextureCache() {
    }

    /**
     * Gets a reference to the texture specified by the given filename. If the
     * texture has already been loaded the previous reference is returned,
     * otherwise, a new texture is created.
     *
     * @param filename image file to load
     * @param isLinear is the texture gamma corrected?
     * @return texture object
     * @see Texture
     */
    public synchronized static Texture getTexture(String filename, boolean isLinear) {
        if (TEXTURES.containsKey(filename)) {
            UI.printInfo(Module.TEX, "Using cached copy for file \"%s\" ...", filename);
            return TEXTURES.get(filename);
        }
        UI.printInfo(Module.TEX, "Using file \"%s\" ...", filename);
        Texture t = new Texture(filename, isLinear);
        TEXTURES.put(filename, t);
        return t;
    }

    /**
     * Flush all TEXTURES from the cache, this will cause them to be reloaded
 anew the next time they are accessed.
     */
    public synchronized static void flush() {
        UI.printInfo(Module.TEX, "Flushing texture cache");
        TEXTURES.clear();
    }
}