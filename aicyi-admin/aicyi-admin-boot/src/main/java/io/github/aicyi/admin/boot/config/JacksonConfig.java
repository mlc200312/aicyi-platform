package io.github.aicyi.admin.boot.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.aicyi.common.model.type.BooleanType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 配置：为脚手架 BooleanType 注册序列化器，
 * 序列化为 code（0/1）而非枚举名（TRUE/FALSE），保持与前端约定一致。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer booleanTypeSerializerCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(BooleanType.class, new JsonSerializer<>() {
                @Override
                public void serialize(BooleanType value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException {
                    if (value == null) {
                        gen.writeNull();
                    } else {
                        gen.writeNumber(value.getCode());
                    }
                }
            });
            builder.modulesToInstall(module);
        };
    }
}
