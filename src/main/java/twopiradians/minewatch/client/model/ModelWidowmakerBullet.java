package twopiradians.minewatch.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelWidowmakerBullet extends ModelBase {

	public ModelRenderer bullet;
	
	public ModelWidowmakerBullet() {
		this.textureWidth = 16;
		this.textureHeight = 16;
		
		this.bullet = new ModelRenderer(this);
		this.bullet.addBox(-1.5f, -0.5f, -10f, 1, 1, 10);
		this.bullet.setRotationPoint(1f, 1f, 1f);
	}
	
	@Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.bullet.render(scale);
    }	
}
