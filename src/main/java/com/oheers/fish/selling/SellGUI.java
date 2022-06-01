package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SellGUI implements InventoryHolder {

    private final Player player;
    private final Inventory menu;

    public boolean modified;

    public double value;

    public boolean error;

    public int fishCount;

    public int guiSize;

    private ItemStack sellIcon, sellAllIcon, filler, errorFiller, confirmIcon, confirmSellAllIcon, noValueIcon, sellAllErrorIcon;

    public SellGUI(Player p) {
        this.guiSize = (EvenMoreFish.mainConfig.getGUISize()+1)*9;
        this.player = p;
        this.modified = false;
        this.menu = Bukkit.createInventory(this, guiSize, new Message(ConfigMessage.WORTH_GUI_NAME).getRawMessage(true, true));
        setFiller();
        addFiller(filler);
        setSellItem();
        setSellAllItem();
        this.player.openInventory(menu);
    }

    public Player getPlayer() {
        return player;
    }

    public void setFiller() {
        // the gray glass panes at the bottom
        ItemStack fill = createItem(EvenMoreFish.mainConfig.getFiller());
        ItemStack error = createItem(EvenMoreFish.mainConfig.getFillerError());
        ItemMeta fillMeta = fill.getItemMeta();
        ItemMeta errMeta = error.getItemMeta();
        fillMeta.setDisplayName(ChatColor.RESET + "");
        errMeta.setDisplayName(ChatColor.RESET + "");
        fill.setItemMeta(fillMeta);
        error.setItemMeta(errMeta);

        // sets it as a default menu item that won't be dropped in a .close() request
        this.filler = WorthNBT.attributeDefault(fill);
        this.errorFiller = WorthNBT.attributeDefault(error);
    }

    public void addFiller(ItemStack fill) {
        for (int i = guiSize-9; i < guiSize; i++) {
            ItemStack item = menu.getItem(i);
            if (item == null || item.isSimilar(filler) || item.isSimilar(errorFiller)) {
                menu.setItem(i, fill);
            }
        }
    }

    public void setSellItem() {
        ItemStack sIcon = createItem(EvenMoreFish.mainConfig.getSellItem());
        ItemMeta sellMeta = sIcon.getItemMeta();
        sellMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_BUTTON_NAME).getRawMessage(true, false));

        // Generates the lore, looping through each line in messages.yml lore thingy, and generating it
        Message message = new Message(ConfigMessage.WORTH_GUI_SELL_LORE);
        message.setSellPrice(getTotalWorth(false));

        sellMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true, true).split("\n"))));

        sIcon.setItemMeta(sellMeta);
        glowify(sIcon);

        this.sellIcon = WorthNBT.attributeDefault(sIcon);
        menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellSlot()), this.sellIcon);
    }

    public void setSellAllItem() {
        ItemStack saIcon = createItem(EvenMoreFish.mainConfig.getSellAllMaterial());
        ItemMeta saMeta = saIcon.getItemMeta();
        saMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_NAME).getRawMessage(true, false));

        Message message = new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_LORE);
        message.setSellPrice(getTotalWorth(true));
        saMeta.setLore(Arrays.asList(message.getRawMessage(true, true).split("\n")));

        saIcon.setItemMeta(saMeta);
        glowify(saIcon);

        this.sellAllIcon = WorthNBT.attributeDefault(saIcon);
        menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellAllSlot()), this.sellAllIcon);
    }

    public void updateSellItem() {
        ItemMeta sellMeta = this.sellIcon.getItemMeta();
        sellMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_BUTTON_NAME).getRawMessage(true, true));

        // Generates the lore, looping through each line in messages.yml lore thingy, and generating it
        Message message = new Message(ConfigMessage.WORTH_GUI_SELL_LORE);
        message.setSellPrice(getTotalWorth(false));

        sellMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true, true).split("\n"))));

        this.sellIcon.setItemMeta(sellMeta);
        menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellSlot()), this.sellIcon);
    }

    public void updateSellAllItem() {
        ItemMeta saMeta = this.sellAllIcon.getItemMeta();
        saMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_NAME).getRawMessage(true, false));

        // Generates the lore, looping through each line in messages.yml lore thingy, and generating it
        Message message = new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_LORE);
        message.setSellPrice(getTotalWorth(true));
        saMeta.setLore(Arrays.asList(message.getRawMessage(true, true).split("\n")));

        this.sellAllIcon.setItemMeta(saMeta);
        menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellAllSlot()), this.sellAllIcon);
    }

    public ItemStack getSellIcon() {
        return this.sellIcon;
    }

    public ItemStack getSellAllIcon() {
        return this.sellAllIcon;
    }

    public ItemStack getConfirmIcon() {
        return this.confirmIcon;
    }

    public ItemStack getConfirmSellAllIcon() {
        return this.confirmSellAllIcon;
    }

    public ItemStack getErrorIcon() {
        return this.noValueIcon;
    }

    public ItemStack getSellAllErrorIcon() {
        return this.sellAllErrorIcon;
    }

    public void createIcon(boolean sellAll) {
        String totalWorth = getTotalWorth(sellAll);
        if (totalWorth.equals("0.0")) {

            ItemStack error;
            if (sellAll) error = createItem(EvenMoreFish.mainConfig.getSellAllErrorMaterial());
            else error = createItem(EvenMoreFish.mainConfig.getSellItemError());

            ItemMeta errorMeta = error.getItemMeta();

            if (sellAll) errorMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_NO_VAL_ALL_BUTTON_NAME).getRawMessage(true, false));
            else errorMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_NO_VAL_BUTTON_NAME).getRawMessage(true, false));

            if (sellAll) {
                errorMeta.setLore(new ArrayList<>(Arrays.asList(new Message(ConfigMessage.WORTH_GUI_SELL_BUTTON_LORE).getRawMessage(true, false).split("\n"))));
            } else {
                errorMeta.setLore(new ArrayList<>(Arrays.asList(new Message(ConfigMessage.WORTH_GUI_NO_VAL_BUTTON_LORE).getRawMessage(true, false).split("\n"))));
            }

            error.setItemMeta(errorMeta);
            glowify(error);
            if (sellAll) this.sellAllErrorIcon = WorthNBT.attributeDefault(error);
            else this.noValueIcon = WorthNBT.attributeDefault(error);
            this.error = true;
        } else {

            ItemStack confirm;
            if (sellAll) confirm = createItem(EvenMoreFish.mainConfig.getSellAllConfirmMaterial());
            else confirm = createItem(EvenMoreFish.mainConfig.getSellItemConfirm());

            ItemMeta cMeta = confirm.getItemMeta();
            if (sellAll) cMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_CONFIRM_ALL_BUTTON_NAME).getRawMessage(true, false));
            else cMeta.setDisplayName(new Message(ConfigMessage.WORTH_GUI_CONFIRM_BUTTON_NAME).getRawMessage(true, false));
            // Generates the lore, looping through each line in messages.yml lore thingy, and generating it
            List<String> lore = new ArrayList<>();

            if (sellAll) {
                Message message = new Message(ConfigMessage.WORTH_GUI_SELL_ALL_BUTTON_LORE);
                message.setSellPrice(getTotalWorth(true));
                cMeta.setLore(Arrays.asList(message.getRawMessage(true, true).split("\n")));
            } else {
                Message message = new Message(ConfigMessage.WORTH_GUI_SELL_LORE);
                message.setSellPrice(getTotalWorth(false));

                cMeta.setLore(new ArrayList<>(Arrays.asList(message.getRawMessage(true, true).split("\n"))));
            }

            confirm.setItemMeta(cMeta);
            glowify(confirm);

            if (sellAll) this.confirmSellAllIcon = WorthNBT.attributeDefault(confirm);
            else this.confirmIcon = WorthNBT.attributeDefault(confirm);

            this.error = false;
        }
    }

    public void setIcon(boolean sellAll) {
        if (this.error) {
            if (sellAll) {
                this.menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellAllSlot()), this.sellAllErrorIcon);
            } else {
                this.menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellSlot()), this.noValueIcon);
            }

            this.addFiller(errorFiller);
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 0.0f);
        } else {
            if (sellAll) {
                this.menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellAllSlot()), this.confirmSellAllIcon);
            } else {
                this.menu.setItem(guiSize - (10 - EvenMoreFish.mainConfig.getSellSlot()), this.confirmIcon);
            }

            this.addFiller(filler);
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.75f);
        }

    }

    public String getTotalWorth(boolean inventory) {
        if (this.menu == null) return Double.toString(0.0d);

        double val = 0.0d;
        int count = 0;

        if (inventory) {
            for (ItemStack is : player.getInventory().getStorageContents()) {
                // -1.0 is given when there's no worth NBT value
                if (WorthNBT.getValue(is) != -1.0) {
                    val += (WorthNBT.getValue(is) * is.getAmount());
                    count += is.getAmount();
                }
            }
        } else {
            for (ItemStack is : this.menu.getContents()) {
                // -1.0 is given when there's no worth NBT value
                if (WorthNBT.getValue(is) != -1.0) {
                    val += (WorthNBT.getValue(is) * is.getAmount());
                    count += is.getAmount();
                }
            }
        }

        this.value = val;
        this.fishCount = count;

        return Double.toString(val);
    }

    // will drop only non-fish items if the method is called from selling, and everything if it's just a gui close
    public void close() {
        player.closeInventory();
    }

    // for each item in the menu, if it isn't a default menu item, it's dropped at the player's feet
    public void doRescue() {
        List<ItemStack> throwing = new ArrayList<>();
        for (ItemStack i : this.menu.getContents()) {
            if (i != null) {
                if (!WorthNBT.isDefault(i)) {
                    throwing.add(i);
                }
            }
        }
        FishUtils.giveItems(throwing, this.player);
    }

    public ItemStack getFiller() {
        return this.filler;
    }

    public ItemStack getErrorFiller() {
        return this.errorFiller;
    }

    public void setModified(boolean mod) {
        this.modified = mod;
    }

    public boolean getModified() {
        return this.modified;
    }

    private void glowify(ItemStack i) {

        // plops on the unbreaking 1 enchantment to make it glow
        i.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta meta = i.getItemMeta();

        // hides the unbreaking 1 enchantment from showing in the lore
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(meta);
    }

    public boolean sell(boolean sellAll) {
        getTotalWorth(sellAll);
        EvenMoreFish.econ.depositPlayer(this.player, value);

        // sending the sell message to the player
        Message message = new Message(ConfigMessage.FISH_SALE);
        message.setSellPrice(Double.toString(value));
        message.setAmount(Integer.toString(fishCount));
        message.setPlayer(this.player.toString());
        message.broadcast(player, true, true);

        this.player.playSound(this.player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);

        if (sellAll) {
            for (ItemStack item : this.player.getInventory()) {
                if (FishUtils.isFish(item)) this.player.getInventory().remove(item);
            }
        } else {
            // Remove sold items
            for (int i = 0; i < guiSize - 9; i++) {
                ItemStack item = menu.getItem(i);
                if (WorthNBT.getValue(item) != -1.0) {
                    menu.setItem(i, null);
                }
            }
        }

        return this.value != 0.0;
    }

    protected ItemStack createItem(String material) {
        String[] names = material.split(":");

        if (names.length < 2) {
            return new ItemStack(Material.valueOf(names[0]));
        }

        String name = names[0];
        int customData = Integer.parseInt(names[1]);

        ItemStack item = new ItemStack(Material.valueOf(name));
        ItemMeta meta =  item.getItemMeta();

        meta.setCustomModelData(customData);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public Inventory getInventory() {
        return menu;
    }
}
