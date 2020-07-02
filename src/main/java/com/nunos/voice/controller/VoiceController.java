package com.nunos.voice.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nunos.voice.model.SpeechToTextRequest;
import com.nunos.voice.model.TextToSpeechRequest;
import com.nunos.voice.service.SpeechToTextService;
import com.nunos.voice.service.TextToSpeechService;

@RestController
@RequestMapping("/api")
public class VoiceController {

	static Logger log = LogManager.getLogger();

	@Autowired
	SpeechToTextService sttService;

	@Autowired
	TextToSpeechService ttsService;

	@PostMapping("/stt_html")
	public ResponseEntity<?> sttHtml(@RequestBody SpeechToTextRequest sttRequest) {
		log.debug("SpeechToText_HTML resource initialized");
		return sttService.sttHtml(sttRequest.getAudio());
	}

	@PostMapping("/stt_socket")
	public ResponseEntity<?> sttSocket(@RequestBody SpeechToTextRequest sttRequest) {
		log.debug("SpeechToText_Socket resource initialized");
		return sttService.sttSocket(sttRequest.getAudio());
	}

	@PostMapping("/tts_html")
	public ResponseEntity<?> ttsHtml(@RequestBody TextToSpeechRequest ttsRequest) {
		log.debug("TextToSpeech_HTML resource initialized");
		return ttsService.ttsHtml(ttsRequest.getText());
	}

	@PostMapping("/tts_socket")
	public ResponseEntity<?> ttsSocket(@RequestBody TextToSpeechRequest ttsRequest) {
		log.debug("TextToSpeech_Socket resource initialized");
		return ttsService.ttsSocket(ttsRequest.getText());
	}
}
