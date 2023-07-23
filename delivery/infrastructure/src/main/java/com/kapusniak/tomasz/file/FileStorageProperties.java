package com.kapusniak.tomasz.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    @JsonProperty("upload-dir")
    private String uploadDir;

}