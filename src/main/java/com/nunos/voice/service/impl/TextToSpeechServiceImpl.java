package com.nunos.voice.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.BaseService;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import com.ibm.watson.text_to_speech.v1.util.WaveUtils;
import com.ibm.watson.text_to_speech.v1.websocket.BaseSynthesizeCallback;
import com.nunos.voice.model.BadRequestResponse;
import com.nunos.voice.service.TextToSpeechService;
import com.nunos.voice.uitl.BaseUtil;

@Service("TextToSpeechService")
public class TextToSpeechServiceImpl extends BaseService implements TextToSpeechService {

	@Autowired
	BaseUtil baseUtil;

	@Value("${nunos.watson.tts.apikey}")
	private String apikey;

	@Value("${nunos.watson.tts.url}")
	private String url;

	@Value("${nunos.watson.voice.request.format}")
	private String requestFormat;

	@Value("${nunos.watson.voice.response.format}")
	private String responseFormat;

	static Logger log = LogManager.getLogger();

	@Override
	public ResponseEntity<?> ttsHtml(String text) {
		if (text == null || text.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BadRequestResponse("Text param cannot be null."));
		}
		
		log.debug("Invoking tts-HTML method");
		long init = System.currentTimeMillis();

		Authenticator ttsAuthenticator = new IamAuthenticator(apikey);
		TextToSpeech synthesizer = new TextToSpeech(ttsAuthenticator);

		SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder().text(text)
				.voice(SynthesizeOptions.Voice.PT_BR_ISABELAV3VOICE).accept(requestFormat).build();

		InputStream in = synthesizer.synthesize(synthesizeOptions).execute().getResult();

		String filename = null;
		HttpStatus httpStatus = null;
		
		try {
			filename = baseUtil.getDefaultOutputDirectory() + baseUtil.generateFileName() + responseFormat;
			OutputStream out = new FileOutputStream(new File(filename));
			byte[] buf = new byte[1024];
			int len;
			while ((len = WaveUtils.reWriteWaveHeader(in).read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
			httpStatus = HttpStatus.CREATED;
		} catch (IOException e) {
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		long end = System.currentTimeMillis();
		log.debug("Processing time: " + (end - init) + " milliseconds");
		log.debug("Closing tts-HTML method");

		return ResponseEntity.status(httpStatus).body(filename);
	}

	@Override
	public ResponseEntity<?> ttsSocket(String text) {
		if (text == null || text.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BadRequestResponse("Text param cannot be null."));
		}

		log.debug("Invoking TTS-Socket method");
		long init = System.currentTimeMillis();

		Authenticator authenticator = new IamAuthenticator(apikey);
		TextToSpeech service = new TextToSpeech(authenticator);

		SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder().text(text).accept(requestFormat)
				.voice(SynthesizeOptions.Voice.PT_BR_ISABELAV3VOICE).build();

		String filename = null;

		HttpStatus httpStatus = HttpStatus.CREATED;

		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			service.synthesizeUsingWebSocket(synthesizeOptions, new BaseSynthesizeCallback() {
				@Override
				public void onAudioStream(byte[] bytes) {
					try {
						byteArrayOutputStream.write(bytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			Thread.sleep(10000);

			filename = baseUtil.getDefaultOutputDirectory() + baseUtil.generateFileName() + responseFormat;
			OutputStream fileOutputStream = new FileOutputStream(filename);
			byteArrayOutputStream.writeTo(fileOutputStream);

			byteArrayOutputStream.close();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		log.debug("Processing time: " + (end - init) + " milliseconds");
		log.debug("Closing TTS-Socket method");

		return ResponseEntity.status(httpStatus).body(filename);
	}
}
