package com.nunos.voice.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.nunos.voice.model.BadRequestResponse;
import com.nunos.voice.service.SpeechToTextService;
import com.nunos.voice.uitl.BaseUtil;

@Service("SpeechToTextService")
public class SpeechToTextServiceImpl implements SpeechToTextService {

	@Autowired
	BaseUtil baseUtil;

	@Value("${nunos.watson.stt.apikey}")
	private String apikey;

	@Value("${nunos.watson.stt.url}")
	private String url;

	@Value("${nunos.watson.voice.request.format}")
	private String requestFormat;

	@Value("${nunos.watson.voice.response.format}")
	private String responseFormat;

	static Logger log = LogManager.getLogger();

	@Override
	public ResponseEntity<?> sttHtml(String audio) {
		if (audio == null || audio.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BadRequestResponse("Audio param cannot be null."));
		}
		
		log.debug("Invoking STT-HTML method");
		long init = System.currentTimeMillis();

		Authenticator t2sAuthenticator = new IamAuthenticator(apikey);
		SpeechToText recognizer = new SpeechToText(t2sAuthenticator);

		File audioFile = new File(baseUtil.getDefaultInputDirectory() + audio);
		
		SpeechRecognitionResults transcript = null;
		HttpStatus httpStatus = null;

		try {
			RecognizeOptions recognizeOptions = new RecognizeOptions.Builder().audio(audioFile)
					.model(RecognizeOptions.Model.PT_BR_BROADBANDMODEL).contentType(requestFormat).build();

			transcript = recognizer.recognize(recognizeOptions).execute().getResult();
			httpStatus = HttpStatus.OK;
		} catch (Exception e) {
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		long end = System.currentTimeMillis();
		log.debug("Processing time: " + (end - init) + " milliseconds");
		log.debug("Closing STT-HTML method");

		return ResponseEntity.status(httpStatus).body(transcript);
	}

	@Override
	public ResponseEntity<?> sttSocket(String audio) {
		if (audio == null || audio.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BadRequestResponse("Audio param cannot be null."));
		}
		
		log.debug("Invoking STT-Socket method");
		long init = System.currentTimeMillis();

		Authenticator authenticator = new IamAuthenticator(apikey);
		SpeechToText service = new SpeechToText(authenticator);
		
		SpeechRecognitionResults transcript = null;
		HttpStatus httpStatus = HttpStatus.OK;

		try {
			InputStream audioFile = new FileInputStream(baseUtil.getDefaultInputDirectory() + audio);

			RecognizeOptions options = new RecognizeOptions.Builder().audio(audioFile)
					.model(RecognizeOptions.Model.PT_BR_BROADBANDMODEL).contentType(HttpMediaType.AUDIO_WAV)
					.interimResults(true).build();

			service.recognizeUsingWebSocket(options, new BaseRecognizeCallback() {
				public void onTranscription(SpeechRecognitionResults speechResults) {
					super.onTranscription(speechResults);
					System.out.println(speechResults);
				}
			});

			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		long end = System.currentTimeMillis();
		log.debug("Processing time: " + (end - init) + " milliseconds");
		log.debug("Closing STT-Socket method");

		return ResponseEntity.status(httpStatus).body(transcript);
	}
}
