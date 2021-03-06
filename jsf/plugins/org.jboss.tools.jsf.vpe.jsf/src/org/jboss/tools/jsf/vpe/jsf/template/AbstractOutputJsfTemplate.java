/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jsf.vpe.jsf.template;

import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.jboss.tools.jsf.vpe.jsf.VpeElementProxyData;
import org.jboss.tools.vpe.editor.context.VpePageContext;
import org.jboss.tools.vpe.editor.mapping.AttributeData;
import org.jboss.tools.vpe.editor.mapping.NodeData;
import org.jboss.tools.vpe.editor.mapping.VpeDomMapping;
import org.jboss.tools.vpe.editor.mapping.VpeElementData;
import org.jboss.tools.vpe.editor.mapping.VpeElementMapping;
import org.jboss.tools.vpe.editor.mapping.VpeNodeMapping;
import org.jboss.tools.vpe.editor.template.VpeChildrenInfo;
import org.jboss.tools.vpe.editor.template.VpeCreationData;
import org.jboss.tools.vpe.editor.util.HTML;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMElement;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIDOMText;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractOutputJsfTemplate extends AbstractEditableJsfTemplate {

	/*
	 * https://issues.jboss.org/browse/JBIDE-9417
	 * For some templates escape attribute name could differ,
	 * so it was made editable.
	 */
	protected String escapeAttributeName = JSF.ATTR_ESCAPE;
	
	/**
	 * copy outputAttributes
	 * 
	 * @param visualElement
	 * @param sourceElement
	 */
	protected void copyOutputJsfAttributes(nsIDOMElement visualElement, Element sourceElement) {
		copyGeneralJsfAttributes(sourceElement, visualElement);
		copyAttribute(visualElement, sourceElement, JSF.ATTR_DIR, HTML.ATTR_DIR);
	}

	protected void processOutputAttribute(VpePageContext pageContext,
			nsIDOMDocument visualDocument, Element sourceElement,
			nsIDOMElement targetVisualElement, VpeCreationData creationData) {
		VpeElementProxyData elementData = new VpeElementProxyData();
		Attr outputAttr = getOutputAttributeNode(sourceElement);
		if (outputAttr != null) {
			String newValue = prepareAttrValue(pageContext, sourceElement, outputAttr);
			/*
			 * if escape then contents of value (or other attribute) is only text
			 */
			if (!sourceElement.hasAttribute(escapeAttributeName)
					|| "true".equalsIgnoreCase(sourceElement.getAttribute(escapeAttributeName))) { //$NON-NLS-1$
				String value = outputAttr.getValue();
				nsIDOMText text;
				// if bundleValue differ from value then will be represent
				// bundleValue, but text will be not edit
				boolean isEditable = value.equals(newValue);
				text = visualDocument.createTextNode(newValue);
				// add attribute for ability of editing
				elementData.addNodeData(new AttributeData(outputAttr, text, isEditable));
				targetVisualElement.appendChild(text);
			} else {
				/*
				 *  then text can be html code
				 *  create VpeChildrenInfo to process source nodes
				 */
				VpeChildrenInfo targetVisualInfo = new VpeChildrenInfo(targetVisualElement);
				// get atribute's offset
				//mareshkau because it's node can be a proxy, see JBIDE-3144
				if(!(outputAttr instanceof IDOMAttr)) {
					outputAttr = (Attr) ((((Attr)outputAttr).getOwnerElement())
							.getAttributes().getNamedItem(outputAttr.getLocalName()));
				}
				int offset = ((IDOMAttr) outputAttr).getValueRegionStartOffset();
				// reparse attribute's value
				NodeList list = NodeProxyUtil.reparseAttributeValue(
						elementData, newValue, offset + 1);
				// add children to info
				for (int i = 0; i < list.getLength(); i++) {
					Node child = list.item(i);
					// add info to creation data
					targetVisualInfo.addSourceChild(child);
				}
				elementData.addNodeData(new AttributeData(outputAttr, targetVisualElement, true));
				creationData.addChildrenInfo(targetVisualInfo);
			}
		}
		creationData.setElementData(elementData);
	}

	@Override
	public void setPseudoContent(VpePageContext pageContext,
			Node sourceContainer, nsIDOMNode visualContainer,
			nsIDOMDocument visualDocument) {
		/*
		 * Do nothing
		 */
	}

	@Override
	public NodeData getNodeData(nsIDOMNode node, VpeElementData elementData,
			VpeDomMapping domMapping) {
		NodeData nodeData = super.getNodeData(node, elementData, domMapping);
		if (nodeData == null) {
			VpeNodeMapping nodeMapping = domMapping.getNodeMapping(node);
			if (nodeMapping != null) {
				if (nodeMapping instanceof VpeElementMapping) {
					nodeData = super.getNodeData(node,
							((VpeElementMapping) nodeMapping).getElementData(),
							domMapping);
				} 
//				else if (nodeMapping.getType() == VpeNodeMapping.TEXT_MAPPING) {
//					nodeData = new NodeData(nodeMapping.getSourceNode(), node,
//							true);
//				}
			}
		}
		return nodeData;
	}

	@Override
	public nsIDOMNode getVisualNodeBySourcePosition(
			VpeElementMapping elementMapping, Point selectionRange, VpeDomMapping domMapping) {
		nsIDOMNode node = null;
		if ((elementMapping.getElementData() instanceof VpeElementProxyData)
				&& (((VpeElementProxyData) elementMapping.getElementData()).getNodelist() != null)) {
			VpeElementProxyData elementProxyData = (VpeElementProxyData) elementMapping.getElementData();
			VpeNodeMapping nodeMapping = NodeProxyUtil.findNodeByPosition(
					domMapping, elementProxyData.getNodelist(), selectionRange);
			if (nodeMapping != null) {
				if (nodeMapping instanceof VpeElementMapping) {
					node = super.getVisualNodeBySourcePosition(
							(VpeElementMapping) nodeMapping, selectionRange, domMapping);
				} else {
					node = nodeMapping.getVisualNode();
				}
			}
		}
		if (node == null) {
			node = super.getVisualNodeBySourcePosition(elementMapping, selectionRange, domMapping);
		}
		return node;
	}

	protected String prepareAttrValue(VpePageContext pageContext, Element parent, Attr attr) {
		/*
		 * Currently used only in JsfOutputFormatTemplate
		 */
		return attr.getNodeValue();
	}
	
	/**
	 * If tag uses different attribute name than "escape" for displaying escaped sequence --
	 * subclasses could set an appropriate attribute name directly
	 * 
	 * @param escapeAttributeName the new attribute name
	 */
	protected void setEscapeAttributeName(String escapeAttributeName) {
		this.escapeAttributeName = escapeAttributeName;
	}
	
}
