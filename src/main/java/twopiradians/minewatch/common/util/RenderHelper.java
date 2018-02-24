package twopiradians.minewatch.common.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;

public class RenderHelper {

	/**Modified from {@link GuiUtils#drawHoveringText(ItemStack, List, int, int, int, int, int, net.minecraft.client.gui.FontRenderer)} to be scalable / less derpy*/
	public static void drawHoveringText(@Nullable ItemStack stack, List<String> textLines, int x, int y, double scaleX, double scaleY, int maxTextWidth) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = new ScaledResolution(mc);
		
		RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, textLines, x, y, resolution.getScaledWidth(), resolution.getScaledHeight(), maxTextWidth, mc.fontRenderer);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        x = (int) (event.getX() / scaleX);
        y = (int) (event.getY() / scaleY);
        int width = (int) (event.getScreenWidth() / scaleX);
        int height = (int) (event.getScreenHeight() / scaleY);
        maxTextWidth = event.getMaxWidth();
        
        GlStateManager.disableRescaleNormal();
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        int tooltipTextWidth = 0;

        for (String textLine : textLines)
        {
            int textLineWidth = mc.fontRenderer.getStringWidth(textLine);

            if (textLineWidth > tooltipTextWidth)
            {
                tooltipTextWidth = textLineWidth;
            }
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = x + 12;
        if (tooltipX + tooltipTextWidth + 4 > width)
        {
            tooltipX = x - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (x > width / 2)
                {
                    tooltipTextWidth = x - 12 - 8;
                }
                else
                {
                    tooltipTextWidth = width - 16 - x;
                }
                needsWrap = true;
            }
        }

        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth)
        {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if (needsWrap)
        {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<String>();
            for (int i = 0; i < textLines.size(); i++)
            {
                String textLine = textLines.get(i);
                List<String> wrappedLine = mc.fontRenderer.listFormattedStringToWidth(textLine, tooltipTextWidth);
                if (i == 0)
                {
                    titleLinesCount = wrappedLine.size();
                }

                for (String line : wrappedLine)
                {
                    int lineWidth = mc.fontRenderer.getStringWidth(line);
                    if (lineWidth > wrappedTooltipWidth)
                    {
                        wrappedTooltipWidth = lineWidth;
                    }
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (x > width / 2)
            {
                tooltipX = x - 16 - tooltipTextWidth;
            }
            else
            {
                tooltipX = x + 12;
            }
        }

        int tooltipY = y - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1)
        {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount) {
                tooltipHeight += 2; // gap between title lines and next lines
            }
        }

        if (tooltipY + tooltipHeight + 6 > height)
        {
            tooltipY = height - tooltipHeight - 6;
        }

        final int zLevel = 300;
        final int backgroundColor = 0xF0100010;
        GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        final int borderColorStart = 0x505000FF;
        final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
        GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, tooltipX, tooltipY, mc.fontRenderer, tooltipTextWidth, tooltipHeight));
        int tooltipTop = tooltipY;
        
        for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber)
        {
            String line = textLines.get(lineNumber);
            mc.fontRenderer.drawStringWithShadow(line, (float)tooltipX, (float)tooltipY, -1);

            if (lineNumber + 1 == titleLinesCount)
            {
                tooltipY += 2;
            }

            tooltipY += 10;
        }

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, tooltipX, tooltipTop, mc.fontRenderer, tooltipTextWidth, tooltipHeight));

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
	}
	
}