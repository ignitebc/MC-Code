package com.daqem.itemrestrictions.data;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.daqem.itemrestrictions.ItemRestrictions;
import com.google.gson.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemRestriction {

    private final ResourceLocation location;
    private final ItemStack icon;
    private final List<RestrictionType> restrictionTypes;
    private final List<ICondition> conditions;

    public ItemRestriction(ResourceLocation location, ItemStack icon, List<RestrictionType> restrictionTypes, List<ICondition> conditions) {
        this.location = location;
        this.icon = icon;
        this.restrictionTypes = restrictionTypes;
        this.conditions = conditions;
    }

    public RestrictionResult isRestricted(ActionData actionData) {
        ItemStack itemStack = actionData.getData(ActionDataType.ITEM_STACK);

        if (itemStack == null) {
            return new RestrictionResult();
        }

        boolean allConditionsMet = this.conditions.stream()
                .allMatch(condition ->
                        (condition.isMet(actionData) && !condition.isInverted()) ||
                                (!condition.isMet(actionData) && condition.isInverted())
                );

        if (allConditionsMet) {
            return new RestrictionResult(this.restrictionTypes);
        } else {
            return new RestrictionResult();
        }
    }

    public static class Serializer implements JsonDeserializer<ItemRestriction>, ArcSerializer {

        @Override
        public ItemRestriction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ResourceLocation location = getResourceLocation(jsonObject, "location");
            JsonArray restrictionTypesArray = GsonHelper.getAsJsonArray(jsonObject, "types");
            JsonArray conditionsArray = GsonHelper.getAsJsonArray(jsonObject, "conditions");

            List<RestrictionType> restrictionTypes = new ArrayList<>();
            List<ICondition> conditions = new ArrayList<>();

            ItemStack iconStack = ItemStack.EMPTY;
            if (jsonObject.has("icon")) {
                iconStack = getItemStack(jsonObject.getAsJsonObject("icon"));
            }

            restrictionTypesArray.forEach(jsonElement -> {
                String restrictionTypeString = jsonElement.getAsString();
                try {
                    RestrictionType restrictionType = RestrictionType.valueOf(restrictionTypeString.toUpperCase());
                    restrictionTypes.add(restrictionType);
                } catch (IllegalArgumentException e) {
                    ItemRestrictions.LOGGER.error("Could not deserialize restriction type {} because: {}", restrictionTypeString, e.getMessage());
                }
            });

            conditionsArray.forEach(jsonElement -> {
                ResourceLocation conditionTypeLocation = ResourceLocation.parse(GsonHelper.getAsString(jsonElement.getAsJsonObject(), "type"));
                ArcRegistry.CONDITION.getOptional(conditionTypeLocation).ifPresent(conditionType -> {
                    conditions.add(conditionType.getSerializer().fromJson(ResourceLocation.parse(""), jsonElement.getAsJsonObject()));
                });
            });

            return new ItemRestriction(location, iconStack, restrictionTypes, conditions);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, ItemRestriction itemRestriction) {
            buf.writeResourceLocation(itemRestriction.location);
            ItemStack.STREAM_CODEC.encode(buf, itemRestriction.icon);
            buf.writeCollection(itemRestriction.restrictionTypes, (byteBuf, restrictionType) -> byteBuf.writeUtf(restrictionType.name()));
            buf.writeCollection(itemRestriction.conditions, (byteBuf, condition) -> IConditionSerializer.toNetwork(condition, (RegistryFriendlyByteBuf) byteBuf, itemRestriction.getLocation()));
        }

        public static ItemRestriction fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation location = buf.readResourceLocation();
            ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
            List<String> restrictionTypeStrings = buf.readList(FriendlyByteBuf::readUtf);
            List<RestrictionType> restrictionTypes = new ArrayList<>();
            restrictionTypeStrings.forEach(restrictionTypeString -> {
                try {
                    RestrictionType restrictionType = RestrictionType.valueOf(restrictionTypeString.toUpperCase());
                    restrictionTypes.add(restrictionType);
                } catch (IllegalArgumentException e) {
                    ItemRestrictions.LOGGER.error("Could not deserialize restriction type {} because: {}", restrictionTypeString, e.getMessage());
                }
            });
            List<ICondition> conditions = buf.readList(object -> IConditionSerializer.fromNetwork((RegistryFriendlyByteBuf) object));
            return new ItemRestriction(location, icon, restrictionTypes, conditions);
        }
    }

    @SuppressWarnings("unused")
    public ResourceLocation getLocation() {
        return location;
    }

    @SuppressWarnings("unused")
    public ItemStack getIcon() {
        return icon;
    }

    @SuppressWarnings("unused")
    public List<ICondition> getConditions() {
        return conditions;
    }

    @SuppressWarnings("unused")
    public List<RestrictionType> getRestrictionTypes() {
        return restrictionTypes;
    }
}
