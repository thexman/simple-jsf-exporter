package com.a9ski.jsf.exporter.dto;

import java.io.Serializable;

import javax.el.MethodExpression;
import javax.faces.view.facelets.TagAttribute;

public class CallbackDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6212035105611244216L;

	
	private final MethodExpression preProcessor;
	private final MethodExpression postProcessor;
	
	public CallbackDto(MethodExpression preProcessor, MethodExpression postProcessor) {
		super();
		this.preProcessor = preProcessor;
		this.postProcessor = postProcessor;
	}

	public MethodExpression getPreProcessor() {
		return preProcessor;
	}

	public MethodExpression getPostProcessor() {
		return postProcessor;
	}
}
