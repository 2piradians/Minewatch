package twopiradians.minewatch.common.util;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Iterator;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;

@SuppressWarnings("unused")
/**Copied from SplashProgress.java (1.10.2)*/
public class Texture
{
	private static final IResourcePack mcPack = Minecraft.getMinecraft().mcDefaultResourcePack;
	private static final IntBuffer buf = BufferUtils.createIntBuffer(4 * 1024 * 1024);
	
    private final ResourceLocation location;
    private final int name;
    private final int width;
    private final int height;
    private final int frames;
    private final int size;

    public Texture(ResourceLocation location)
    {
        this(location, true);
    }

    public Texture(ResourceLocation location, boolean allowRP)
    {
        InputStream s = null;
        try
        {
            this.location = location;
            s = open(location, allowRP);
            ImageInputStream stream = ImageIO.createImageInputStream(s);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if(!readers.hasNext()) throw new IOException("No suitable reader found for image" + location);
            ImageReader reader = readers.next();
            reader.setInput(stream);
            frames = reader.getNumImages(true);
            BufferedImage[] images = new BufferedImage[frames];
            for(int i = 0; i < frames; i++)
            {
                images[i] = reader.read(i);
            }
            reader.dispose();
            int size = 1;
            width = images[0].getWidth();
            height = images[0].getHeight();
            while((size / width) * (size / height) < frames) size *= 2;
            this.size = size;
            glEnable(GL_TEXTURE_2D);
            //synchronized(SplashProgress.class)
            {
                name = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, name);
            }
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)null);
            //checkGLError("Texture creation");
            for(int i = 0; i * (size / width) < frames; i++)
            {
                for(int j = 0; i * (size / width) + j < frames && j < size / width; j++)
                {
                    buf.clear();
                    BufferedImage image = images[i * (size / width) + j];
                    for(int k = 0; k < height; k++)
                    {
                        for(int l = 0; l < width; l++)
                        {
                            buf.put(image.getRGB(l, k));
                        }
                    }
                    buf.position(0).limit(width * height);
                    glTexSubImage2D(GL_TEXTURE_2D, 0, j * width, i * height, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buf);
                    //checkGLError("Texture uploading");
                }
            }
            glBindTexture(GL_TEXTURE_2D, 0);
            glDisable(GL_TEXTURE_2D);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(s);
        }
    }

    public ResourceLocation getLocation()
    {
        return location;
    }

    public int getName()
    {
        return name;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getFrames()
    {
        return frames;
    }

    public int getSize()
    {
        return size;
    }

    public void bind()
    {
        glBindTexture(GL_TEXTURE_2D, name);
    }

    public void delete()
    {
        glDeleteTextures(name);
    }

    public float getU(int frame, float u)
    {
        return width * (frame % (size / width) + u) / size;
    }

    public float getV(int frame, float v)
    {
        return height * (frame / (size / width) + v) / size;
    }

    public void texCoord(int frame, float u, float v)
    {
        glTexCoord2f(getU(frame, u), getV(frame, v));
    }
    
    private static InputStream open(ResourceLocation loc, boolean allowRP) throws IOException
    {
        /*if (!allowRP)
            return mcPack.getInputStream(loc);

        if(miscPack.resourceExists(loc))
        {
            return miscPack.getInputStream(loc);
        }
        else if(fmlPack.resourceExists(loc))
        {
            return fmlPack.getInputStream(loc);
        }*/
        return mcPack.getInputStream(loc);
    }
}
