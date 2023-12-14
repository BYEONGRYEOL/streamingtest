package com.example.streamingtest;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathUtils {
	public static Path getOrCreateSaveFilePath(String directory, String key) {
		Path filePath = FileSystems.getDefault().getPath(directory + "/" + key);
		try {
			Files.createDirectories(filePath.getParent());
		} catch (IOException e) {
			log.info(" 부모 디렉토리 생성 실패");
			throw new RuntimeException(e);
		}
		return filePath;
	}
}
