/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.squirrel_lang.sqdev.ui.editor;




import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.squirrel_lang.sqdev.SQDevPlugin;
import org.squirrel_lang.sqdev.ui.editor.squirrel.SquirrelAutoIndentStrategy;
import org.squirrel_lang.sqdev.ui.editor.squirrel.SquirrelCompletionProcessor;
import org.squirrel_lang.sqdev.ui.editor.squirrel.SquirrelDoubleClickSelector;
import org.squirrel_lang.sqdev.ui.editor.squirreldoc.SquirreDocCompletionProcessor;
import org.squirrel_lang.sqdev.ui.editor.util.SquirreColorProvider;

/**
 * Example configuration for an <code>SourceViewer</code> which shows Java code.
 */
public class SquirrelSourceViewerConfiguration extends SourceViewerConfiguration {
	
	
		/**
		 * Single token scanner.
		 */
		static class SingleTokenScanner extends BufferedRuleBasedScanner {
			public SingleTokenScanner(TextAttribute attribute) {
				setDefaultReturnToken(new Token(attribute));
			}
		}
		

	/**
	 * Default constructor.
	 */
	public SquirrelSourceViewerConfiguration() {
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new SquirrelAnnotationHover();
	}
		
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		IAutoEditStrategy strategy= (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? new SquirrelAutoIndentStrategy() : new DefaultIndentLineAutoEditStrategy());
		return new IAutoEditStrategy[] { strategy };
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return SQDevPlugin.SQUIRREL_PARTITIONING;
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, SquirrelPartitionScanner.SQUIRREL_CLASS_ATTRIBUTE, SquirrelPartitionScanner.SQUIRREL_MULTILINE_COMMENT, SquirrelPartitionScanner.SQUIRREL_MULTILINE_STRING };
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant= new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new SquirrelCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new SquirreDocCompletionProcessor(), SquirrelPartitionScanner.SQUIRREL_CLASS_ATTRIBUTE);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		//assistant.setContextInformationPopupBackground(SQDevPlugin.getDefault().getSquirrelColorProvider().getColor(new RGB(150, 150, 0)));

		return assistant;
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		return (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" : null); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new SquirrelDoubleClickSelector();
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "\t", "    " }; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		SquirreColorProvider provider= SQDevPlugin.getDefault().getSquirrelColorProvider();
		PresentationReconciler reconciler= new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(SQDevPlugin.getDefault().getSquirrelCodeScanner() );
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr= new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(provider.getColor(SquirreColorProvider.CLASS_ATTRIBUTE))));
		reconciler.setDamager(dr, SquirrelPartitionScanner.SQUIRREL_CLASS_ATTRIBUTE);
		reconciler.setRepairer(dr, SquirrelPartitionScanner.SQUIRREL_CLASS_ATTRIBUTE);

		dr= new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(provider.getColor(SquirreColorProvider.MULTI_LINE_COMMENT))));
		reconciler.setDamager(dr, SquirrelPartitionScanner.SQUIRREL_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, SquirrelPartitionScanner.SQUIRREL_MULTILINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(provider.getColor(SquirreColorProvider.MULTI_LINE_STRING))));
		reconciler.setDamager(dr, SquirrelPartitionScanner.SQUIRREL_MULTILINE_STRING);
		reconciler.setRepairer(dr, SquirrelPartitionScanner.SQUIRREL_MULTILINE_STRING);

		return reconciler;
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}
	
	/* (non-Javadoc)
	 * Method declared on SourceViewerConfiguration
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new SquirrelTextHover();
	}
}