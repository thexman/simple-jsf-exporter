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

import javax.el.ValueExpression;

/**
 * DTO containing the info about the exporter class (either class name and
 * classloader OR the class object)
 * 
 * @author Kiril Arabadzhiyski
 *
 */
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
