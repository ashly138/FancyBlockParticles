package com.TominoCZ.FBP.gui;

import java.io.IOException;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class FBPGuiExceptionList extends GuiScreen {
	private GuiTextField search;

	int ID = 1;

	GuiScreen parent;

	boolean okToAdd = false;

	FBPGuiButton buttonAdd, buttonRemove;

	public FBPGuiExceptionList(GuiScreen parent) {
		this.parent = parent;
	}

	public void initGui() {
		this.buttonList.clear();

		int width = 100;
		int height = 20;

		search = new GuiTextField(0, mc.fontRendererObj, this.width / 2 - width / 2, this.height / 2 - 75, width,
				height);
		search.setFocused(true);
		search.setCanLoseFocus(true);
		search.setText((ID = FBP.lastIDAdded) + "");

		buttonAdd = new FBPGuiButton(1, search.xPosition, search.yPosition + 130, "\u00A7aADD", false, false);
		buttonRemove = new FBPGuiButton(2, buttonAdd.xPosition, buttonAdd.yPosition + 20, "\u00A7cREMOVE", false,
				false);
		buttonAdd.width = buttonRemove.width = width;

		okToAdd = !FBP.blockExceptions.contains(FBP.lastIDAdded);

		this.buttonList.add(buttonAdd);
		this.buttonList.add(buttonRemove);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);
		search.mouseClicked(x, y, button);
		if (button == 1 && x >= search.xPosition && x < search.xPosition + search.width && y >= search.yPosition
				&& y < search.yPosition + search.height) {
			search.setText("");
		}
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		if (keyCode == 1) {
			closeGui();
			return;
		}

		super.keyTyped(c, keyCode);

		try {
			if (keyCode != 14 && keyCode != 200 && keyCode != 208)
				Integer.parseInt("" + c);

			search.textboxKeyTyped(c, keyCode);

			ID = Integer.parseInt(search.getText());

			if (keyCode == 200 && ID < Integer.MAX_VALUE - 1)
				search.setText((ID = ID + 1) + "");
			else if (keyCode == 208 && ID > 0)
				search.setText((ID = ID - 1) + "");
		} catch (Exception e) {

		}

		okToAdd = !FBP.blockExceptions.contains(ID);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		search.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		parent.width = this.width;
		parent.height = this.height;

		parent.initGui();
		parent.drawScreen(0, 0, partialTicks);

		this.drawDefaultBackground();
		search.drawTextBox();

		Block b = Block.REGISTRY.getObjectById(ID);

		String name = "";

		buttonAdd.enabled = okToAdd;
		buttonRemove.enabled = !okToAdd;

		if (b != null) {
			String itemName = I18n.format(b.getRegistryName().getResourcePath());
			//Item i = Item.getItemFromBlock(b);
			name = itemName;

			//if (i == null)
				//i = Item.getByNameOrId(b.getRegistryName().getResourceDomain() + ':' + itemName);
			
			
			
			//if (i != null)
				//drawStack(new ItemStack(i));
			//else
				drawStack(new ItemStack(b, 1, 0));
		} else {
			buttonAdd.enabled = false;
			name = "";
		}

		this.drawCenteredString(fontRendererObj, name, buttonAdd.xPosition + buttonAdd.width / 2,
				buttonAdd.yPosition - 25, fontRendererObj.getColorCode('6'));

		this.drawCenteredString(fontRendererObj, "\u00A7LAdd Exception For Blocks", width / 2, 20,
				fontRendererObj.getColorCode('a'));

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawStack(ItemStack itemstack) {
		GlStateManager.enableDepth();
		GlStateManager.enableLight(0);

		int x = search.xPosition + search.width / 2 - 32;
		int y = search.yPosition + 30;

		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(4, 4, 4);
		
		this.itemRender.renderItemAndEffectIntoGUI(itemstack, 0, 0);
		
		GlStateManager.scale(0.25, 0.25, 0.25);
		GlStateManager.translate(-x, -y, 0);

		this.itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 1:
			if (!FBP.blockExceptions.contains(ID))
				FBP.blockExceptions.add(FBP.lastIDAdded = ID);

			FBPConfigHandler.writeExceptions();
			break;
		case 2:
			if (FBP.blockExceptions.contains(ID))
				FBP.blockExceptions.remove((Integer) ID);

			FBPConfigHandler.writeExceptions();
			break;
		}

		okToAdd = !FBP.blockExceptions.contains(ID);
	}

	void closeGui() {
		mc.displayGuiScreen(parent);
	}
}