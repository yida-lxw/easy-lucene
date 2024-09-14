package com.yida.lucene.spring.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * @author yida
 * @package com.hydangan.common.utils
 * @date 2023-12-06 15:36
 * @description 资源文件加载工具类
 */
public class ResourceFileUtils {
	public static final Logger log = LoggerFactory.getLogger(ResourceFileUtils.class);

	public static String readResourceFile(String filePath) {
		File file = null;
		try {
			file = ResourceUtils.getFile("classpath:" + filePath);
			if (null == file) {
				log.info("kick off to read the resource file:[{}] in jar.", filePath);
				return readResourceFileInJar(filePath);
			}
			return new String(Files.readAllBytes(file.toPath()));
		} catch (Exception e) {
			log.error("Load resource file:[{}] from the classpath with the spring ResourceUtils occur exception:[{}].",
					filePath, e.getMessage());
			return readResourceFileInJar(filePath);
		}

	}

	public static String readResourceFileInJar(String filePath) {
		ClassLoader classLoader = BeanScanUtils.class.getClassLoader();
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			inputStream = classLoader.getResourceAsStream(filePath);
			if (null == inputStream) {
				inputStream = classLoader.getResourceAsStream("/" + filePath);
				if (null == inputStream) {
					inputStream = BeanScanUtils.class.getResourceAsStream(filePath);
					if (null == inputStream) {
						inputStream = BeanScanUtils.class.getResourceAsStream("/" + filePath);
					}
				}
			}
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
		} catch (Exception e) {
			log.error("Read resource file:[{}] in jar occur exception.", filePath);
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (Exception e) {
				}
			}
			if (null != bufferedReader) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
				}
			}
			return stringBuilder.toString();
		}
	}

	public static InputStream loadResourceFileAsInputStreamInJar(String filePath) {
		ClassLoader classLoader = BeanScanUtils.class.getClassLoader();
		InputStream inputStream = null;
		try {
			inputStream = classLoader.getResourceAsStream(filePath);
			if (null == inputStream) {
				inputStream = classLoader.getResourceAsStream("/" + filePath);
				if (null == inputStream) {
					inputStream = BeanScanUtils.class.getResourceAsStream(filePath);
					if (null == inputStream) {
						inputStream = BeanScanUtils.class.getResourceAsStream("/" + filePath);
					}
				}
			}
		} catch (Exception e) {
			log.error("Load resource file:[{}] in jar as InputStream occur exception.", filePath);
		} finally {
			return inputStream;
		}
	}
}
