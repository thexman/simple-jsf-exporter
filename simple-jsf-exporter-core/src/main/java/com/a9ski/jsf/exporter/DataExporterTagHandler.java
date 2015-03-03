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
package com.a9ski.jsf.exporter;

import java.io.IOException;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import com.a9ski.jsf.exporter.dto.CallbackDto;
import com.a9ski.jsf.exporter.dto.ClassInfoDto;
import com.a9ski.jsf.exporter.dto.FileInfoDto;

/**
 * Export Tag handler
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class DataExporterTagHandler extends TagHandler {
	private final TagAttribute sourceTag;
	private final TagAttribute optionsTag;
	private final TagAttribute fileTypeTag;
	private final TagAttribute fileNameTag;
	private final TagAttribute classNameTag;
	private final TagAttribute classTag;
	private final TagAttribute classLoaderTag;
	private final TagAttribute preProcessorTag;
	private final TagAttribute postProcessorTag;

	public DataExporterTagHandler(final TagConfig tagConfig) {
		super(tagConfig);
		this.sourceTag = getRequiredAttribute("source");
		this.optionsTag = getAttribute("options");
		this.classNameTag = getAttribute("exporterClassName");
		this.classTag = getAttribute("exporterClass");
		this.classLoaderTag = getAttribute("exporterClassLoader");
		this.fileTypeTag = getRequiredAttribute("fileType");
		this.fileNameTag = getRequiredAttribute("fileName");
		this.preProcessorTag = getAttribute("preProcessor");
		this.postProcessorTag = getAttribute("postProcessor");
	}

	private ValueExpression getValueExpression(final FaceletContext ctx, final TagAttribute attr, final Class<?> type) {
		final ValueExpression exp;
		if (attr != null) {
			exp = attr.getValueExpression(ctx, type);
		} else {
			exp = null;
		}
		return exp;
	}

	private MethodExpression getMethodExpression(final FaceletContext ctx, final TagAttribute attr, final Class<?> returnType, final Class<?>[] paramTypes) {
		final MethodExpression exp;
		if (attr != null) {
			exp = attr.getMethodExpression(ctx, returnType, paramTypes);
		} else {
			exp = null;
		}
		return exp;
	}

	private MethodExpression getPrePostProcessorMethodExpression(final FaceletContext ctx, final TagAttribute attr) {
		return getMethodExpression(ctx, attr, null, new Class[] { Object.class });
	}

	@Override
	public void apply(final FaceletContext ctx, final UIComponent parent) throws IOException, FacesException, FaceletException, ELException {
		if (ComponentHandler.isNew(parent)) {
			final ValueExpression source = sourceTag.getValueExpression(ctx, Object.class);
			final ValueExpression options = getValueExpression(ctx, optionsTag, Object.class);

			final ValueExpression exporterClassName = getValueExpression(ctx, classNameTag, String.class);
			final ValueExpression exporterClass = getValueExpression(ctx, classTag, Class.class);
			final ValueExpression exporterClassLoader = getValueExpression(ctx, classLoaderTag, Class.class);
			final ClassInfoDto classInfo = new ClassInfoDto(exporterClassName, exporterClass, exporterClassLoader);

			final ValueExpression fileType = getValueExpression(ctx, fileTypeTag, String.class);
			final ValueExpression fileName = getValueExpression(ctx, fileNameTag, Object.class);
			final FileInfoDto fileInfo = new FileInfoDto(fileName, fileType);

			final MethodExpression preProcessor = getPrePostProcessorMethodExpression(ctx, preProcessorTag);
			final MethodExpression postProcessor = getPrePostProcessorMethodExpression(ctx, postProcessorTag);
			final CallbackDto callbackTags = new CallbackDto(preProcessor, postProcessor);

			if (parent instanceof ActionSource) {
				final ActionSource actionSource = (ActionSource) parent;
				actionSource.addActionListener(new DataExporterAction(source, options, fileInfo, classInfo, callbackTags));
			} else {
				throw new IllegalArgumentException("Expected ActionSource class");
			}
		}
	}
}
