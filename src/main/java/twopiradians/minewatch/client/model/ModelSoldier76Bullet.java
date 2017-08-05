package twopiradians.minewatch.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSoldier76Bullet extends ModelBase {

	public ModelRenderer bullet;
	
	public ModelSoldier76Bullet() {
		this.textureWidth = 16;
		this.textureHeight = 16;
		
		this.bullet = new ModelRenderer(this);
		this.bullet.addBox(-0.5f, 0.5f, -1.5f, 1, 1, 3);
		this.bullet.setRotationPoint(0f, 0f, 0f);
	}
	
	@Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.bullet.render(scale);
    }	
}
