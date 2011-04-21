package com.zimbra.cs.account.ldap.upgrade;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.ZimbraLdapContext;
import com.zimbra.cs.account.ldap.legacy.LegacyLdapUtil;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.SearchLdapOptions;
import com.zimbra.cs.ldap.SearchLdapOptions.SearchLdapVisitor;
import com.zimbra.cs.prov.ldap.LdapFilter;

public class Bug53745 extends LdapUpgrade {

    private static String ATTR_IMPORTEXPORT = Provisioning.A_zimbraFeatureImportExportFolderEnabled;
    private static String ATTR_IMPORT = Provisioning.A_zimbraFeatureImportFolderEnabled;
    private static String ATTR_EXPORT = Provisioning.A_zimbraFeatureExportFolderEnabled;
    
    Bug53745() throws ServiceException {
    }
    
    @Override
    void doUpgrade() throws ServiceException {
        ZimbraLdapContext zlc = new ZimbraLdapContext(true);
        try {
            doCos(zlc);
            doAccount(zlc);
        } finally {
            ZimbraLdapContext.closeContext(zlc);
        }

    }
    
    private static class Bug53745Visitor implements SearchLdapVisitor {
        private ZimbraLdapContext mModZlc;
        
        Bug53745Visitor(ZimbraLdapContext modZlc) {
            mModZlc = modZlc;
        }
        
        public void visit(String dn, Map<String, Object> attrs, IAttributes ldapAttrs) {
            Attributes modAttrs = new BasicAttributes(true);
            
            try {
                String importExportVal = ldapAttrs.getAttrString(ATTR_IMPORTEXPORT);
                String importVal = ldapAttrs.getAttrString(ATTR_IMPORT);
                String exportVal = ldapAttrs.getAttrString(ATTR_EXPORT);
                
                if (importExportVal != null) {
                    if (importVal == null) {
                        modAttrs.put(ATTR_IMPORT, importExportVal);
                    }
                    
                    if (exportVal == null) {
                        modAttrs.put(ATTR_EXPORT, importExportVal);
                    }
                } 
                
                if (modAttrs.size() > 0) {
                    System.out.print("Modifying " + dn + ": " + modAttrs.toString());
                    mModZlc.replaceAttributes(dn, modAttrs);
                }
            } catch (NamingException e) {
                // log and continue
                System.out.println("Caught NamingException while modifying " + dn);
                e.printStackTrace();
            } catch (ServiceException e) {
                // log and continue
                System.out.println("Caught ServiceException while modifying " + dn);
                e.printStackTrace();
            }
        }
    }
    
    private void upgrade(ZimbraLdapContext modZlc, String bases[], String query) {
        SearchLdapOptions.SearchLdapVisitor visitor = new Bug53745Visitor(modZlc);

        String attrs[] = new String[] {ATTR_IMPORTEXPORT, ATTR_IMPORT, ATTR_EXPORT};
        
        for (String base : bases) {
            try {
                LegacyLdapUtil.searchLdapOnMaster(base, query, attrs, visitor);
            } catch (ServiceException e) {
                // log and continue
                System.out.println("Caught ServiceException while searching " + query + " under base " + base);
                e.printStackTrace();
            }
        }
    }
    
    private String query() {
        return "(" + ATTR_IMPORTEXPORT + "=*)";
    }
    
    private void doCos(ZimbraLdapContext modZlc) {
        String bases[] = mProv.getSearchBases(Provisioning.SD_COS_FLAG);
        String query = "(&" + LdapFilter.allCoses() + query() + ")";
        upgrade(modZlc, bases, query);
    }
    
    private void doAccount(ZimbraLdapContext modZlc) {
        String bases[] = mProv.getSearchBases(Provisioning.SA_ACCOUNT_FLAG);
        String query = "(&" + LdapFilter.allAccounts() + query() + ")";
        upgrade(modZlc, bases, query);
    }
    
}
