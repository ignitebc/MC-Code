package com.tacz.guns.resource.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;


public class AttachmentDataManager extends CommonDataManager<AttachmentData> {

    public AttachmentDataManager() {
        super(DataType.ATTACHMENT_DATA, AttachmentData.class, CommonAssetsManager.GSON, "data/attachments", "AttachmentDataLoader");
    }

    @Override
    protected AttachmentData parseJson(JsonElement element) {
        AttachmentData data = getGson().fromJson(element, getDataClass());
        if (data != null) {
            // 등록된 부착물 속성 변경을 직렬화한다
            AttachmentPropertyManager.getModifiers().forEach((key, value) -> {
                String json = getGson().toJson(element);
                if (!element.isJsonObject()) {
                    return;
                }
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has(key)) {
                    JsonProperty<?> property = value.readJson(json);
                    property.initComponents();
                    data.addModifier(key, property);
                } else if (jsonObject.has(value.getOptionalFields())) {
                    // 예전 버전과 호환하려고 선택 필드 이름을 읽는다
                    JsonProperty<?> property = value.readJson(json);
                    property.initComponents();
                    data.addModifier(key, property);
                }
            });
        }
        return data;
    }
}
