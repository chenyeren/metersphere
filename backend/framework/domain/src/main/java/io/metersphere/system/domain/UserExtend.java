package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class UserExtend implements Serializable {
    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_extend.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{user_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "UI本地调试地址")
    private String seleniumServer;

    @Schema(title = "其他平台对接信息")
    private byte[] platformInfo;

    private static final long serialVersionUID = 1L;
}