package twopiradians.minewatch.client.render.entity;

import java.awt.Color;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderFactory implements IRenderFactory {

	private int width;
	private int height;
	private int depth;
	private String texture;
	private Color color;

	public RenderFactory() {
		this("", 0, 0, 0);
	}
	
	public RenderFactory(String texture) {
		this(texture, 0, 0, 0);
	}

	public RenderFactory(Color color, int width, int height, int depth) {
		this("white", width, height, depth);
		this.color = color;
	}

	public RenderFactory(String texture, int width, int height, int depth) {
		this.texture = texture;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	@Override
	public Render createRenderFor(RenderManager manager) {
		if (texture.contains("arrow"))
			return new RenderHanzoArrow(manager, texture);
		else
			return new RenderSimple(manager, color, texture, width, height, depth);
	}
}
