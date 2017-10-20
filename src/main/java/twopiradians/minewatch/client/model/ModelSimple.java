package twopiradians.minewatch.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSimple extends ModelBase {

	public ModelRenderer model;

	public ModelSimple(int width, int height, int depth) {
		this.textureWidth = 16;
		this.textureHeight = 16;

		this.model = new ModelRenderer(this);
		this.model.addBox(0, 0, 0, width, height, depth);
		this.model.setRotationPoint(-width/2f, -height/2f, -depth/2f);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.model.render(scale);
	}	
}
