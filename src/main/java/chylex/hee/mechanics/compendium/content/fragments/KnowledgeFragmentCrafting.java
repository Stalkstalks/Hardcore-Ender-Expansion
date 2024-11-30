package chylex.hee.mechanics.compendium.content.fragments;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import chylex.hee.gui.GuiEnderCompendium;
import chylex.hee.gui.helpers.GuiItemRenderHelper;
import chylex.hee.init.ItemList;
import chylex.hee.item.ItemSpecialEffects;
import chylex.hee.mechanics.compendium.content.KnowledgeFragment;
import chylex.hee.mechanics.compendium.content.KnowledgeObject;
import chylex.hee.mechanics.compendium.util.KnowledgeUtils;
import chylex.hee.system.J8;
import chylex.hee.system.logging.Log;
import chylex.hee.system.util.RecipeUnifier;
import chylex.hee.system.util.RecipeUnifier.Recipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class KnowledgeFragmentCrafting extends KnowledgeFragment {

    public static final ItemStack lockedItem = new ItemStack(
            ItemList.special_effects,
            1,
            ItemSpecialEffects.questionMark);

    private static Recipe findRecipe(ItemStack outputToFind, @Nullable final ItemStack[] matchIngredients) {
        List<IRecipe> list = CraftingManager.getInstance().getRecipeList();

        for (int a = list.size() - 1; a >= 0; a--) {
            IRecipe recipe = list.get(a);

            if (ItemStack.areItemStacksEqual(outputToFind, recipe.getRecipeOutput())) {
                Recipe unified = new RecipeUnifier().unify(recipe);
                final ItemStack[] ingredients = unified.getIngredientArray();

                if (matchIngredients == null) return unified;
                else if (matchIngredients.length == ingredients.length
                        && J8.allMatch(ingredients.length, new Predicate<Integer>() {

                            @Override
                            public boolean apply(Integer index) {
                                return ingredients[index].getItem() == matchIngredients[index].getItem()
                                        && ingredients[index].getItemDamage()
                                                == matchIngredients[index].getItemDamage();
                            }
                        }))
                    return unified;
            }
        }

        return null;
    }

    private enum Status {
        UNVERIFIED,
        FINE,
        CHANGED,
        REMOVED
    }

    private ItemStack findOutput;
    private ItemStack[] findIngredients;

    private ItemStack[] ingredients;
    private ItemStack output;
    private Status status;

    public KnowledgeFragmentCrafting(int globalID) {
        super(globalID);
    }

    public KnowledgeFragmentCrafting setRecipe(ItemStack outputToFind) {
        return setRecipe(outputToFind, null);
    }

    public KnowledgeFragmentCrafting setRecipe(ItemStack outputToFind, @Nullable ItemStack[] matchIngredients) {
        this.findOutput = outputToFind;
        this.findIngredients = matchIngredients;

        Recipe recipe = findRecipe(outputToFind, matchIngredients);

        if (recipe != null) {
            this.ingredients = recipe.getIngredientArray();
            this.output = recipe.getOutput();
            this.status = Status.UNVERIFIED;
        } else Log.warn("Could not find ItemStack $0 when registering recipe from registry.", outputToFind.toString());

        return this;
    }

    private void verifyRecipe() {
        if (status != Status.UNVERIFIED) return;

        final Recipe recipe = findRecipe(findOutput, findIngredients);

        if (recipe == null) {
            this.status = Status.REMOVED;
            this.output = null;
            this.ingredients = null;
            return;
        } else if (ItemStack.areItemStacksEqual(recipe.getOutput(), output) && ingredients != null
                && ingredients.length == recipe.getIngredientArray().length
                && J8.allMatch(ingredients.length, new Predicate<Integer>() {

                    @Override
                    public boolean apply(Integer index) {
                        return ItemStack.areItemStacksEqual(ingredients[index], recipe.getIngredientArray()[index]);
                    }
                })) {
                    status = Status.FINE;
                    return;
                } else {
                    this.ingredients = recipe.getIngredientArray();
                    this.output = recipe.getOutput();
                    this.status = Status.CHANGED;
                }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getHeight(GuiEnderCompendium gui, boolean isUnlocked) {
        return 58;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean onClick(GuiEnderCompendium gui, int x, int y, int mouseX, int mouseY, int buttonId,
            boolean isUnlocked) {
        if (ingredients == null || output == null || !isUnlocked || buttonId != 0) return false;

        KnowledgeObject<?> obj = null;

        for (int a = 0, cnt = 0, xx = x, yy = y; a < ingredients.length; a++, xx += 19) {
            if (ingredients[a] != null && checkRect(mouseX, mouseY, xx + 1, yy + 1, 17, 17)) {
                obj = KnowledgeUtils.tryGetFromItemStack(ingredients[a]);
                break;
            }

            if (++cnt >= 3) {
                yy += 19;
                xx -= 19 * 3;
                cnt = 0;
            }
        }

        if (obj == null && checkRect(mouseX, mouseY, x + 95, y + 20, 17, 17)) {
            obj = KnowledgeUtils.tryGetFromItemStack(output);
        }

        if (obj != null) {
            gui.showObject(obj);
            gui.moveToCurrentObject(true);
            return true;
        } else return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRender(GuiEnderCompendium gui, int x, int y, int mouseX, int mouseY, boolean isUnlocked) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        gui.mc.getTextureManager().bindTexture(GuiEnderCompendium.texFragments);
        gui.drawTexturedModalRect(x, y, 0, 0, 88, 58);

        verifyRecipe();

        if (status != Status.FINE) {
            gui.mc.fontRenderer.drawString("?", x + 107, y + 8, 255 << 24);

            if (checkRect(mouseX, mouseY, x + 106, y + 7, 6, 9)) {
                GuiItemRenderHelper.setupTooltip(
                        mouseX,
                        mouseY,
                        I18n.format(
                                status == Status.CHANGED ? "compendium.crafting.changed"
                                        : "compendium.crafting.removed"));
            }
        }

        if (ingredients == null || output == null) return;

        ItemStack is;

        for (int a = 0, cnt = 0, xx = x, yy = y; a < (isUnlocked ? ingredients.length : 9); a++, xx += 19) {
            if ((is = isUnlocked ? ingredients[a] : lockedItem) != null) {
                GuiItemRenderHelper.renderItemIntoGUI(gui.mc.getTextureManager(), is, xx + 2, yy + 2);

                if (isUnlocked && checkRect(mouseX, mouseY, xx + 1, yy + 1, 17, 17)) {
                    GuiItemRenderHelper.setupTooltip(
                            mouseX,
                            mouseY,
                            Joiner.on('\n').join(KnowledgeUtils.getCompendiumTooltip(is, gui.mc.thePlayer)));
                }
            }

            if (++cnt >= 3) {
                yy += 19;
                xx -= 19 * 3;
                cnt = 0;
            }
        }

        GuiItemRenderHelper.renderItemIntoGUI(gui.mc.getTextureManager(), output, x + 96, y + 21);

        if (isUnlocked && checkRect(mouseX, mouseY, x + 95, y + 20, 17, 17)) {
            GuiItemRenderHelper.setupTooltip(
                    mouseX,
                    mouseY,
                    Joiner.on('\n').join(KnowledgeUtils.getCompendiumTooltip(output, gui.mc.thePlayer)));
        }
    }

    private static final boolean checkRect(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseY >= y && mouseX <= x + w && mouseY <= y + h;
    }
}
