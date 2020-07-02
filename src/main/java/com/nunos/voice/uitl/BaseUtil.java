package com.nunos.voice.uitl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaseUtil {
	@Value("${nunos.watson.voice.default.directory.input}")
	private String input;

	@Value("${nunos.watson.voice.default.directory.output}")
	private String output;

	public String generateFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		return sdf.format(new Date()).toString();
	}

	public String getDefaultOutputDirectory() {
		return System.getProperty("user.dir") + this.output;
	}

	public String getDefaultInputDirectory() {
		return System.getProperty("user.dir") + this.input;
	}
}
