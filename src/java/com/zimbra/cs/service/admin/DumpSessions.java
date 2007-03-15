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
 * Portions created by Zimbra are Copyright (C) 2006 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.cs.session.Session;
import com.zimbra.cs.session.SessionCache;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.common.soap.AdminConstants;

public class DumpSessions extends AdminDocumentHandler {
    
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Element response = lc.createElement(AdminConstants.DUMP_SESSIONS_RESPONSE);
        
        boolean includeAccounts = request.getAttributeBool(AdminConstants.A_LIST_SESSIONS, false);
        boolean groupByAccount = request.getAttributeBool(AdminConstants.A_GROUP_BY_ACCOUNT, false);
        int totalActiveSessions = 0;
        
        for (Session.Type type : Session.Type.values()) {
            if (type == Session.Type.NULL)
                continue;
            
            if (!includeAccounts) {
                int[] stats = SessionCache.countActive(type);

                if (stats[1] == 0)
                    continue; // no active sessions, skip this type!
                
                Element e = response.addElement(type.name().toLowerCase());
                totalActiveSessions += stats[1];
                e.addAttribute(AdminConstants.A_ACTIVE_ACCOUNTS, stats[0]);
                e.addAttribute(AdminConstants.A_ACTIVE_SESSIONS, stats[1]);
            } else {
                List<Session> sessions = SessionCache.getActiveSessions(type);
                if (sessions.size() == 0)
                    continue; // no active sessions, skip this type!
                
                Element e = response.addElement(type.name().toLowerCase());
                totalActiveSessions+=sessions.size();
                e.addAttribute(AdminConstants.A_ACTIVE_SESSIONS, sessions.size());
                if (sessions.size() == 0)
                    continue;
                
                if (groupByAccount) {
                    // stick the sessions into a big map organized by the account ID
                    HashMap<String/*accountid*/, List<Session>> map = new HashMap<String, List<Session>>();
                    for (Session s : sessions) {
                        List<Session> list = map.get(s.getAccountId());
                        if (list == null) {
                            list = new ArrayList<Session>();
                            map.put(s.getAccountId(), list);
                        }
                        list.add(s);
                    }
                    
                    e.addAttribute(AdminConstants.A_ACTIVE_ACCOUNTS, map.size());
                    
                    for (Map.Entry<String, List<Session>> entry : map.entrySet()) {
                        Element acctElt = e.addElement(AdminConstants.A_ZIMBRA_ID);
                        acctElt.addAttribute(AdminConstants.A_ID, entry.getKey());
                        for (Session s : entry.getValue()) {
                            encodeSession(acctElt, s, false);
                        }
                    }
                } else {
                    int[] stats = SessionCache.countActive(type);
                    e.addAttribute(AdminConstants.A_ACTIVE_ACCOUNTS, stats[0]);
                    
                    for (Session s : sessions) {
                        encodeSession(e, s, true);
                    }
                }
            }
        }
        response.addAttribute(AdminConstants.A_ACTIVE_SESSIONS, totalActiveSessions);
        
        return response;
    }
    
    private static void encodeSession(Element parent, Session s, boolean includeAcct) {
        Element sElt = parent.addElement("s");
        if (includeAcct) {
            sElt.addAttribute(AdminConstants.A_ZIMBRA_ID, s.getAccountId());
        }
        sElt.addAttribute(AdminConstants.A_SESSION_ID, s.getSessionId());
        sElt.addAttribute(AdminConstants.A_CREATED_DATE, s.getCreationTime());
        sElt.addAttribute(AdminConstants.A_LAST_ACCESSED_DATE, s.getLastAccessTime());
        s.encodeState(sElt);
    }
}
