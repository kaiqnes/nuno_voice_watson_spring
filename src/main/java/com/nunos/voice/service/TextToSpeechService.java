package com.nunos.voice.service;

import org.springframework.http.ResponseEntity;

public interface TextToSpeechService {
	public ResponseEntity<?> ttsHtml(String text);
	public ResponseEntity<?> ttsSocket(String text);
}
