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
 * DTO containing info about the file (file type and name)
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class FileInfoDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5407190444968339112L;

	private final ValueExpression fileName;
	private final ValueExpression fileType;

	public FileInfoDto(final ValueExpression fileName, final ValueExpression fileType) {
		super();
		this.fileName = fileName;
		this.fileType = fileType;
	}

	public ValueExpression getFileName() {
		return fileName;
	}

	public ValueExpression getFileType() {
		return fileType;
	}
}
