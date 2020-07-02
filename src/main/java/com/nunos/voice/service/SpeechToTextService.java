package com.nunos.voice.service;

import org.springframework.http.ResponseEntity;

public interface SpeechToTextService {
	public ResponseEntity<?> sttHtml(String audio);
	public ResponseEntity<?> sttSocket(String audio);
}
