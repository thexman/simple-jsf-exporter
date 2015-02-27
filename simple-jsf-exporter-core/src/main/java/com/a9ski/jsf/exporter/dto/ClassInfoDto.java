package com.a9ski.jsf.exporter.dto;

import java.io.Serializable;

import javax.el.ValueExpression;

public class ClassInfoDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7547535505642660853L;

	private final ValueExpression className;
	private final ValueExpression classValue;
	private final ValueExpression classLoader;

	public ClassInfoDto(final ValueExpression className, final ValueExpression classValue, final ValueExpression classLoader) {
		super();
		this.className = className;
		this.classValue = classValue;
		this.classLoader = classLoader;
	}

	public ValueExpression getClassName() {
		return className;
	}

	public ValueExpression getClassValue() {
		return classValue;
	}

	public ValueExpression getClassLoader() {
		return classLoader;
	}
}
