package com.yupi.yuaicodemother.manager;

import com.qcloud.cos.model.PutObjectResult;
import com.yupi.yuaicodemother.config.CosClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

class CosManagerTest {

    @Test
    void uploadFile_shouldJoinHostAndKeyWithSingleSlash() throws Exception {
        TestableCosManager cosManager = new TestableCosManager();
        CosClientConfig config = new CosClientConfig();
        config.setHost("https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com");
        config.setBucket("econgencode-1379443208");
        injectField(cosManager, "cosClientConfig", config);

        String url = cosManager.uploadFile("screenshots/2026/03/22/cover.jpg", new File("cover.jpg"));

        Assertions.assertEquals(
                "https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com/screenshots/2026/03/22/cover.jpg",
                url
        );
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = CosManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class TestableCosManager extends CosManager {
        @Override
        public PutObjectResult putObject(String key, File file) {
            return new PutObjectResult();
        }
    }
}
