/*
 * #%L
 * Simple JSF Exporter Core
 * %%
 * Copyright (C) 2015 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 *  
 * Copyright 2015 Kiril Arabadzhiyski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.a9ski.jsf.exporter.dto;

import java.io.Serializable;

import javax.el.MethodExpression;

/**
 * DTO containing preProcessor/postProcessor methods
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class CallbackDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6212035105611244216L;

	private final MethodExpression preProcessor;
	private final MethodExpression postProcessor;

	public CallbackDto(final MethodExpression preProcessor, final MethodExpression postProcessor) {
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
