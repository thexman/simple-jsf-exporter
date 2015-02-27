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

	public DataExporterTagHandler(TagConfig tagConfig) {
		super(tagConfig);
		this.sourceTag = getRequiredAttribute("source");
		this.optionsTag = getAttribute("options");
		this.classNameTag = getRequiredAttribute("exporterClassName");
		this.classTag = getRequiredAttribute("exporterClass");
		this.classLoaderTag = getRequiredAttribute("exporterClassLoader");
		this.fileTypeTag = getRequiredAttribute("fileType");
		this.fileNameTag = getRequiredAttribute("fileName");		
		this.preProcessorTag = getAttribute("preProcessor");
		this.postProcessorTag = getAttribute("postProcessor");
	}
	
	private ValueExpression getValueExpression(FaceletContext ctx, TagAttribute attr, Class<?> type) {
		if (attr != null) {
			attr.getValueExpression(ctx, type);
		}
		return null;
	}
	
	private MethodExpression getMethodExpression(FaceletContext ctx, TagAttribute attr, Class<?> returnType, Class<?>[] paramTypes) {
		if (attr != null) {
			attr.getMethodExpression(ctx, returnType, paramTypes);
		}
		return null;
	}
	
	private MethodExpression getPrePostProcessorMethodExpression(FaceletContext ctx, TagAttribute attr) {
		return getMethodExpression(ctx, attr, null, new Class[]{ Object.class });
	}

	public void apply(FaceletContext ctx, UIComponent parent) throws IOException, FacesException, FaceletException, ELException {
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
