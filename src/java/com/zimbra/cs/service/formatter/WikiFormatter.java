/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Server.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.WikiItem;
import com.zimbra.cs.service.ServiceException;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.UserServlet.Context;
import com.zimbra.cs.util.ByteUtil;
import com.zimbra.cs.wiki.WikiTemplate;
import com.zimbra.cs.wiki.WikiTemplateStore;

public class WikiFormatter extends Formatter {

	@Override
	public String getType() {
		return "wiki";
	}

	@Override
	public boolean canBeBlocked() {
		return true;
	}
	
    private void handleDocument(Context context, Document doc) throws IOException, ServiceException {
        context.resp.setContentType(doc.getContentType());
        InputStream is = doc.getRawDocument();
        ByteUtil.copy(is, true, context.resp.getOutputStream(), false);
    }
    
    private void handleWiki(Context context, WikiItem wiki) throws IOException, ServiceException {
    	Folder f = context.targetMailbox.getFolderById(context.opContext, wiki.getFolderId());
    	String template = getTemplate(context, f, CHROME);
    	WikiTemplate wt = new WikiTemplate(template);
    	context.resp.setContentType(WikiItem.WIKI_CONTENT_TYPE);
    	context.resp.getOutputStream().print(wt.toString(context.opContext, wiki));
    }

    private static final String TOC = "_INDEX_";
    private static final String CHROME = "_CHROME_";
    
    private String getTemplate(Context context, Folder folder, String name) throws IOException, ServiceException {
    	WikiTemplateStore wiki = WikiTemplateStore.getInstance(context.authAccount.getName(), folder.getId());
    	return wiki.getTemplate(context.opContext, name);
    }
    private String getDefaultTOC() {
    	return WikiTemplateStore.getDefaultTOC();
    }
    
    private void handleWikiFolder(Context context, Folder folder) throws IOException, ServiceException {
    	StringBuffer ret = new StringBuffer();
    	
    	String template = getTemplate(context, folder, TOC);
    	
    	if (template == null) {
    		template = getDefaultTOC();
    	}
    	WikiTemplate wt = new WikiTemplate(template);
    	ret.append(wt.toString(context.opContext, folder));
    	
    	context.resp.setContentType(WikiItem.WIKI_CONTENT_TYPE);
    	context.resp.getOutputStream().print(ret.toString());
    }
    
	@Override
	public void format(Context context, MailItem item)
			throws UserServletException, ServiceException, IOException,
			ServletException {
        if (item instanceof Folder && !context.itemPath.endsWith("/")) {
        	context.resp.sendRedirect(context.req.getRequestURI() + "/");
        	return;
        }
        if (item instanceof WikiItem) {
            handleWiki(context, (WikiItem) item);
        } else if (item instanceof Document) {
            handleDocument(context, (Document) item);
        } else if (item instanceof Folder) {
            handleWikiFolder(context, (Folder) item);
        } else {
            throw UserServletException.notImplemented("can only handle Wiki messages and Documents");
        }
	}

	@Override
	public void save(byte[] body, Context context, Folder folder)
			throws UserServletException, ServiceException, IOException,
			ServletException {
        throw UserServletException.notImplemented("saving documents via POST not yet supported.");
	}

}
