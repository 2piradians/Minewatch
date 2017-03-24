package twopiradians.minewatch.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAnaBullet extends ModelBase {

	public ModelRenderer pellet;
	
	public ModelAnaBullet() {
		this.textureWidth = 16;
		this.textureHeight = 16;
		
		this.pellet = new ModelRenderer(this);
		this.pellet.addBox(-1.5f, -0.5f, -10f, 1, 1, 10);
		this.pellet.setRotationPoint(1f, 1f, 1f);
	}
	
	@Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.pellet.render(scale);
    }	
}
