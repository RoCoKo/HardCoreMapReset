package modmuss50.HardCoreMapReset;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GuiMapList extends GuiScreen {
	private static final Logger logger = LogManager.getLogger();
	private static final int CREATE_BUTTON_ID = 0;
	private static final int CANCEL_BUTTON_ID = 1;
	public GuiScreen parent;
	private TemplateSaveLoader saveLoader;
	private List<WorldSummary> saveList;
	private GuiButton createButton;
	private GuiTextField nameField;
	private MapList mapList;
	private SimpleDateFormat dateFormat = new SimpleDateFormat();
	private int selectedSlot;
	private String folderString;
	private static final String[] portNames = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

	public GuiMapList(GuiScreen parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		try {
			initSaveList();
		} catch (AnvilConverterException anvilconverterexception) {
			logger.error("Couldn't load level list", anvilconverterexception);
			this.mc.displayGuiScreen(new GuiErrorScreen("Unable to load worlds", anvilconverterexception.getMessage()));
			return;
		}

		this.nameField = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, 45, 200, 20);
		this.nameField.setFocused(true);
		this.nameField.setText(I18n.format("selectWorld.newWorld"));
		sanitizeFolderName();

		this.mapList = new MapList();

		this.buttonList.add(this.createButton = new GuiButton(CREATE_BUTTON_ID, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("selectWorld.create")));
		this.buttonList.add(new GuiButton(CANCEL_BUTTON_ID, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));
		this.createButton.enabled = false;
	}


	private void initSaveList() throws AnvilConverterException {
		if (saveLoader == null) {
			DataFixer fixer = null;
			if (Minecraft.getMinecraft().getSaveLoader() instanceof SaveFormatOld) {
				SaveFormatOld old = (SaveFormatOld) Minecraft.getMinecraft().getSaveLoader();
				try {
					Field field = ReflectionHelper.findField(SaveFormatOld.class, "dataFixer", "field_186354_b", "b");
					field.setAccessible(true);
					fixer = (DataFixer) field.get(old);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			saveLoader = new TemplateSaveLoader(new File(Minecraft.getMinecraft().mcDataDir, "maps"), fixer);
		}
		this.saveList = saveLoader.getSaveList();
		Collections.sort(this.saveList);
		this.selectedSlot = -1;
	}

	@Override
	public void actionPerformed(GuiButton guiButton) throws IOException {
		super.actionPerformed(guiButton);
		if (guiButton.id == CANCEL_BUTTON_ID) {
			Minecraft.getMinecraft().displayGuiScreen(parent);
		} else if (guiButton.id == CREATE_BUTTON_ID) {
			createMap();

			//if (Minecraft.getMinecraft().getSaveLoader().canLoadWorld(getSave().getFileName())) {
				net.minecraftforge.fml.client.FMLClientHandler.instance().tryLoadExistingWorld(new GuiWorldSelection(this), getSave());
		//	}
		}
		mapList.actionPerformed(guiButton);
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode) throws IOException {
		if (keyCode == 28 || keyCode == 156) {
			this.actionPerformed((GuiButton) this.buttonList.get(0));
		}

		if (this.nameField.isFocused()) {
			this.nameField.textboxKeyTyped(keyChar, keyCode);
		}

		this.sanitizeFolderName();
	}

	/**
	 * Handles mouse input.
	 */
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.mapList.handleMouseInput();
	}

	// TODO Cleanup
	public void sanitizeFolderName() {
		ISaveFormat saveLoader = this.mc.getSaveLoader();
		this.folderString = this.nameField.getText().trim();
		this.folderString = this.folderString.replaceAll("[\\./\"]", "_");
		char[] achar = ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS;
		int i = achar.length;

		for (int j = 0; j < i; ++j) {
			char c0 = achar[j];
			this.folderString = this.folderString.replace(c0, '_');
		}

		String[] astring = GuiMapList.portNames;
		i = astring.length;

		for (int j = 0; j < i; ++j) {
			String s0 = astring[j];
			if (this.folderString.equalsIgnoreCase(s0)) {
				this.folderString = "_" + this.folderString + "_";
			}
		}

		while (saveLoader.getWorldInfo(this.folderString) != null) {
			this.folderString = this.folderString + "-";
		}
	}

	@Override
	public void drawScreen(int x, int y, float f) {
		mapList.drawScreen(x, y, f);
		nameField.drawTextBox();
		this.drawCenteredString(this.fontRenderer, I18n.format("gui.hardcoremapreset.create_title"), this.width / 2, 20, -1);
		this.drawString(this.fontRenderer, I18n.format("selectWorld.enterName"), this.width / 2 - 100, 35, -6250336);
		this.drawString(this.fontRenderer, I18n.format("selectWorld.resultFolder") + " " + this.folderString, this.width / 2 - 100, 68, -6250336);
		super.drawScreen(x, y, f);
	}

	private class MapList extends GuiSlot {
		public MapList() {
			super(GuiMapList.this.mc, GuiMapList.this.width, GuiMapList.this.height, 78, GuiMapList.this.height - 32, 36);
		}

		@Override
		protected int getSize() {
			return GuiMapList.this.saveList.size();
		}

		protected void elementClicked(int slot, boolean doubleClicked, int mouseX, int mouseY) {
			GuiMapList.this.selectedSlot = slot;
			GuiMapList.this.createButton.enabled = true;

			if (doubleClicked) {
				GuiMapList.this.createMap();
			}
		}

		@Override
		protected boolean isSelected(int slot) {
			return (slot == GuiMapList.this.selectedSlot);
		}

		@Override
		protected void drawBackground() {
			GuiMapList.this.drawDefaultBackground();
		}

		@Override
		public int getListWidth() {
			return GuiMapList.this.width - 40;
		}

		@Override
		protected int getScrollBarX() {
			return (this.width / 2) + (this.getListWidth() / 2) - 6;
		}

		@Override
		public void drawSlot(int slot, int x, int y, int slotHeight, int mouseX, int mouseY, float f) {
			WorldSummary saveFormat = (WorldSummary) GuiMapList.this.saveList.get(slot);

			String displayName = saveFormat.getDisplayName();
			String author = saveLoader.authorList.get(saveFormat);
			String by = I18n.format("gui.hardcoremapreset.by");
			String topLine = displayName + ", " + TextFormatting.ITALIC + TextFormatting.BOLD + by + TextFormatting.RESET + ": " + author;

			String folder = saveFormat.getFileName();
			String date = GuiMapList.this.dateFormat.format(new Date(saveFormat.getLastTimePlayed()));
			String middleLine = folder + " (" + date + ")";

			String mode = WordUtils.capitalize(saveFormat.getEnumGameType().getName());
			String cheats = saveFormat.getCheatsEnabled() ? I18n.format("selectWorld.cheats") : "";
			String bottomLine = mode + ", " + cheats;

			GuiMapList.this.drawString(GuiMapList.this.fontRenderer, topLine, x + 34, y + 1, 16777215);
			GuiMapList.this.drawString(GuiMapList.this.fontRenderer, middleLine, x + 34, y + 12, 8421504);
			GuiMapList.this.drawString(GuiMapList.this.fontRenderer, bottomLine, x + 34, y + 12 + 10, 8421504);

			if(saveLoader.imageList.containsKey(saveFormat)){
				ResourceLocation texture = saveLoader.imageList.get(saveFormat);
				Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
				GlStateManager.color(1F, 1F, 1F, 1F);
				GlStateManager.enableBlend();
				Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
				GlStateManager.disableBlend();
			}
		}
	}

	private WorldSummary getSave() {
		return GuiMapList.this.saveList.get(this.selectedSlot);
	}

	private void createMap() {
		if (this.selectedSlot == -1) {
			return;
		}
		ResetMaps.copymap(getSave().getFileName(), this.folderString);
		Minecraft.getMinecraft().getSaveLoader().renameWorld(this.folderString, this.nameField.getText().trim());
	}

}
