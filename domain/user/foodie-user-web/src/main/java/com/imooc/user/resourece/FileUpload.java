package com.imooc.user.resourece;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @描述: 和资源文件--上传文件的路径资源文件
 * @Author
 */
@Component
@ConfigurationProperties(prefix = "file")  /** 属性文件的前缀 */
@PropertySource("classpath:file-upload-prod.properties") /** 扫描属性文件 */
public class FileUpload {

    private String imageUserFaceLocation;
    private String imageServerUrl;

    public String getImageServerUrl() {
        return imageServerUrl;
    }

    public void setImageServerUrl(String imageServerUrl) {
        this.imageServerUrl = imageServerUrl;
    }

    public String getImageUserFaceLocation() {
        return imageUserFaceLocation;
    }

    public void setImageUserFaceLocation(String imageUserFaceLocation) {
        this.imageUserFaceLocation = imageUserFaceLocation;
    }
}
