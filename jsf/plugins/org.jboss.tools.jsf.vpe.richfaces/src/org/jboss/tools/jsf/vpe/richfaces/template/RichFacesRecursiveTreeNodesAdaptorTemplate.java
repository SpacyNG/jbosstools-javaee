/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jsf.vpe.richfaces.template;

import org.jboss.tools.jsf.vpe.richfaces.ComponentUtil;
import org.jboss.tools.jsf.vpe.richfaces.HtmlComponentUtil;
import org.jboss.tools.jsf.vpe.richfaces.RichFacesTemplatesActivator;
import org.jboss.tools.vpe.editor.context.VpePageContext;
import org.jboss.tools.vpe.editor.template.VpeAbstractTemplate;
import org.jboss.tools.vpe.editor.template.VpeChildrenInfo;
import org.jboss.tools.vpe.editor.template.VpeCreationData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Create template for rich:recursiveTreeNodesAdaptor element
 * 
 * @author dsakovich@exadel.com
 * 
 */
public class RichFacesRecursiveTreeNodesAdaptorTemplate extends
	VpeAbstractTemplate {

    private static final String TREE_NAME = "tree";

    private final static String TREE_NODE_NAME = "treeNode";

    public final static String TREE_NODES_ADAPTOR_NAME = "treeNodesAdaptor";

    public final static String RECURSIVE_TREE_NODES_ADAPTOR_NAME = "recursiveTreeNodesAdaptor";

    private static final String STYLE_PATH = "/tree/tree.css";

    public static final String ICON_DIV_LINE = "/tree/divLine.gif";

    private static final String ADAPTER_LINES_STYLE = "background-position: left center; background-repeat: repeat-y;";

    public static final String ID_ATTR_NAME = "ID";

    public VpeCreationData create(VpePageContext pageContext, Node sourceNode,
	    Document visualDocument) {
	ComponentUtil.setCSSLink(pageContext, STYLE_PATH, "recursiveTreeNodesAdaptor");
	Element visualElement = visualDocument
		.createElement(HtmlComponentUtil.HTML_TAG_DIV);
	visualElement.setAttribute(ID_ATTR_NAME, RECURSIVE_TREE_NODES_ADAPTOR_NAME);
	if (isHasParentAdapter(sourceNode)) {
	    visualElement.setAttribute(HtmlComponentUtil.HTML_CLASS_ATTR,
		    "dr-tree-h-ic-div");
	    if (getShowLinesAttr(sourceNode)) {
		String path = RichFacesTemplatesActivator
			.getPluginResourcePath()
			+ ICON_DIV_LINE;
		visualElement.setAttribute(HtmlComponentUtil.HTML_STYLE_ATTR,
			"background-image: url(file://" + path + "); "
				+ ADAPTER_LINES_STYLE);
	    }
	}
	VpeCreationData vpeCreationData = new VpeCreationData(visualElement);
	parseTree(pageContext, sourceNode, visualDocument, vpeCreationData,
		visualElement);
	return vpeCreationData;
    }

    /**
     * 
     * Function for parsing tree by tree nodes;
     * 
     * @param pageContext
     * @param sourceNode
     * @param visualDocument
     * @return
     */
    public void parseTree(VpePageContext pageContext, Node sourceNode,
	    Document visualDocument, VpeCreationData vpeCreationData,
	    Element parentElement) {
	NodeList nodeList = sourceNode.getChildNodes();
	Element element = null;
	int lenght = nodeList.getLength();
	String treeNodeName = sourceNode.getPrefix() + ":" + TREE_NODE_NAME;
	String treeNodesAdaptorName = sourceNode.getPrefix() + ":"
		+ TREE_NODES_ADAPTOR_NAME;
	String recursiveTreeNodesAdaptorName = sourceNode.getPrefix() + ":"
		+ RECURSIVE_TREE_NODES_ADAPTOR_NAME;
	VpeChildrenInfo vpeChildrenInfo = null;
	for (int i = 0; i < lenght; i++) {
	    if (!(nodeList.item(i) instanceof Element)) {
		continue;
	    }
	    element = (Element) nodeList.item(i);
	    if (element.getNodeName().equals(treeNodeName)
		    || element.getNodeName().equals(
			    recursiveTreeNodesAdaptorName)) {
		vpeChildrenInfo = new VpeChildrenInfo(parentElement);
		vpeCreationData.addChildrenInfo(vpeChildrenInfo);
		vpeChildrenInfo.addSourceChild(element);
	    } else if (element.getNodeName().equals(treeNodesAdaptorName)) {
		vpeChildrenInfo = new VpeChildrenInfo(parentElement);
		vpeCreationData.addChildrenInfo(vpeChildrenInfo);
		vpeChildrenInfo.addSourceChild(element);
	    }
	}
    }

    /**
     * 
     * @param sourceNode
     * @return
     */
    public boolean isHasParentAdapter(Node sourceNode) {
	String treeNodesAdaptorName = sourceNode.getPrefix() + ":"
		+ TREE_NODES_ADAPTOR_NAME;
	String recursiveTreeNodesAdaptorName = sourceNode.getPrefix() + ":"
		+ RECURSIVE_TREE_NODES_ADAPTOR_NAME;
	Node node = sourceNode.getParentNode();
	if (node.getNodeName().equals(treeNodesAdaptorName)
		|| node.getNodeName().equals(recursiveTreeNodesAdaptorName)) {
	    return true;
	}
	return false;
    }

    /**
     * Get showConnectingLines attribute
     * 
     * @param sourceNode
     * @return
     */
    private boolean getShowLinesAttr(Node sourceNode) {
	String treeName = sourceNode.getPrefix() + ":" + TREE_NAME;
	do {
	    sourceNode = sourceNode.getParentNode();
	    if (!(sourceNode instanceof Element)) {
		return true;
	    }
	} while (!sourceNode.getNodeName().equals(treeName));

	String showLinesParam = ((Element) sourceNode)
		.getAttribute(RichFacesTreeTemplate.SHOW_LINES_ATTR_NAME);

	boolean showLinesValue = true;
	if (showLinesParam != null && showLinesParam.equalsIgnoreCase("false")) {
	    showLinesValue = false;
	}
	return showLinesValue;
    }
}
